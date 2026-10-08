package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val accountId: Long = 0,
    val name: String,
    val type: String = "Bank",
    val isActive: Boolean = true
)
