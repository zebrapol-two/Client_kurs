package com.example.client_kurs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client_kurs.data.remote.api.KtorApiService
import com.example.client_kurs.data.remote.dto.ExternalProductDto
import com.example.client_kurs.data.remote.dto.OpenFoodFactsProductDto
import com.example.client_kurs.domain.model.DisplayProduct
import com.example.client_kurs.domain.model.Nutriments
import com.example.client_kurs.domain.model.NutriscoreComponent
import com.example.client_kurs.domain.model.NutriscoreData
import com.example.client_kurs.domain.model.OffProductItem
import com.example.client_kurs.domain.model.SupplierOffer
import com.example.client_kurs.domain.usecase.CreatePurchaseUseCase
import com.example.client_kurs.domain.usecase.GetProductsUseCase
import com.example.client_kurs.domain.usecase.GetSuppliersUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@OptIn(FlowPreview::class)
class SupplierComparisonViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val getSuppliersUseCase: GetSuppliersUseCase,
    private val createPurchaseUseCase: CreatePurchaseUseCase,
    private val apiService: KtorApiService
) : ViewModel() {

    private val _localProducts = MutableStateFlow<List<DisplayProduct>>(emptyList())
    val localProducts: StateFlow<List<DisplayProduct>> = _localProducts.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _offProducts = MutableStateFlow<List<DisplayProduct>>(emptyList())
    val offProducts: StateFlow<List<DisplayProduct>> = _offProducts.asStateFlow()

    private val _isLoadingSearch = MutableStateFlow(false)
    val isLoadingSearch: StateFlow<Boolean> = _isLoadingSearch.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    private data class SearchCacheEntry(
        val results: List<DisplayProduct>,
        val rawResults: Map<String, OffProductItem>,
        val cachedAt: Long
    )

    private val searchCache = mutableMapOf<String, SearchCacheEntry>()
    private val searchCacheMutex = Mutex()
    private val searchCacheTtlMs = 5 * 60 * 1000L
    private val searchDebounceMs = 600L

    val allProducts: StateFlow<List<DisplayProduct>> =
        combine(_localProducts, _offProducts) { locals, offs ->
            locals + offs
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProduct = MutableStateFlow<DisplayProduct?>(null)
    val selectedProduct: StateFlow<DisplayProduct?> = _selectedProduct.asStateFlow()

    private val _selectedOffProduct = MutableStateFlow<OffProductItem?>(null)
    val selectedOffProduct: StateFlow<OffProductItem?> = _selectedOffProduct.asStateFlow()

    private val _offProductsRaw = MutableStateFlow<Map<String, OffProductItem>>(emptyMap())

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
        viewModelScope.launch {
            _searchQuery
                .debounce(searchDebounceMs)
                .collectLatest { query ->
                    performSearch(query, forceRefresh = false)
                }
        }
        viewModelScope.launch {
            while (isActive) {
                delay(searchCacheTtlMs)
                clearExpiredSearchCache()
            }
        }
    }

    private suspend fun performSearch(query: String, forceRefresh: Boolean) {
        val normalizedQuery = query.trim()
        _searchError.value = null

        if (normalizedQuery.isBlank()) {
            _offProducts.value = emptyList()
            _offProductsRaw.value = emptyMap()
            _isLoadingSearch.value = false
            return
        }

        val cacheKey = normalizedQuery.lowercase() + "|20"
        if (!forceRefresh) {
            val cached = searchCacheMutex.withLock {
                searchCache[cacheKey]?.takeIf { System.currentTimeMillis() - it.cachedAt <= searchCacheTtlMs }
            }
            if (cached != null) {
                _offProducts.value = cached.results
                _offProductsRaw.value = cached.rawResults
                _selectedOffProduct.value = _selectedProduct.value
                    ?.takeIf { !it.isLocal }
                    ?.let { cached.rawResults[it.id] }
                return
            }
        }

        _isLoadingSearch.value = true
        try {
            val results = apiService.searchProducts(normalizedQuery)
            val offItems = results.map { it.toOffProductItem() }
            val items = offItems.map { item ->
                DisplayProduct(
                    id = item.code,
                    name = item.productName ?: item.brands ?: "Без названия",
                    price = null,
                    marketPrice = item.marketPrice,
                    isLocal = false
                )
            }
            val rawResults = offItems.associateBy { it.code }

            searchCacheMutex.withLock {
                searchCache[cacheKey] = SearchCacheEntry(items, rawResults, System.currentTimeMillis())
            }

            _offProducts.value = items
            _offProductsRaw.value = rawResults
            _selectedOffProduct.value = _selectedProduct.value
                ?.takeIf { !it.isLocal }
                ?.let { rawResults[it.id] }
            if (items.isEmpty()) {
                _searchError.value = "Ничего не найдено"
            }
        } catch (e: Exception) {
            _searchError.value = e.message?.takeIf { it.isNotBlank() } ?: "Ошибка поиска"
            _offProducts.value = emptyList()
        } finally {
            _isLoadingSearch.value = false
        }
    }

    private suspend fun clearExpiredSearchCache() {
        val now = System.currentTimeMillis()
        searchCacheMutex.withLock {
            val iterator = searchCache.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (now - entry.value.cachedAt > searchCacheTtlMs) {
                    iterator.remove()
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun retrySearch() {
        viewModelScope.launch {
            performSearch(_searchQuery.value, forceRefresh = true)
        }
    }

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
                }
                .onFailure { _error.value = it.message ?: "Ошибка загрузки товаров" }

            searchCacheMutex.withLock { searchCache.clear() }
        }
    }

    fun selectProduct(product: DisplayProduct) {
        _selectedProduct.value = product
        _supplierOffers.value = emptyList()
        if (product.isLocal) {
            _selectedOffProduct.value = null
            _marketPrice.value = product.price ?: 0.0
            loadSuppliers(product.id)
        } else {
            val off = _offProductsRaw.value[product.id]
            _selectedOffProduct.value = off
            val marketBase = product.marketPrice ?: 100.0
            _marketPrice.value = marketBase
            loadExternalSuppliers(product.id, marketBase)
        }
    }

    private fun loadSuppliers(productId: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            getSuppliersUseCase(productId)
                .onSuccess { list ->
                    _supplierOffers.value = list.sortedBy { it.price }
                }
                .onFailure {
                    _supplierOffers.value = emptyList()
                    _error.value = it.message ?: "Не удалось получить поставщиков"
                }
            _isLoading.value = false
        }
    }

    private fun loadExternalSuppliers(code: String, marketBase: Double) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = apiService.getExternalSuppliers(code, marketBase)
            if (result.isSuccess) {
                val offers = result.getOrNull() ?: emptyList()
                _supplierOffers.value = offers.sortedBy { it.price }
            } else {
                _supplierOffers.value = emptyList()
                _error.value = result.exceptionOrNull()?.message ?: "Не удалось загрузить бренды"
            }
            _isLoading.value = false
        }
    }

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
        val supplier = _selectedSupplier.value ?: run {
            _error.value = "Выберите поставщика"
            return
        }
        val quantity = _purchaseQuantityInput.value.toIntOrNull()
        if (quantity == null || quantity <= 0) {
            _error.value = "Введите корректное количество"
            return
        }
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            if (!product.isLocal) {
                val createResult = apiService.createExternalProduct(
                    code = product.id,
                    productName = product.name,
                    marketPrice = product.marketPrice ?: 100.0
                )
                if (!createResult.isSuccess) {
                    _error.value = createResult.exceptionOrNull()?.message ?: "Не удалось синхронизировать товар"
                    _isLoading.value = false
                    return@launch
                }
            }

            createPurchaseUseCase(product.id, supplier.supplierId, quantity)
                .onSuccess {
                    _successMessage.value = "Приход оформлен"
                    _selectedSupplier.value = null
                    loadLocalProducts()
                }
                .onFailure {
                    _error.value = it.message ?: "Не удалось оформить приход"
                }
            _isLoading.value = false
        }
    }

    fun purchaseAtMarketPrice() {
        val product = _selectedProduct.value ?: return
        val quantity = 1
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

    private fun OpenFoodFactsProductDto.toOffProductItem(): OffProductItem = OffProductItem(
        code = code,
        productName = productName,
        brands = brands,
        imageUrl = imageUrl,
        nutriments = null,
        ingredientsText = null,
        nutritionGrades = null,
        nutriscoreData = null,
        marketPrice = marketPrice
    )

    private fun ExternalProductDto.toOffProductItem(): OffProductItem = OffProductItem(
        code = code.orEmpty(),
        productName = product_name,
        brands = brands,
        imageUrl = image_url,
        nutriments = null,
        ingredientsText = ingredients_text,
        nutritionGrades = nutrition_grades,
        nutriscoreData = null,
        marketPrice = null
    )
}