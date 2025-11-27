package com.shamana.smartgroceryapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamana.smartgroceryapp.data.SecureStorage
import com.shamana.smartgroceryapp.data.model.Ingredient
import com.shamana.smartgroceryapp.data.model.Recipe
import com.shamana.smartgroceryapp.data.repository.OCRRepository
import com.shamana.smartgroceryapp.data.repository.RecipeRepository
import com.shamana.smartgroceryapp.utils.TextParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _ingredients = MutableStateFlow<List<Ingredient>>(emptyList())
    val ingredients: StateFlow<List<Ingredient>> = _ingredients

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun updateIngredients(newList: List<Ingredient>) {
        _ingredients.value = newList
    }

    fun generateRecipes(indianOnly: Boolean = false) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val ingredientNames = ingredients.value.map { it.name }
                var generatedRecipes =
                    RecipeRepository.generateRecipesFromAPI(ingredientNames)

                if (indianOnly) {
                    generatedRecipes = generatedRecipes.filter { it.cuisine == "Indian" }
                }

                if (generatedRecipes.isEmpty()) {
                    _error.value = "Model returned no recipes."
                }

                _recipes.value = generatedRecipes

            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }


    fun processImage(file: java.io.File, context: android.content.Context) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val ocrResult = OCRRepository.extractTextFromImage(file)
                val parsedIngredients = TextParser.parseIngredients(ocrResult.text)
                    .map { it.copy(confidence = ocrResult.averageConfidence) }

                updateIngredients(parsedIngredients)

                SecureStorage.saveLastScan(context, ocrResult.text)

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to process image"
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

}
