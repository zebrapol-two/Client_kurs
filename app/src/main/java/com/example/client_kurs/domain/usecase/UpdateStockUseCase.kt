package com.example.client_kurs.domain.usecase

import com.example.client_kurs.domain.repository.ProductRepository

class UpdateStockUseCase(private val productRepository: ProductRepository) {
    suspend operator fun invoke(productId: Int, newQuantity: Int): Result<Unit> {
        return productRepository.updateQuantity(productId, newQuantity)
    }
}

