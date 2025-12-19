package com.nesrine.smartshop.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nesrine.smartshop.data.local.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreProductDataSource(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun col() =
        firestore.collection("users")
            .document(requireNotNull(auth.currentUser).uid)
            .collection("products")

    suspend fun upsert(product: Product) {
        col().document(product.id.toString()).set(product).await()
    }

    suspend fun delete(productId: Int) {
        col().document(productId.toString()).delete().await()
    }

    fun observeProducts(): Flow<List<Product>> = callbackFlow {
        val reg = col().addSnapshotListener { snap, err ->
            if (err != null) {
                close(err)
                return@addSnapshotListener
            }
            if (snap != null) {
                trySend(snap.toObjects(Product::class.java))
            }
        }
        awaitClose { reg.remove() }
    }
}
