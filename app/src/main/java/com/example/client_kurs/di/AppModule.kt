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

    // UseCases
    factory { GetProductsUseCase(get()) }
    factory { CreateOrderUseCase(get()) }
    factory { UpdateStockUseCase(get()) }

    // ViewModels
    viewModel { AuthViewModel(get()) }
    viewModel { CustomerViewModel(get(), get()) }
    viewModel { StorekeeperViewModel(get(), get(), get()) }
}

