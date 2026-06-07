package com.example.client_kurs.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExternalSupplierDto(
    val supplierId: String,
    val supplierName: String,
    val price: Double
)