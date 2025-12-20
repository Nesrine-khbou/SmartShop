package com.nesrine.smartshop.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nesrine.smartshop.data.local.Product
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ProductViewModel,
    onLogout: () -> Unit
) {
    val products by viewModel.products.collectAsState()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Add/Edit dialog state
    var showForm by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) } // null = add

    // Delete confirmation state
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    // Form fields
    var name by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadProducts() }

    fun openAdd() {
        editingProduct = null
        name = ""
        quantity = ""
        price = ""
        showForm = true
    }

    fun openEdit(p: Product) {
        editingProduct = p
        name = p.name
        quantity = p.quantity.toString()
        price = p.price.toString()
        showForm = true
    }

    fun closeForm() {
        showForm = false
        editingProduct = null
    }

    fun requestDelete(p: Product) {
        productToDelete = p
        showDeleteDialog = true
    }

    fun closeDelete() {
        showDeleteDialog = false
        productToDelete = null
    }

    // Nice summary (optional but premium)
    val totalValue by remember(products) {
        mutableStateOf(products.sumOf { it.quantity * it.price })
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SmartShop", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${products.size} produits • Valeur stock: ${"%.2f".format(totalValue)} DT",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Outlined.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { openAdd() }) {
                Icon(Icons.Outlined.Add, contentDescription = "Add product")
            }
        }
    ) { padding ->

        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Aucun produit", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Ajoute ton premier produit avec le bouton +",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { openEdit(product) }
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(product.name, style = MaterialTheme.typography.titleMedium)
                            },
                            supportingContent = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AssistChip(
                                        onClick = { },
                                        label = { Text("Qty ${product.quantity}") }
                                    )
                                    AssistChip(
                                        onClick = { },
                                        label = { Text("${product.price} DT") }
                                    )
                                }
                            },
                            trailingContent = {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilledTonalIconButton(onClick = { openEdit(product) }) {
                                        Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { requestDelete(product) }) {
                                        Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // ---------------- Add/Edit Form (same validation) ----------------
    if (showForm) {
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
        val isEdit = editingProduct != null

        AlertDialog(
            onDismissRequest = { closeForm() },
            title = { Text(if (isEdit) "Modifier le produit" else "Ajouter un produit") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            nameError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

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
                            quantityError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

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
                            priceError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { closeForm() }) { Text("Annuler") }
            },
            confirmButton = {
                Button(
                    enabled = formValid,
                    onClick = {
                        val finalName = name.trim()
                        val finalQ = qInt!!
                        val finalP = pDouble!!

                        if (isEdit) {
                            val updated = editingProduct!!.copy(
                                name = finalName,
                                quantity = finalQ,
                                price = finalP
                            )
                            viewModel.updateProduct(updated)
                            scope.launch { snackbarHostState.showSnackbar("Produit mis à jour ✅") }
                        } else {
                            viewModel.addProduct(Product(name = finalName, quantity = finalQ, price = finalP))
                            scope.launch { snackbarHostState.showSnackbar("Produit ajouté ✅") }
                        }

                        closeForm()
                    }
                ) {
                    Text(if (isEdit) "Enregistrer" else "Ajouter")
                }
            }
        )
    }

    // ---------------- Delete Confirmation ----------------
    if (showDeleteDialog && productToDelete != null) {
        AlertDialog(
            onDismissRequest = { closeDelete() },
            title = { Text("Supprimer") },
            text = { Text("Voulez-vous vraiment supprimer ce produit ?") },
            dismissButton = {
                TextButton(onClick = { closeDelete() }) { Text("Annuler") }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        viewModel.deleteProduct(productToDelete!!)
                        closeDelete()
                        scope.launch { snackbarHostState.showSnackbar("Produit supprimé!") }
                    }
                ) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.onError)
                }
            }
        )
    }
}
