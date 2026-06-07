package com.example.client_kurs.domain.repository

import com.example.client_kurs.domain.model.SupplierOffer

interface SupplierRepository {
    suspend fun getSuppliers(productId: String): Result<List<SupplierOffer>>
    suspend fun createPurchase(productId: String, supplierId: String, quantity: Int): Result<Unit>
}