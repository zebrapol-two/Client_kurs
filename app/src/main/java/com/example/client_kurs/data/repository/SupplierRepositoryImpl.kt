package com.example.client_kurs.data.repository

import com.example.client_kurs.data.remote.dto.PurchaseCreateRequest
import com.example.client_kurs.data.remote.dto.SupplierDto
import com.example.client_kurs.data.remote.dto.toDomain
import com.example.client_kurs.domain.model.SupplierOffer
import com.example.client_kurs.domain.repository.SupplierRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class SupplierRepositoryImpl(
    private val httpClient: HttpClient
) : SupplierRepository {

    override suspend fun getSuppliers(productId: String): Result<List<SupplierOffer>> {
        return try {
            val response = httpClient.get("/api/suppliers/$productId")
            if (!response.status.isSuccess()) {
                val errorBody = try {
                    response.body<Map<String, String>>()
                } catch (e: Exception) {
                    mapOf("error" to "Ошибка сервера (${response.status.value})")
                }
                val message = errorBody["error"] ?: "Неизвестная ошибка"
                return Result.failure(Exception(message))
            }
            val suppliers = response.body<List<SupplierDto>>()
            Result.success(suppliers.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPurchase(
        productId: String,
        supplierId: String,
        quantity: Int
    ): Result<Unit> {
        return try {
            val response = httpClient.post("/api/purchase/create") {
                contentType(ContentType.Application.Json)
                setBody(PurchaseCreateRequest(productId, supplierId, quantity))
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val errorBody = try {
                    response.body<Map<String, String>>()
                } catch (e: Exception) {
                    emptyMap()
                }
                val errorMsg = errorBody["error"] ?: "Ошибка сервера: ${response.status.value}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}