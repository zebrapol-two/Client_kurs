package com.example.client_kurs.data.mappers

import com.example.client_kurs.data.remote.dto.OrderItemDto
import com.example.client_kurs.domain.model.OrderItem

/** Преобразует [OrderItemDto] (ответ API) в доменную модель [OrderItem]. */
fun OrderItemDto.toDomainOrderItem() = OrderItem(
    productId = productId,
    quantity = quantity,

)

/** Преобразует доменную модель [OrderItem] в [OrderItemDto] для отправки на сервер. */
fun OrderItem.toOrderItemDto() = OrderItemDto(
    productId = productId,
    quantity = quantity
)
