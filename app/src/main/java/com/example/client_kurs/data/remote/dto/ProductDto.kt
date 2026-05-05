package com.example.client_kurs.data.remote.dto

import com.example.client_kurs.domain.model.Product
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Int? = null,
    val name: String,
    val price: Double,
    val quantity: Int
) {
    fun toDomain() = Product(
        id = id ?: 0,
        name = name,
        price = price,
        quantity = quantity
    )
}

fun Product.toDto() = ProductDto(
    id = if (id == 0) null else id,
    name = name,
    price = price,
    quantity = quantity
)

