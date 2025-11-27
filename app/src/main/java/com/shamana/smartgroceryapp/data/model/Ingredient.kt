package com.shamana.smartgroceryapp.data.model

data class Ingredient(
    val name: String,
    val quantity: Double,
    val unit: String,
    val confidence: Float = 0f
)

