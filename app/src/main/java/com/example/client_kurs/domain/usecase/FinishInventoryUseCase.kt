package com.example.client_kurs.domain.usecase

import com.example.client_kurs.domain.model.InventoryAdjustment
import com.example.client_kurs.domain.repository.InventoryRepository

class FinishInventoryUseCase(private val inventoryRepository: InventoryRepository) {
    suspend operator fun invoke(adjustments: List<InventoryAdjustment>): Result<Unit> {
        return inventoryRepository.finishInventory(adjustments)
    }
}