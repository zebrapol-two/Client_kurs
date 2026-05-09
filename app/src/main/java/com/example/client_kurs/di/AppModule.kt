package com.example.client_kurs.di

import com.example.client_kurs.data.repository.AuthRepositoryImpl
import com.example.client_kurs.data.repository.OrderRepositoryImpl
import com.example.client_kurs.data.repository.ProductRepositoryImpl
import com.example.client_kurs.domain.repository.AuthRepository
import com.example.client_kurs.domain.repository.OrderRepository
import com.example.client_kurs.domain.repository.ProductRepository
import com.example.client_kurs.domain.usecase.CreateOrderUseCase
import com.example.client_kurs.domain.usecase.GetProductsUseCase
import com.example.client_kurs.domain.usecase.UpdateStockUseCase
import com.example.client_kurs.presentation.viewmodel.AuthViewModel
import com.example.client_kurs.presentation.viewmodel.CustomerViewModel
import com.example.client_kurs.presentation.viewmodel.StorekeeperViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Firebase
    single { FirebaseAuth.getInstance() }

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<ProductRepository> { ProductRepositoryImpl() }
    single<OrderRepository> { OrderRepositoryImpl() }

<<<<<<< HEAD
    // UseCases
=======
<<<<<<< HEAD
    // Room Database
    single { AppDatabase.getInstance(androidContext()) }
    single { get<AppDatabase>().orderDao() }
=======
    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<ProductRepository> { ProductRepositoryImpl(get()) }
    single<OrderRepository> { OrderRepositoryImpl() }
    single<InventoryRepository> { InventoryRepositoryImpl() }
    single<SupplierRepository> { SupplierRepositoryImpl() }
    single<AnalyticsRepository> { AnalyticsRepositoryImpl() }
>>>>>>> 6d7f8b3 (Исправление логики отправки запросов на получение имеющихся товаров на складе)

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
>>>>>>> 8119b08 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
    factory { GetProductsUseCase(get()) }
    factory { CreateOrderUseCase(get()) }
    factory { UpdateStockUseCase(get()) }

    // ViewModels
    viewModel { AuthViewModel(get()) }
    viewModel { CustomerViewModel(get(), get()) }
    viewModel { StorekeeperViewModel(get(), get(), get()) }
}

