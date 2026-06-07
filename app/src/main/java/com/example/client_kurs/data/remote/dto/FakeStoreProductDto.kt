package com.example.client_kurs.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FakeStoreProductDto(
    val code: String,           // id из API (как строка)
    val productName: String?,   // title
    val brands: String?,        // category (используется как бренд)
    val imageUrl: String?,      // image
    val marketPrice: Double     // price из API (реальная цена)
)