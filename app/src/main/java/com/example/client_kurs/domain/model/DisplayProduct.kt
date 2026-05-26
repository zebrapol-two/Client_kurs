package com.example.client_kurs.domain.model

/**
 * Универсальный товар для отображения на экране сравнения поставщиков.
 * Может представлять как локальный товар со склада, так и результат поиска из OFF.
 */
data class DisplayProduct(
    val id: String,            // штрихкод или id товара
    val name: String,
    val price: Double?,        // цена, если локальный товар (может быть null для OFF)
    val isLocal: Boolean       // true = со склада, false = из OFF
)