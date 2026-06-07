package com.example.client_kurs.domain.usecase

import com.example.client_kurs.domain.model.OrderItem
import com.example.client_kurs.domain.model.Product
import com.example.client_kurs.domain.repository.OrderRepository

class CreateOrderUseCase(private val orderRepository: OrderRepository) {
    suspend operator fun invoke(cart: Map<Product, Int>): Result<Unit> {
        val items = cart.map { (product, quantity) -> OrderItem(product.id, quantity) }
        return orderRepository.createOrder(items)
    }
}
