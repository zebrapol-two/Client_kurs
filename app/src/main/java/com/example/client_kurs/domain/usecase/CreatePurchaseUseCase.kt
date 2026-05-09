package com.example.client_kurs.domain.usecase

import com.example.client_kurs.domain.repository.SupplierRepository

class CreatePurchaseUseCase(private val supplierRepository: SupplierRepository) {
<<<<<<< HEAD
    suspend operator fun invoke(productId: String, supplierId: String, quantity: Int): Result<Unit> {
=======
    suspend operator fun invoke(productId: String, supplierId: Int, quantity: Int): Result<Unit> {
>>>>>>> 6d7f8b3 (Исправление логики отправки запросов на получение имеющихся товаров на складе)
        return supplierRepository.createPurchase(productId, supplierId, quantity)
    }
}