package com.nesrine.smartshop.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nesrine.smartshop.data.local.Product
import com.nesrine.smartshop.data.repository.ProductRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private var cloudJob: Job? = null

    fun loadProducts() {
        // Show Room first
        viewModelScope.launch {
            _products.value = repository.getAllLocalProducts()
        }

        // Listen to Firestore once, and sync Cloud -> Room
        if (cloudJob == null) {
            cloudJob = viewModelScope.launch {
                repository.observeCloud().collectLatest { cloudProducts ->
                    repository.upsertAllLocal(cloudProducts)              // Cloud -> Room
                    _products.value = repository.getAllLocalProducts()    // UI <- Room
                }
            }
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            val newId = repository.insertLocalProduct(product)
            val saved = product.copy(id = newId)

            repository.upsertCloud(saved)
            _products.value = repository.getAllLocalProducts()
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateLocalProduct(product)
            repository.upsertCloud(product)
            _products.value = repository.getAllLocalProducts()
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteLocalProduct(product)
            repository.deleteCloud(product.id)
            _products.value = repository.getAllLocalProducts()
        }
    }

    fun clearOnLogout() {
        cloudJob?.cancel()
        cloudJob = null
        _products.value = emptyList()
    }
}
