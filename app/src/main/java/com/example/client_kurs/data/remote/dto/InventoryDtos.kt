package com.example.client_kurs.data.remote.dto

import com.example.client_kurs.domain.model.InventoryAdjustment
import kotlinx.serialization.Serializable

@Serializable
data class InventoryAdjustmentDto(
    val productId: String,
    val expectedQuantity: Int,
    val actualQuantity: Int,
    val difference: Int
)

@Serializable
data class CompleteInventoryRequest(
    val adjustments: List<InventoryAdjustmentDto>
)

fun InventoryAdjustment.toDto() = InventoryAdjustmentDto(
    productId = productId,
    expectedQuantity = expectedQuantity,
    actualQuantity = actualQuantity,
    difference = quantityDifference
)