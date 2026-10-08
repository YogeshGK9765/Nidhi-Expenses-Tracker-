package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {

    @Query("SELECT * FROM payment_methods WHERE isActive = 1 ORDER BY paymentMethodId ASC")
    fun getAllActivePaymentMethods(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT * FROM payment_methods WHERE paymentMethodId = :id")
    suspend fun getPaymentMethodById(id: Long): PaymentMethodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(paymentMethod: PaymentMethodEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPaymentMethods(paymentMethods: List<PaymentMethodEntity>)

    @Update
    suspend fun updatePaymentMethod(paymentMethod: PaymentMethodEntity)

    @Query("SELECT COUNT(*) FROM payment_methods")
    suspend fun getPaymentMethodCount(): Int
}
