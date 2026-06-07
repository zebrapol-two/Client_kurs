package com.example.client_kurs.domain.repository

import com.example.client_kurs.domain.model.AnalyticsOverview
import com.example.client_kurs.domain.model.DailySales
import com.example.client_kurs.domain.model.RecommendedOrder
import com.example.client_kurs.domain.model.TopSellingProduct

interface AnalyticsRepository {
    suspend fun getOverviewAnalytics(): Result<AnalyticsOverview>
    suspend fun getTopSelling(): Result<List<TopSellingProduct>>
    suspend fun getProductSales(productId: String): Result<List<DailySales>>

    suspend fun getRecommendedOrder(): Result<List<RecommendedOrder>>
}