package com.shamana.smartgroceryapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shamana.smartgroceryapp.data.model.Ingredient
import com.shamana.smartgroceryapp.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(
    ingredients: List<Ingredient>,
    onGenerateRecipes: (indianOnly: Boolean) -> Unit,
    viewModel: MainViewModel
) {
    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFFF4F7FB),
            Color(0xFFEFF3F8)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Your Grocery List",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            Text(
                "Review and adjust your items",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Gray
                ),
                modifier = Modifier.padding(bottom = 14.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(5.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(ingredients) { item ->
                        IngredientItemCard(item) { updated ->
                            val updatedList = ingredients.map {
                                if (it == item) updated else it
                            }
                            viewModel.updateIngredients(updatedList)
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.height(20.dp))

            var indianOnly by remember { mutableStateOf(false) }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    "Indian recipes only",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = indianOnly,
                    onCheckedChange = { indianOnly = it }
                )
            }


            Button(
                onClick = {
                    onGenerateRecipes(indianOnly)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(15.dp),
                        ambientColor = Color.Black.copy(alpha = 0.1f)
                    ),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF43A047)
                )
            ) {
                Text(
                    "Generate Recipes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }


        }
    }
}

@Composable
private fun IngredientItemCard(
    item: Ingredient,
    onUpdate: (Ingredient) -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var quantity by remember { mutableStateOf(item.quantity.toString()) }
    var unit by remember { mutableStateOf(item.unit) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        onUpdate(item.copy(name = it))
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Name") },
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        quantity = it
                        onUpdate(item.copy(quantity = it.toDoubleOrNull() ?: 0.0))
                    },
                    modifier = Modifier.width(90.dp),
                    label = { Text("Qty") },
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = unit,
                    onValueChange = {
                        unit = it
                        onUpdate(item.copy(unit = it))
                    },
                    modifier = Modifier.width(90.dp),
                    label = { Text("Unit") },
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Confidence: ${"%.0f".format(item.confidence * 100)}%",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

