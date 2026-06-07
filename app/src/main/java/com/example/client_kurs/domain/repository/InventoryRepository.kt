package com.example.client_kurs.domain.repository

import com.example.client_kurs.domain.model.InventoryAdjustment
import com.example.client_kurs.domain.model.InventoryItem

interface InventoryRepository {
    suspend fun getLowInventory(): Result<List<InventoryItem>>
    suspend fun finishInventory(adjustments: List<InventoryAdjustment>): Result<Unit>
}