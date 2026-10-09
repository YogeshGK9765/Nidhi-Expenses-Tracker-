package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.ExpenseRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class NidhiApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository: ExpenseRepository by lazy {
        ExpenseRepositoryImpl(
            database.expenseDao(),
            database.categoryDao(),
            database.subcategoryDao(),
            database.accountDao(),
            database.paymentMethodDao()
        )
    }
}
