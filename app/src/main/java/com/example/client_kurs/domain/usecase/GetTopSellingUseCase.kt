package com.example.client_kurs.domain.usecase

import com.example.client_kurs.domain.model.TopSellingProduct
import com.example.client_kurs.domain.repository.AnalyticsRepository

class GetTopSellingUseCase(
    private val analyticsRepository: AnalyticsRepository
) {
    suspend operator fun invoke(): Result<List<TopSellingProduct>> {
        return analyticsRepository.getTopSelling()
    }
}