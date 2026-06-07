package com.example.client_kurs.domain.usecase

import com.example.client_kurs.domain.model.SupplierOffer
import com.example.client_kurs.domain.repository.SupplierRepository

class GetSuppliersUseCase(private val supplierRepository: SupplierRepository) {
    suspend operator fun invoke(productId: String): Result<List<SupplierOffer>> {
        return supplierRepository.getSuppliers(productId)
    }
}