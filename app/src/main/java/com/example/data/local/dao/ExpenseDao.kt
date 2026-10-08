package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.ExpenseEntity
import com.example.data.model.ExpenseWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Transaction
    @Query("SELECT * FROM expenses ORDER BY expenseDate DESC, createdAt DESC")
    fun getAllExpensesWithDetails(): Flow<List<ExpenseWithDetails>>

    @Transaction
    @Query("SELECT * FROM expenses ORDER BY expenseDate DESC, createdAt DESC LIMIT 5")
    fun getRecentExpensesWithDetails(): Flow<List<ExpenseWithDetails>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE expenseId = :id")
    suspend fun getExpenseById(id: Long): ExpenseWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE expenseId = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses")
    fun getTotalExpensePaise(): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE expenseDate >= :startMillis AND expenseDate <= :endMillis")
    fun getExpensePaiseBetween(startMillis: Long, endMillis: Long): Flow<Long>

    @Transaction
    @Query("SELECT * FROM expenses WHERE expenseDate >= :startMillis AND expenseDate <= :endMillis ORDER BY expenseDate DESC, createdAt DESC")
    fun getExpensesBetween(startMillis: Long, endMillis: Long): Flow<List<ExpenseWithDetails>>

    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun getExpenseCount(): Int
}
