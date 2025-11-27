package com.shamana.smartgroceryapp.utils

import com.shamana.smartgroceryapp.data.model.Ingredient

object TextParser {

    private val pattern = Regex(
        """([A-Za-z\s]+)\s*[-:]?\s*(\d*\.?\d+)\s*([a-zA-Z]+)?""",
        RegexOption.IGNORE_CASE
    )

    fun parseIngredients(raw: String): List<Ingredient> {
        if (raw.isBlank()) return emptyList()

        return pattern.findAll(raw).map { match ->
            Ingredient(
                name = match.groupValues[1].trim(),
                quantity = match.groupValues[2].toDoubleOrNull() ?: 0.0,
                unit = match.groupValues[3].orEmpty().lowercase()
            )
        }.toList()
    }
}
