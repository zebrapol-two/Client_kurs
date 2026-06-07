package com.example.client_kurs.data.remote.dto

import com.example.client_kurs.domain.model.Product
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: String,
    val name: String,
    val price: String,
    val quantity: Int
) {
    fun toDomain() = Product(
        id = id,
        name = name,
        price = price.toDoubleOrNull() ?: 0.0,
        quantity = quantity
    )
}

fun Product.toDto() = ProductDto(
    id = id,
    name = name,
    price = price.toString(),
    quantity = quantity
)
