package com.example.client_kurs.data.repository

import com.example.client_kurs.auth.FirebaseAuthManager
import com.example.client_kurs.data.remote.dto.PurchaseCreateRequest
import com.example.client_kurs.data.remote.dto.SupplierDto
import com.example.client_kurs.data.remote.dto.toDomain
import com.example.client_kurs.data.remote.ktorClient
import com.example.client_kurs.domain.model.SupplierOffer
import com.example.client_kurs.domain.repository.SupplierRepository
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.get
import io.ktor.http.isSuccess

class SupplierRepositoryImpl(
    private val authManager: FirebaseAuthManager
) : SupplierRepository {

    private suspend fun getToken(): String? = authManager.getIdToken()

    override suspend fun getSuppliers(productId: String): Result<List<SupplierOffer>> {
        return try {
            val token = getToken() ?: return Result.failure(Exception("Нет токена авторизации"))
            val response = ktorClient.get("/api/suppliers/$productId") {
                header("Authorization", "Bearer $token")
            }
            if (!response.status.isSuccess()) {
                // Попытаемся прочитать сообщение об ошибке от сервера
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
            val token = getToken() ?: return Result.failure(Exception("Нет токена авторизации"))
            ktorClient.post("/api/purchase/create") {
                header("Authorization", "Bearer $token")
                setBody(PurchaseCreateRequest(productId, supplierId, quantity))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}