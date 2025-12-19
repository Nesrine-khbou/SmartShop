package com.nesrine.smartshop.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nesrine.smartshop.data.local.Product
import com.nesrine.smartshop.ui.products.ProductViewModel

@Composable
fun ProductsScreen(viewModel: ProductViewModel) {
    val products by viewModel.products.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    var name by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadProducts() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(products) { product ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${product.name} - ${product.quantity} - ${product.price}")
                    Button(onClick = { viewModel.deleteProduct(product) }) {
                        Text("Supprimer")
                    }
                }
            }
        }

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Ajouter un produit")
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ajouter un produit", style = MaterialTheme.typography.titleMedium)

                    Spacer(Modifier.height(8.dp))
                    TextField(value = name, onValueChange = { name = it }, label = { Text("Nom") })
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantité") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Prix") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showDialog = false }) { Text("Annuler") }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            if (name.isNotBlank() && quantity.toIntOrNull() != null && price.toDoubleOrNull() != null) {
                                val newProduct = Product(
                                    name = name,
                                    quantity = quantity.toInt(),
                                    price = price.toDouble()
                                )
                                viewModel.addProduct(newProduct)
                                name = ""
                                quantity = ""
                                price = ""
                                showDialog = false
                            }
                        }) {
                            Text("Ajouter")
                        }
                    }
                }
            }
        }
    }
}
