package com.example.client_kurs.domain.usecase

import com.example.client_kurs.domain.model.Product
import com.example.client_kurs.domain.repository.ProductRepository

class AddProductUseCase(private val productRepository: ProductRepository) {
    suspend operator fun invoke(
        productId: String,
        name: String,
        price: Double,
        quantity: Int
    ): Result<Product> {
        return productRepository.addProduct(
            productId = productId,
            name = name,
            price = price,
            quantity = quantity
        )
    }
}