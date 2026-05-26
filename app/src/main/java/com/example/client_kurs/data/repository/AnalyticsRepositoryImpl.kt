package com.example.client_kurs.data.repository

import com.example.client_kurs.auth.FirebaseAuthManager
import com.example.client_kurs.data.remote.dto.OverviewAnalyticsDto
import com.example.client_kurs.data.remote.dto.RecommendedOrderDto
import com.example.client_kurs.data.remote.dto.TopSellingDto
import com.example.client_kurs.data.remote.dto.toDomain
import com.example.client_kurs.data.remote.ktorClient
import com.example.client_kurs.domain.model.AnalyticsOverview
import com.example.client_kurs.domain.model.DailySales
import com.example.client_kurs.domain.model.RecommendedOrder
import com.example.client_kurs.domain.model.TopSellingProduct
import com.example.client_kurs.domain.repository.AnalyticsRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header

// Предполагаем, что есть data class DailySalesDto (см. ниже)
import com.example.client_kurs.data.remote.dto.DailySalesDto

class AnalyticsRepositoryImpl(
    private val authManager: FirebaseAuthManager
) : AnalyticsRepository {

    private suspend fun getToken(): String? = authManager.getIdToken()

    override suspend fun getOverviewAnalytics(): Result<AnalyticsOverview> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("Нет токена"))
            val response = ktorClient.get("/api/analytics/overview") {
                header("Authorization", "Bearer $token")
            }.body<OverviewAnalyticsDto>()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTopSelling(): Result<List<TopSellingProduct>> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("Нет токена"))
            val response = ktorClient.get("/api/analytics/top-selling") {
                header("Authorization", "Bearer $token")
            }.body<List<TopSellingDto>>()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProductSales(productId: String): Result<List<DailySales>> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("Нет токена"))
            val response = ktorClient.get("/api/analytics/product-sales/$productId") {
                header("Authorization", "Bearer $token")
            }.body<List<DailySalesDto>>()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecommendedOrder(): Result<List<RecommendedOrder>> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("Нет токена"))
            val response = ktorClient.get("/api/analytics/recommend-order") {
                header("Authorization", "Bearer $token")
            }.body<List<RecommendedOrderDto>>()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}