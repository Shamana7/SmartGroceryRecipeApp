package com.shamana.smartgroceryapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shamana.smartgroceryapp.screens.CaptureScreen
import com.shamana.smartgroceryapp.screens.GroceryListScreen
import com.shamana.smartgroceryapp.screens.RecipeScreen
import com.shamana.smartgroceryapp.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val CAMERA_PERMISSION_CODE = 100
    private val API_KEY = BuildConfig.GROQ_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestCameraPermission()

        setContent {
            MaterialTheme {
                val context = LocalContext.current

                val viewModel: MainViewModel = viewModel()
                val navController = rememberNavController()

                NavHost(navController, startDestination = "capture") {


                    composable("capture") {
                        CaptureScreen { file ->
                            viewModel.processImage(file, context)
                            navController.navigate("grocery")
                        }
                    }

                    composable("grocery") {
                        GroceryListScreen(
                            ingredients = viewModel.ingredients.collectAsState().value,
                            onGenerateRecipes = { indianOnly ->
                                viewModel.generateRecipes(indianOnly)
                                navController.navigate("recipes")
                            },
                            viewModel = viewModel
                        )
                    }

                    composable("recipes") {
                        RecipeScreen(
                            recipes = viewModel.recipes.collectAsState().value,
                            apiKey = API_KEY,
                            onRecipeClick = { recipe ->
                                navController.navigate("recipe_detail/${recipe.title}")
                            }
                        )
                    }
                }
            }
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }
}
