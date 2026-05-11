package com.example.client_kurs.data.repository

import com.example.client_kurs.data.remote.dto.PurchaseCreateRequest
import com.example.client_kurs.data.remote.dto.SupplierDto
import com.example.client_kurs.data.remote.dto.toDomain
import com.example.client_kurs.data.remote.getAuthToken
import com.example.client_kurs.data.remote.ktorClient
import com.example.client_kurs.domain.model.SupplierOffer
import com.example.client_kurs.domain.repository.SupplierRepository
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class SupplierRepositoryImpl : SupplierRepository {
    override suspend fun getSuppliers(productId: String): Result<List<SupplierOffer>> {
        return try {
            val suppliers = ktorClient.get("/api/suppliers/$productId")
                .body<List<SupplierDto>>()
            Result.success(suppliers.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPurchase(
        productId: String,
        supplierId: Int,
        quantity: Int
    ): Result<Unit> {
        return try {
            ktorClient.post("/api/purchase/create") {
                setBody(PurchaseCreateRequest(productId, supplierId, quantity))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
