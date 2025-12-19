package com.nesrine.smartshop.data.repository

import com.nesrine.smartshop.data.local.Product
import com.nesrine.smartshop.data.local.ProductDao
import com.nesrine.smartshop.data.remote.FirestoreProductDataSource
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val dao: ProductDao,
    private val remote: FirestoreProductDataSource
) {
    // Local
    suspend fun getAllLocalProducts(): List<Product> = dao.getAllProducts()
    suspend fun updateLocalProduct(product: Product) = dao.updateProduct(product)
    suspend fun deleteLocalProduct(product: Product) = dao.deleteProduct(product)


    suspend fun upsertAllLocal(products: List<Product>) = dao.insertAll(products)

    // Remote
    suspend fun upsertCloud(product: Product) = remote.upsert(product)
    suspend fun deleteCloud(productId: Int) = remote.delete(productId)
    fun observeCloud(): Flow<List<Product>> = remote.observeProducts()

    // Insert local needs generated id (see section 2)
    suspend fun insertLocalProduct(product: Product): Int =
        dao.insertProduct(product).toInt()
}
