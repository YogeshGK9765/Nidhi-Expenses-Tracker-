package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [
        Index("categoryId"),
        Index("accountId"),
        Index("expenseDate"),
        Index("createdAt")
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val expenseId: Long = 0,
    val userId: String = "user_default",
    val amount: Long, // minor units (paise)
    val currency: String = "INR",
    val title: String,
    val categoryId: Long,
    val subcategoryId: Long? = null,
    val accountId: Long,
    val paymentMethodId: Long? = null,
    val note: String? = null,
    val expenseDate: Long,
    val expenseTime: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
