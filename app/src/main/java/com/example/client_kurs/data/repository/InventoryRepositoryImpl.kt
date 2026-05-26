package com.example.client_kurs.data.repository

import com.example.client_kurs.data.remote.dto.CompleteInventoryRequest
import com.example.client_kurs.data.remote.dto.ProductDto
import com.example.client_kurs.data.remote.dto.toDto
import com.example.client_kurs.data.remote.getAuthToken
import com.example.client_kurs.data.remote.ktorClient
import com.example.client_kurs.domain.model.InventoryAdjustment
import com.example.client_kurs.domain.model.InventoryItem
import com.example.client_kurs.domain.repository.InventoryRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class InventoryRepositoryImpl : InventoryRepository {
    override suspend fun getLowInventory(): Result<List<InventoryItem>> {
        return try {
            val token = getAuthToken()
            val products = ktorClient.get("/api/inventory/low") {
                header("Authorization", "Bearer $token")
            }.body<List<ProductDto>>()
            Result.success(products.map { InventoryItem(it.toDomain(), it.quantity, it.quantity) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun finishInventory(adjustments: List<InventoryAdjustment>): Result<Unit> {
        return try {
            val token = getAuthToken()
            ktorClient.post("/inventory/complete") {
                header("Authorization", "Bearer $token")
                setBody(
                    CompleteInventoryRequest(
                        adjustments = adjustments.map { it.toDto() }
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
