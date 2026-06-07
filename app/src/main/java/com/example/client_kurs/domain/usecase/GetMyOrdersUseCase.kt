package com.example.client_kurs.domain.usecase

import com.example.client_kurs.domain.model.Order
import com.example.client_kurs.domain.repository.OrderRepository

class GetMyOrdersUseCase(private val orderRepository: OrderRepository) {
    suspend operator fun invoke(): Result<List<Order>> {
        return orderRepository.getMyOrders()
    }
}