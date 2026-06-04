package com.example.client_kurs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client_kurs.data.remote.OpenPricesApiService
import com.example.client_kurs.domain.model.DisplayProduct
import com.example.client_kurs.domain.model.SupplierOffer
import com.example.client_kurs.domain.usecase.CreatePurchaseUseCase
import com.example.client_kurs.domain.usecase.GetProductsUseCase
import com.example.client_kurs.domain.usecase.GetSuppliersUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@OptIn(FlowPreview::class)
class SupplierComparisonViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val getSuppliersUseCase: GetSuppliersUseCase,
    private val createPurchaseUseCase: CreatePurchaseUseCase,
    private val openPricesApi: OpenPricesApiService   // новый сервис
) : ViewModel() {

    // --- Локальные товары (склад) ---
    private val _localProducts = MutableStateFlow<List<DisplayProduct>>(emptyList())
    val localProducts: StateFlow<List<DisplayProduct>> = _localProducts.asStateFlow()

    // --- Поиск через Open Prices ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _offProducts = MutableStateFlow<List<DisplayProduct>>(emptyList())
    val offProducts: StateFlow<List<DisplayProduct>> = _offProducts.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    // --- Объединённый список (локальные + результаты поиска) ---
    val allProducts: StateFlow<List<DisplayProduct>> =
        combine(_localProducts, _offProducts) { locals, offs ->
            locals + offs
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Выбранный товар и поставщики ---
    private val _selectedProduct = MutableStateFlow<DisplayProduct?>(null)
    val selectedProduct: StateFlow<DisplayProduct?> = _selectedProduct.asStateFlow()

    private val _supplierOffers = MutableStateFlow<List<SupplierOffer>>(emptyList())
    val supplierOffers: StateFlow<List<SupplierOffer>> = _supplierOffers.asStateFlow()

    private val _marketPrice = MutableStateFlow(0.0)
    val marketPrice: StateFlow<Double> = _marketPrice.asStateFlow()

    private val _selectedSupplier = MutableStateFlow<SupplierOffer?>(null)
    val selectedSupplier: StateFlow<SupplierOffer?> = _selectedSupplier.asStateFlow()

    private val _purchaseQuantityInput = MutableStateFlow("1")
    val purchaseQuantityInput: StateFlow<String> = _purchaseQuantityInput.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadLocalProducts()
        // Поиск с debounce 400 мс
        viewModelScope.launch {
            _searchQuery
                .debounce(400)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    _searchError.value = null
                    if (query.isBlank()) {
                        _offProducts.value = emptyList()
                        _isSearching.value = false
                        flowOf(emptyList<DisplayProduct>())
                    } else {
                        flow {
                            _isSearching.value = true
                            try {
                                val results = openPricesApi.searchProducts(query)
                                if (results.isEmpty() && query.isNotBlank()) {
                                    _searchError.value = "Ничего не найдено"
                                }
                                val items = results.map { item ->
                                    DisplayProduct(
                                        id = item.id,
                                        name = item.name ?: "Без названия",
                                        price = null,
                                        isLocal = false
                                    )
                                }
                                emit(items)
                            } catch (e: Exception) {
                                _searchError.value = "Ошибка поиска: ${e.message}"
                                emit(emptyList())
                            } finally {
                                _isSearching.value = false
                            }
                        }
                    }
                }
                .collect { products ->
                    _offProducts.value = products
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /** Повтор последнего поискового запроса (при ошибке) */
    fun retrySearch() {
        _searchQuery.value = _searchQuery.value   // заново триггерит flow
    }

    // --- Загрузка локальных товаров ---
    fun loadLocalProducts() {
        viewModelScope.launch(Dispatchers.IO) {
            getProductsUseCase()
                .onSuccess { productList ->
                    _localProducts.value = productList.map {
                        DisplayProduct(
                            id = it.id,
                            name = it.name,
                            price = it.price,
                            isLocal = true
                        )
                    }
                    // Если был выбран товар, обновим информацию о нём
                    _selectedProduct.value?.let { selected ->
                        val updated = _localProducts.value.find { it.id == selected.id }
                        if (updated != null) {
                            _selectedProduct.value = updated
                            // перезагрузим поставщиков / цены
                            if (updated.isLocal) {
                                loadSuppliers(updated.id)
                            } else {
                                loadOpenPrices(updated.id)
                            }
                        }
                    }
                }
                .onFailure { _error.value = it.message ?: "Ошибка загрузки товаров" }
        }
    }

    // --- Выбор товара (и локального, и из результата поиска) ---
    fun selectProduct(product: DisplayProduct) {
        _selectedProduct.value = product
        _supplierOffers.value = emptyList()
        if (product.isLocal) {
            loadSuppliers(product.id)
        } else {
            loadOpenPrices(product.id)
        }
    }

    private fun loadSuppliers(productId: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            getSuppliersUseCase(productId)
                .onSuccess { list ->
                    _supplierOffers.value = list.sortedBy { it.price }
                    updateMarketPrice()
                }
                .onFailure {
                    _supplierOffers.value = emptyList()
                    updateMarketPrice()
                    _error.value = it.message ?: "Не удалось получить поставщиков"
                }
            _isLoading.value = false
        }
    }

    private fun loadOpenPrices(productId: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prices = openPricesApi.getPrices(productId)
                if (prices.isNotEmpty()) {
                    val offers = prices.mapNotNull { priceItem ->
                        val store = priceItem.store ?: priceItem.location ?: "Неизвестный магазин"
                        val price = priceItem.price ?: return@mapNotNull null
                        SupplierOffer(
                            supplierId = store,          // используем название магазина как ID
                            supplierName = store,
                            price = price
                        )
                    }
                    _supplierOffers.value = offers.sortedBy { it.price }
                    updateMarketPrice()
                } else {
                    _supplierOffers.value = emptyList()
                    // рыночная цена по локальному товару (если он выбран) или оставляем 0
                    val localPrice = _selectedProduct.value?.price
                    _marketPrice.value = localPrice?.times(1.12) ?: 100.0
                }
            } catch (e: Exception) {
                _supplierOffers.value = emptyList()
                _error.value = "Ошибка загрузки цен: ${e.message}"
                // fallback-цена
                val localPrice = _selectedProduct.value?.price
                _marketPrice.value = localPrice?.times(1.12) ?: 100.0
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Пересчитывает рыночную цену на основе offer-ов или цены локального товара */
    private fun updateMarketPrice() {
        val localPrice = _selectedProduct.value?.price
        val bestPrice = _supplierOffers.value.minOfOrNull { it.price }
        _marketPrice.value = bestPrice ?: (localPrice?.times(1.12) ?: 100.0)
    }

    // --- Заказ у конкретного поставщика ---
    fun openPurchaseDialog(supplierOffer: SupplierOffer) {
        _selectedSupplier.value = supplierOffer
        _purchaseQuantityInput.value = "1"
    }

    fun closePurchaseDialog() {
        _selectedSupplier.value = null
    }

    fun onPurchaseQuantityChange(value: String) {
        if (value.isNotEmpty() && value.any { !it.isDigit() }) return
        _purchaseQuantityInput.value = value
    }

    fun confirmPurchase() {
        val product = _selectedProduct.value ?: run {
            _error.value = "Сначала выберите товар"
            return
        }
        val supplierId = _selectedSupplier.value?.supplierId ?: "market"
        val quantity = _purchaseQuantityInput.value.toIntOrNull()
        if (quantity == null || quantity <= 0) {
            _error.value = "Введите корректное количество"
            return
        }
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            createPurchaseUseCase(product.id, supplierId, quantity)
                .onSuccess {
                    _successMessage.value = "Приход оформлен"
                    _selectedSupplier.value = null
                    loadLocalProducts() // обновить всё
                }
                .onFailure {
                    _error.value = it.message ?: "Не удалось оформить приход"
                    _isLoading.value = false
                }
        }
    }

    // --- Закупка по рыночной цене (без поставщика) ---
    fun purchaseAtMarketPrice() {
        val product = _selectedProduct.value ?: return
        val quantity = 1 // можно позже добавить запрос количества
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            createPurchaseUseCase(product.id, "market", quantity)
                .onSuccess {
                    _successMessage.value = "Закупка по рыночной цене оформлена"
                    loadLocalProducts()
                }
                .onFailure {
                    _error.value = it.message ?: "Не удалось оформить закупку"
                    _isLoading.value = false
                }
        }
    }

    fun clearError() { _error.value = null }
    fun clearSuccessMessage() { _successMessage.value = null }
}