package com.example.client_kurs.di

import com.example.client_kurs.auth.FirebaseAuthManager
import com.example.client_kurs.data.local.UserPreferencesManager
import com.example.client_kurs.data.remote.KtorClientFactory
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
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.HttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Firebase
    single { FirebaseAuth.getInstance() }
    single { FirebaseAuthManager(get()) }

    // Ktor HttpClient (через фабрику с автоматическим Bearer-токеном)
    single<HttpClient> { KtorClientFactory.create() }

    // API Service
    single<KtorApiService> { KtorApiServiceImpl(get()) }

    // Local storage
    single { UserPreferencesManager(get()) }

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<ProductRepository> { ProductRepositoryImpl(get()) }
    single<OrderRepository> { OrderRepositoryImpl() }
    single<InventoryRepository> { InventoryRepositoryImpl() }
    single<SupplierRepository> { SupplierRepositoryImpl(get()) }
    single<AnalyticsRepository> { AnalyticsRepositoryImpl() }

    // UseCases
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

    // ViewModels
    viewModel { AuthViewModel(get()) }
    viewModel { CustomerViewModel(get(), get(), get()) }
    viewModel { StorekeeperViewModel(get(), get(), get(), get()) }
    viewModel { InventoryViewModel(get(), get()) }
    viewModel { SupplierComparisonViewModel(get(), get(), get()) }
    viewModel { AnalyticsViewModel(get(), get()) }
    viewModel { RecommendOrderViewModel(get(), get()) }
    viewModel { PendingDeliveriesViewModel(get()) }
}
