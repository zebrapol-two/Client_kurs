package com.example.client_kurs.domain.usecase

import com.example.client_kurs.domain.model.OrderItem
import com.example.client_kurs.domain.repository.OrderRepository

class CreateOrderUseCase(private val orderRepository: OrderRepository) {
    suspend operator fun invoke(items: List<OrderItem>): Result<Unit> {
        return orderRepository.createOrder(items)
    }
}

