package com.nesrine.smartshop.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String ="",
    val quantity: Int =0,
    val price: Double =0.0
)