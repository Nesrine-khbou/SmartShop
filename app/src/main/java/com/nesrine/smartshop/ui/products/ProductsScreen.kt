package com.nesrine.smartshop.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nesrine.smartshop.data.local.Product
import kotlinx.coroutines.launch
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ProductViewModel,
    onLogout: () -> Unit
) {
    val products by viewModel.products.collectAsState()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Sheet (Add/Edit)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) } // null = add

    // Delete confirmation
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    // Form fields
    var name by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }

    // Search
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(products, query) {
        if (query.isBlank()) products
        else products.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }

    // Stats
    val totalValue = products.sumOf { it.quantity * it.price }

    LaunchedEffect(Unit) { viewModel.loadProducts() }

    fun openAdd() {
        editingProduct = null
        name = ""
        quantity = ""
        price = ""
        showSheet = true
    }

    fun openEdit(p: Product) {
        editingProduct = p
        name = p.name
        quantity = p.quantity.toString()
        price = p.price.toString()
        showSheet = true
    }

    fun closeSheet() {
        showSheet = false
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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("SmartShop", fontWeight = FontWeight.SemiBold)
                        Text(
                            "${products.size} produits • ${"%.2f".format(totalValue)} DT",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Outlined.Logout, contentDescription = "Logout")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { openAdd() },
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("Ajouter") }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ---------- Premium header: stats + search ----------
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    ElevatedCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.ShoppingCart, contentDescription = null)
                                Text("Produits", style = MaterialTheme.typography.labelLarge)
                            }
                            Text(
                                "${products.size}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Total en stock",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    ElevatedCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.Payments, contentDescription = null)
                                Text("Valeur", style = MaterialTheme.typography.labelLarge)
                            }
                            Text(
                                "${"%.2f".format(totalValue)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "DT (prix × quantité)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Rechercher un produit") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Clear")
                            }
                        }
                    }
                )
            }

            // ---------- Empty state ----------
            if (filtered.isEmpty()) {
                item {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Aucun résultat", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Essaie un autre nom, ou ajoute un produit.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // ---------- Products list ----------
                items(filtered, key = { it.id }) { product ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { openEdit(product) }
                    ) {
                        ListItem(

                            headlineContent = {
                                Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            },
                            supportingContent = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AssistChip(onClick = { }, label = { Text("Qty ${product.quantity}") })
                                    AssistChip(onClick = { }, label = { Text("${"%.2f".format(product.price)} DT") })
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

    // -------------------- Add/Edit as Premium Bottom Sheet (with same validation) --------------------
    if (showSheet) {
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

        ModalBottomSheet(
            onDismissRequest = { closeSheet() },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    if (isEdit) "Modifier le produit" else "Ajouter un produit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Remplis les informations ci-dessous.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    isError = nameError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (nameError != null) {
                    Text(nameError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
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
                    Text(quantityError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Prix (DT)") },
                    isError = priceError != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                if (priceError != null) {
                    Text(priceError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { closeSheet() }
                    ) { Text("Annuler") }

                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = formValid,
                        onClick = {
                            val finalName = name.trim()
                            val finalQ = qInt!!
                            val finalP = pDouble!!

                            if (isEdit) {
                                val updated = editingProduct!!.copy(name = finalName, quantity = finalQ, price = finalP)
                                viewModel.updateProduct(updated)
                                scope.launch { snackbarHostState.showSnackbar("Produit mis à jour ✅") }
                            } else {
                                viewModel.addProduct(Product(name = finalName, quantity = finalQ, price = finalP))
                                scope.launch { snackbarHostState.showSnackbar("Produit ajouté ✅") }
                            }

                            closeSheet()
                        }
                    ) {
                        Text(if (isEdit) "Enregistrer" else "Ajouter")
                    }
                }
            }
        }
    }

    // -------------------- Delete Confirmation --------------------
    if (showDeleteDialog && productToDelete != null) {
        AlertDialog(
            onDismissRequest = { closeDelete() },
            title = { Text("Supprimer") },
            text = { Text("Supprimer « ${productToDelete!!.name} » ?") },
            dismissButton = {
                TextButton(onClick = { closeDelete() }) { Text("Annuler") }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        viewModel.deleteProduct(productToDelete!!)
                        closeDelete()
                        scope.launch { snackbarHostState.showSnackbar("Produit supprimé ✅") }
                    }
                ) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.onError)
                }
            }
        )
    }
}
