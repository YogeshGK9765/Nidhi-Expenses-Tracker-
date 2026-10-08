package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SubcategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubcategoryDao {

    @Query("SELECT * FROM subcategories WHERE categoryId = :categoryId AND isActive = 1 ORDER BY subcategoryId ASC")
    fun getSubcategoriesForCategory(categoryId: Long): Flow<List<SubcategoryEntity>>

    @Query("SELECT * FROM subcategories WHERE isActive = 1 ORDER BY subcategoryId ASC")
    fun getAllActiveSubcategories(): Flow<List<SubcategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubcategory(subcategory: SubcategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubcategories(subcategories: List<SubcategoryEntity>)

    @Update
    suspend fun updateSubcategory(subcategory: SubcategoryEntity)
}
