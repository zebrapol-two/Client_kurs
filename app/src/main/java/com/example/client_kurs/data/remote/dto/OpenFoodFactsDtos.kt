package com.example.client_kurs.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenFoodFactsSearchResponseDto(
    val products: List<OpenFoodFactsProductDto> = emptyList()
)

@Serializable
data class OpenFoodFactsProductResponseDto(
    val product: OpenFoodFactsProductDto? = null
)

@Serializable
data class OpenFoodFactsProductDto(
    val code: String = "",
    @SerialName("product_name") val productName: String? = null,
    val brands: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val nutriments: OpenFoodFactsNutrimentsDto? = null,
    @SerialName("ingredients_text") val ingredientsText: String? = null,
    @SerialName("nutrition_grades") val nutritionGrades: String? = null,
    @SerialName("nutriscore_data") val nutriscoreData: OpenFoodFactsNutriscoreDataDto? = null
    ,
    // Цена на рынке, возвращается сервером при поиске
    @SerialName("marketPrice") val marketPrice: Double? = null
)

@Serializable
data class OpenFoodFactsNutrimentsDto(
    @SerialName("energy-kcal_100g") val energyKcal100g: Double? = null,
    @SerialName("fat_100g") val fat100g: Double? = null,
    @SerialName("saturated-fat_100g") val saturatedFat100g: Double? = null,
    @SerialName("carbohydrates_100g") val carbohydrates100g: Double? = null,
    @SerialName("sugars_100g") val sugars100g: Double? = null,
    @SerialName("proteins_100g") val proteins100g: Double? = null,
    @SerialName("salt_100g") val salt100g: Double? = null
)

@Serializable
data class OpenFoodFactsNutriscoreComponentDto(
    val id: String,
    val points: Int,
    @SerialName("points_max") val pointsMax: Int,
    val unit: String,
    val value: Double? = null
)

@Serializable
data class OpenFoodFactsNutriscoreDataDto(
    val grade: String? = null,
    val score: Int? = null,
    @SerialName("negative_points") val negativePoints: Int? = null,
    @SerialName("positive_points") val positivePoints: Int? = null,
    val components: Map<String, List<OpenFoodFactsNutriscoreComponentDto>>? = null
)


