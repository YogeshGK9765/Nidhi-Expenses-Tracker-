package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subcategories",
    indices = [Index("categoryId")]
)
data class SubcategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val subcategoryId: Long = 0,
    val categoryId: Long,
    val name: String,
    val isActive: Boolean = true
)
