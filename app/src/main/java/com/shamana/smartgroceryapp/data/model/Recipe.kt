package com.shamana.smartgroceryapp.data.model

data class Recipe(
    val title: String,
    val steps: List<String>,
    val cuisine: String = "Unknown"
)
