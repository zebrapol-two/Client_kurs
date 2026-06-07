package com.example.client_kurs.di

import android.content.Context
import com.example.client_kurs.auth.FirebaseAuthManager
import com.example.client_kurs.data.local.AppDatabase
import com.example.client_kurs.data.local.OrderDao
import com.example.client_kurs.data.local.UserPreferencesManager
import com.example.client_kurs.data.remote.KtorClientFactory
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
import com.example.client_kurs.domain.usecase.*
import com.example.client_kurs.presentation.viewmodel.*
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
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    // Firebase
    single { FirebaseAuth.getInstance() }
    single { FirebaseAuthManager(get()) }

    // Локальное хранилище
    single { UserPreferencesManager(get()) }

    // Room Database
    single { AppDatabase.getInstance(androidContext()) }
    single { get<AppDatabase>().orderDao() }

    // Неавторизованный HTTP клиент
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

    single { RefreshTokenApi(get(named("unauthorized"))) }

    // Авторизованный HTTP клиент
    single { KtorClientFactory(get(), get()).create() }

    // API сервис
    single<KtorApiService> { KtorApiServiceImpl(get()) }

    // Репозитории
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get(named("unauthorized")), get(), get()) }
    single<ProductRepository> { ProductRepositoryImpl(get()) }
    single<OrderRepository> { OrderRepositoryImpl(get(), get()) }  // передаём database
    single<InventoryRepository> { InventoryRepositoryImpl(get()) }
    single<SupplierRepository> { SupplierRepositoryImpl(get()) }
    single<OrderRepositoryImpl> { OrderRepositoryImpl(get(), get()) }
    single<AnalyticsRepository> { AnalyticsRepositoryImpl(get()) }

    // UseCase
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

    // ViewModel
    viewModel { AuthViewModel(get()) }
    viewModel { CustomerViewModel(get(), get(), get(), get()) }
    viewModel { StorekeeperViewModel(get(), get(), get(), get()) }
    viewModel { InventoryViewModel(get(), get()) }
    viewModel { SupplierComparisonViewModel(get(), get(), get(), get<KtorApiService>()) }
    viewModel { AnalyticsViewModel(get(), get(), get()) }
    viewModel { RecommendOrderViewModel(get(), get()) }
    viewModel { PendingDeliveriesViewModel(get()) }
}