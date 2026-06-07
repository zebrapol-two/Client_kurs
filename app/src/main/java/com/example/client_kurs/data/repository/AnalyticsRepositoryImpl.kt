package com.example.client_kurs.data.repository

import com.example.client_kurs.data.remote.dto.DailySalesDto
import com.example.client_kurs.data.remote.dto.OverviewAnalyticsDto
import com.example.client_kurs.data.remote.dto.RecommendedOrderDto
import com.example.client_kurs.data.remote.dto.TopSellingDto
import com.example.client_kurs.data.remote.dto.toDomain
import com.example.client_kurs.domain.model.AnalyticsOverview
import com.example.client_kurs.domain.model.DailySales
import com.example.client_kurs.domain.model.RecommendedOrder
import com.example.client_kurs.domain.model.TopSellingProduct
import com.example.client_kurs.domain.repository.AnalyticsRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class AnalyticsRepositoryImpl(
    private val httpClient: HttpClient
) : AnalyticsRepository {

    override suspend fun getOverviewAnalytics(): Result<AnalyticsOverview> {
        return try {
            val response = httpClient.get("/api/analytics/overview").body<OverviewAnalyticsDto>()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTopSelling(): Result<List<TopSellingProduct>> {
        return try {
            val response = httpClient.get("/api/analytics/top-selling").body<List<TopSellingDto>>()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProductSales(productId: String): Result<List<DailySales>> {
        return try {
            val response = httpClient.get("/api/analytics/product-sales/$productId").body<List<DailySalesDto>>()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecommendedOrder(): Result<List<RecommendedOrder>> {
        return try {
            val response = httpClient.get("/api/analytics/recommend-order").body<List<RecommendedOrderDto>>()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}