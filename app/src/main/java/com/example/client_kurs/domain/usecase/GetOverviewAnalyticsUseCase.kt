package com.example.client_kurs.domain.usecase

import com.example.client_kurs.domain.model.AnalyticsOverview
import com.example.client_kurs.domain.repository.AnalyticsRepository

class GetOverviewAnalyticsUseCase(
    private val analyticsRepository: AnalyticsRepository
) {
    suspend operator fun invoke(): Result<AnalyticsOverview> {
        return analyticsRepository.getOverviewAnalytics()
    }
}