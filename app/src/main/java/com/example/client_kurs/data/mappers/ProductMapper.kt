package com.example.client_kurs.data.mappers

import com.example.client_kurs.data.remote.dto.ProductDto
import com.example.client_kurs.domain.model.Product

/** Преобразует [ProductDto] (ответ API) в доменную модель [Product]. */
fun ProductDto.toDomainProduct() = Product(
    id = id ?: throw IllegalArgumentException("Product id must not be null"),
    name = name,
    price = price.toDoubleOrNull() ?: 0.0,
    quantity = quantity
)

/** Преобразует доменную модель [Product] в [ProductDto] для отправки на сервер. */
fun Product.toProductDto() = ProductDto(
    id = id,
    name = name,
    price = price.toString(),
    quantity = quantity
)