package com.example.client_kurs.di

import com.example.client_kurs.auth.FirebaseAuthManager
import com.example.client_kurs.data.local.UserPreferencesManager
import com.example.client_kurs.data.remote.KtorClientFactory
import com.example.client_kurs.data.remote.OpenPricesApiService
import com.example.client_kurs.data.remote.RefreshTokenApi
import com.example.client_kurs.data.remote.api.KtorApiService
import com.example.client_kurs.data.remote.api.KtorApiServiceImpl
import com.example.client_kurs.data.repository.AuthRepositoryImpl
import com.example.client_kurs.data.repository.AnalyticsRepositoryImpl
import com.example.client_kurs.data.repository.InventoryRepositoryImpl
import com.example.client_kurs.data.repository.OrderRepositoryImpl
import com.example.client_kurs.data.repository.ProductRepositoryImpl
import com.example.client_kurs.data.repository.SupplierRepositoryImpl
import com.example.client_kurs.domain.repository.AnalyticsRepository
import com.example.client_kurs.domain.repository.AuthRepository
import com.example.client_kurs.domain.repository.InventoryRepository
import com.example.client_kurs.domain.repository.OrderRepository
import com.example.client_kurs.domain.repository.ProductRepository
import com.example.client_kurs.domain.repository.SupplierRepository
import com.example.client_kurs.domain.usecase.AddProductUseCase
import com.example.client_kurs.domain.usecase.CreateOrderUseCase
import com.example.client_kurs.domain.usecase.CreatePurchaseUseCase
import com.example.client_kurs.domain.usecase.FinishInventoryUseCase
import com.example.client_kurs.domain.usecase.GetLowInventoryUseCase
import com.example.client_kurs.domain.usecase.GetMyOrdersUseCase
import com.example.client_kurs.domain.usecase.GetOverviewAnalyticsUseCase
import com.example.client_kurs.domain.usecase.GetProductsUseCase
import com.example.client_kurs.domain.usecase.GetRecommendedOrderUseCase
import com.example.client_kurs.domain.usecase.GetSuppliersUseCase
import com.example.client_kurs.domain.usecase.GetTopSellingUseCase
import com.example.client_kurs.domain.usecase.UpdateProductStockUseCase
import com.example.client_kurs.domain.usecase.UpdateStockUseCase
import com.example.client_kurs.presentation.viewmodel.AnalyticsViewModel
import com.example.client_kurs.presentation.viewmodel.AuthViewModel
import com.example.client_kurs.presentation.viewmodel.CustomerViewModel
import com.example.client_kurs.presentation.viewmodel.InventoryViewModel
import com.example.client_kurs.presentation.viewmodel.PendingDeliveriesViewModel
import com.example.client_kurs.presentation.viewmodel.RecommendOrderViewModel
import com.example.client_kurs.presentation.viewmodel.StorekeeperViewModel
import com.example.client_kurs.presentation.viewmodel.SupplierComparisonViewModel
import com.example.client_kurs.utils.ServerConfig
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    // Firebase
    single { FirebaseAuth.getInstance() }
    single { FirebaseAuthManager(get()) }

    // Локальное хранилище
    single { UserPreferencesManager(get()) }

    // ---- Неавторизованный HTTP клиент (для login, register, refresh) ----
    single(named("unauthorized")) {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            defaultRequest {
                url(ServerConfig.BASE_URL)
                contentType(ContentType.Application.Json)
            }
        }
    }

    // ---- RefreshTokenApi использует неавторизованный клиент ----
    single { RefreshTokenApi(get(named("unauthorized"))) }

    // ---- Репозиторий аутентификации (использует неавторизованный клиент + RefreshTokenApi) ----
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get(named("unauthorized")), get()) }

    // ---- Фабрика для авторизованного клиента (зависит от UserPreferencesManager и AuthRepository) ----
    single { KtorClientFactory(get(), get()) }

    // ---- Авторизованный HTTP клиент для всех защищённых запросов ----
    single<HttpClient>(named("authorized")) { get<KtorClientFactory>().create() }

    // ---- API сервис (использует авторизованный клиент) ----
    single<KtorApiService> { KtorApiServiceImpl(get(named("authorized"))) }

    // ---- Внешний сервис OpenPrices ----
    single { OpenPricesApiService() }

    // ---- Репозитории (все используют авторизованный клиент) ----
    // ВНИМАНИЕ: конструкторы этих репозиториев должны принимать HttpClient,
    // а не FirebaseAuthManager (это уже исправлено в коде репозиториев)
    single<ProductRepository> { ProductRepositoryImpl(get(named("authorized"))) }
    single<OrderRepository> { OrderRepositoryImpl(get(named("authorized"))) }
    single<InventoryRepository> { InventoryRepositoryImpl(get(named("authorized"))) }
    single<SupplierRepository> { SupplierRepositoryImpl(get(named("authorized"))) }
    single<AnalyticsRepository> { AnalyticsRepositoryImpl(get(named("authorized"))) }

    // ---- UseCase (без изменений) ----
    factory { GetProductsUseCase(get()) }
    factory { CreateOrderUseCase(get()) }
    factory { GetMyOrdersUseCase(get()) }
    factory { UpdateStockUseCase(get()) }
    factory { UpdateProductStockUseCase(get()) }
    factory { AddProductUseCase(get()) }
    factory { GetLowInventoryUseCase(get()) }
    factory { FinishInventoryUseCase(get()) }
    factory { GetSuppliersUseCase(get()) }
    factory { CreatePurchaseUseCase(get()) }
    factory { GetOverviewAnalyticsUseCase(get()) }
    factory { GetTopSellingUseCase(get()) }
    factory { GetRecommendedOrderUseCase(get()) }

    // ---- ViewModel ----
    viewModel { AuthViewModel(get()) }
    viewModel { CustomerViewModel(get(), get(), get()) }
    viewModel { StorekeeperViewModel(get(), get(), get(), get()) }
    viewModel { InventoryViewModel(get(), get()) }
    viewModel { SupplierComparisonViewModel(get(), get(), get(), get()) }
    viewModel { AnalyticsViewModel(get(), get(), get()) }
    viewModel { RecommendOrderViewModel(get(), get()) }
    viewModel { PendingDeliveriesViewModel(get()) }
}