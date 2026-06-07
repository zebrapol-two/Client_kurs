package com.example.client_kurs.domain.usecase

import com.example.client_kurs.domain.repository.SupplierRepository

class CreatePurchaseUseCase(private val supplierRepository: SupplierRepository) {
    suspend operator fun invoke(productId: String, supplierId: String, quantity: Int): Result<Unit> {
        return supplierRepository.createPurchase(productId, supplierId, quantity)
    }
}