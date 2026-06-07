package com.example.client_kurs.domain.usecase

import com.example.client_kurs.domain.model.RecommendedOrder
import com.example.client_kurs.domain.repository.AnalyticsRepository

class GetRecommendedOrderUseCase(
    private val analyticsRepository: AnalyticsRepository
) {
    suspend operator fun invoke(): Result<List<RecommendedOrder>> {
        return analyticsRepository.getRecommendedOrder()
    }
}