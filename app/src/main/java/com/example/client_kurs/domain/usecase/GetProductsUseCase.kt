package com.example.client_kurs.domain.usecase

import com.example.client_kurs.domain.model.Product
import com.example.client_kurs.domain.repository.ProductRepository

class GetProductsUseCase(private val productRepository: ProductRepository) {
    suspend operator fun invoke(): Result<List<Product>> {
        return productRepository.getAllProducts()
    }
}

