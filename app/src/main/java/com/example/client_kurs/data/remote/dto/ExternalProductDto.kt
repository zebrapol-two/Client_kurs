package com.example.client_kurs.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ExternalProductDto(
    val code: String? = null,
    val product_name: String? = null,
    val brands: String? = null,
    val image_url: String? = null,
    val nutriments: Map<String, JsonElement>? = null,
    val ingredients_text: String? = null,
    val nutrition_grades: String? = null,
    val nutriscore_data: Map<String, JsonElement>? = null
)
@Serializable
data class ExternalSupplierDto(
    val supplierId: String,
    val supplierName: String,
    val price: Double
)