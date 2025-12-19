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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ProductViewModel,
    onLogout: () -> Unit
) {
    val products by viewModel.products.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) } // null = add, else edit

    var name by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadProducts() }

    fun openAddDialog() {
        editingProduct = null
        name = ""
        quantity = ""
        price = ""
        showDialog = true
    }

    fun openEditDialog(product: Product) {
        editingProduct = product
        name = product.name
        quantity = product.quantity.toString()
        price = product.price.toString()
        showDialog = true
    }

    fun closeDialog() {
        showDialog = false
        editingProduct = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SmartShop") },
                actions = {
                    TextButton(onClick = onLogout) { Text("Logout") }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(products, key = { it.id }) { product ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${product.id} - ${product.name}")
                            Text("Qty: ${product.quantity}   Price: ${product.price}")
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { openEditDialog(product) }) {
                                Text("Modifier")
                            }
                            Button(onClick = { viewModel.deleteProduct(product) }) {
                                Text("Supprimer")
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { openAddDialog() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Ajouter un produit")
            }
        }

        if (showDialog) {
            // --- Validation (same for Add and Edit) ---
            val qInt = quantity.toIntOrNull()
            val pDouble = price.toDoubleOrNull()

            val nameError = if (name.isBlank()) "Le nom est obligatoire" else null

            val quantityError = when {
                quantity.isBlank() -> "La quantité est obligatoire"
                qInt == null -> "La quantité doit être un nombre"
                qInt < 0 -> "La quantité doit être ≥ 0"
                else -> null
            }

            val priceError = when {
                price.isBlank() -> "Le prix est obligatoire"
                pDouble == null -> "Le prix doit être un nombre"
                pDouble <= 0.0 -> "Le prix doit être > 0"
                else -> null
            }

            val formValid = nameError == null && quantityError == null && priceError == null

            val isEditMode = editingProduct != null
            val dialogTitle = if (isEditMode) "Modifier un produit" else "Ajouter un produit"
            val confirmLabel = if (isEditMode) "Enregistrer" else "Ajouter"

            Dialog(onDismissRequest = { closeDialog() }) {
                Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 8.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(dialogTitle, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(10.dp))

                        // Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nom") },
                            isError = nameError != null,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (nameError != null) {
                            Text(
                                text = nameError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        // Quantity
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Quantité") },
                            isError = quantityError != null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (quantityError != null) {
                            Text(
                                text = quantityError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        // Price
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Prix") },
                            isError = priceError != null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (priceError != null) {
                            Text(
                                text = priceError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { closeDialog() }) { Text("Annuler") }
                            Spacer(Modifier.width(8.dp))

                            Button(
                                enabled = formValid,
                                onClick = {
                                    val finalName = name.trim()
                                    val finalQ = qInt!!
                                    val finalP = pDouble!!

                                    if (isEditMode) {
                                        val updated = editingProduct!!.copy(
                                            name = finalName,
                                            quantity = finalQ,
                                            price = finalP
                                        )
                                        viewModel.updateProduct(updated)
                                    } else {
                                        viewModel.addProduct(
                                            Product(
                                                name = finalName,
                                                quantity = finalQ,
                                                price = finalP
                                            )
                                        )
                                    }

                                    closeDialog()
                                }
                            ) {
                                Text(confirmLabel)
                            }
                        }
                    }
                }
            }
        }
    }
}
