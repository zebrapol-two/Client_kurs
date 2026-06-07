package com.example.client_kurs.domain.model

data class OffProductItem(
    val code: String,                    // штрихкод
    val productName: String? = null,
    val brands: String? = null,
    val imageUrl: String? = null,
    val nutriments: Nutriments? = null,
    val ingredientsText: String? = null,
    val nutritionGrades: String? = null,
    val nutriscoreData: NutriscoreData? = null,
    val marketPrice: Double? = null
)

data class Nutriments(
    val energyKcal100g: Double? = null,
    val fat100g: Double? = null,
    val saturatedFat100g: Double? = null,
    val carbohydrates100g: Double? = null,
    val sugars100g: Double? = null,
    val proteins100g: Double? = null,
    val salt100g: Double? = null
)

// Модель для компонентов Nutri-Score (отрицательные и положительные факторы)
data class NutriscoreComponent(
    val id: String,
    val points: Int,
    val points_max: Int,
    val unit: String,
    val value: Double?
)

// Основная модель Nutri-Score
data class NutriscoreData(
    val grade: String,
    val score: Int,
    val negative_points: Int,
    val positive_points: Int,
    val components: Map<String, List<NutriscoreComponent>>? = null
)