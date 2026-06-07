package com.example.client_kurs.domain.model

data class DailySales(
    val day: String,
    val soldUnits: Int
)

data class AnalyticsOverview(
    val totalStockValue: Double,
    val lowStockCount: Int,
    val salesByDay: List<DailySales>
)

data class TopSellingProduct(
    val productId: String,
    val productName: String,
    val soldUnits: Int
)

data class RecommendedOrder(
    val productId: String,
    val productName: String,
    val currentStock: Int,
    val minimumStock: Int,
    val averageSalesPerDay: Double,
    val deliveryDays: Int,
    val recommendedOrder: Int
)