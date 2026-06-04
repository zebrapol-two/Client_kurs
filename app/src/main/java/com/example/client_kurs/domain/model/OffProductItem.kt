package com.example.client_kurs.domain.model

data class OffProductItem(
    val code: String,        // штрихкод
    val name: String,
    val imageUrl: String? = null
)