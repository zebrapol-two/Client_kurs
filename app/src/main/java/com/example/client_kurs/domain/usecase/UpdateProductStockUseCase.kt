package com.example.client_kurs.domain.usecase

import com.example.client_kurs.domain.repository.ProductRepository

class UpdateProductStockUseCase(private val productRepository: ProductRepository) {
    suspend operator fun invoke(productId: String, delta: Int): Result<Unit> {
        return productRepository.updateStock(productId, delta)
    }
}