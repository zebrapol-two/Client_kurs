package com.example.client_kurs.domain.usecase

import com.example.client_kurs.domain.model.InventoryItem
import com.example.client_kurs.domain.repository.InventoryRepository

class GetLowInventoryUseCase(private val inventoryRepository: InventoryRepository) {
    suspend operator fun invoke(): Result<List<InventoryItem>> {
        return inventoryRepository.getLowInventory()
    }
}