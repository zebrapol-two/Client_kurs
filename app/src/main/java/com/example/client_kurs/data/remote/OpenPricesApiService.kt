package com.example.client_kurs.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json

class OpenPricesApiService {
    // Базовый URL API
    private val baseUrl = "https://prices.openfoodfacts.org/api/v1"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    // --- Модели ответа ---
    @Serializable
    data class SearchResponse(
        val items: List<ProductItem> = emptyList(),
        val page: Int = 1,
        val total: Int = 0
    )

    @Serializable
    data class ProductItem(
        val id: String = "",                // product code (barcode)
        val name: String? = null,           // product name
        val image_url: String? = null       // product image
    )

    @Serializable
    data class PricesResponse(
        val items: List<PriceItem> = emptyList()
    )

    @Serializable
    data class PriceItem(
        val price: Double? = null,
        val currency: String? = null,
        val store: String? = null,          // магазин
        val location: String? = null,       // город
        val date: String? = null
    )

    /**
     * Поиск продуктов по названию.
     * Возвращает список товаров (первые 20).
     */
    suspend fun searchProducts(query: String): List<ProductItem> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/products") {
                parameter("search", query)
                parameter("size", 20)
            }
            if (response.status.isSuccess()) {
                response.body<SearchResponse>().items
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Получить список цен для конкретного товара.
     * Возвращаем цены с информацией о магазине.
     */
    suspend fun getPrices(productId: String): List<PriceItem> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/products/$productId/prices")
            if (response.status.isSuccess()) {
                response.body<PricesResponse>().items
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}