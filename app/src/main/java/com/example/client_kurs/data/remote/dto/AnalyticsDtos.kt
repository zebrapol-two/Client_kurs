package com.example.client_kurs.data.remote.dto

import com.example.client_kurs.domain.model.AnalyticsOverview
import com.example.client_kurs.domain.model.DailySales
import com.example.client_kurs.domain.model.RecommendedOrder
import com.example.client_kurs.domain.model.TopSellingProduct
import kotlinx.serialization.Serializable

@Serializable
data class DailySalesDto(
    val day: String,
    val soldUnits: Int
)

@Serializable
data class OverviewAnalyticsDto(
    val totalStockValue: Double,
    val lowStockCount: Int,
    val salesByDay: List<DailySalesDto>
)

@Serializable
data class TopSellingDto(
    val productId: String,
    val productName: String,
    val soldUnits: Int
)

@Serializable
data class RecommendedOrderDto(
    val productId: String,
    val productName: String,
    val currentStock: Int,
    val minimumStock: Int,
    val averageSalesPerDay: Double,
    val deliveryDays: Int,
    val recommendedOrder: Int
)

fun OverviewAnalyticsDto.toDomain() = AnalyticsOverview(
    totalStockValue = totalStockValue,
    lowStockCount = lowStockCount,
    salesByDay = salesByDay.map { it.toDomain() }
)

fun DailySalesDto.toDomain() = DailySales(
    day = day,
    soldUnits = soldUnits
)

fun TopSellingDto.toDomain() = TopSellingProduct(
    productId = productId,
    productName = productName,
    soldUnits = soldUnits
)

fun RecommendedOrderDto.toDomain() = RecommendedOrder(
    productId = productId.toString(),
    productName = productName,
    currentStock = currentStock,
    minimumStock = minimumStock,
    averageSalesPerDay = averageSalesPerDay,
    deliveryDays = deliveryDays,
    recommendedOrder = recommendedOrder
)