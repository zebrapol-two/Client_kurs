package com.example.client_kurs.data.remote.dto

import com.example.client_kurs.domain.model.SupplierOffer
import kotlinx.serialization.Serializable

@Serializable
data class SupplierDto(
    val supplierId: String,
    val supplierName: String,
    val price: Double
)

fun SupplierDto.toDomain() = SupplierOffer(
    supplierId = supplierId,
    supplierName = supplierName,
    price = price
)