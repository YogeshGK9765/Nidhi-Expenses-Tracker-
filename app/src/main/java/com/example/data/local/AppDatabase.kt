package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AccountDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.PaymentMethodDao
import com.example.data.local.dao.SubcategoryDao
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.PaymentMethodEntity
import com.example.data.local.entity.SubcategoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExpenseEntity::class,
        CategoryEntity::class,
        SubcategoryEntity::class,
        AccountEntity::class,
        PaymentMethodEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun subcategoryDao(): SubcategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun paymentMethodDao(): PaymentMethodDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nidhi_expense_tracker.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    // Ensure categories and accounts are present if fresh or cleared
                    if (database.categoryDao().getCategoryCount() == 0) {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val categoryDao = db.categoryDao()
            val subcategoryDao = db.subcategoryDao()
            val accountDao = db.accountDao()
            val paymentMethodDao = db.paymentMethodDao()

            // 1. Categories
            val catFoodId = categoryDao.insertCategory(CategoryEntity(name = "Food", icon = "food"))
            val catTravelId = categoryDao.insertCategory(CategoryEntity(name = "Travel", icon = "travel"))
            val catEducationId = categoryDao.insertCategory(CategoryEntity(name = "Education", icon = "education"))
            val catShoppingId = categoryDao.insertCategory(CategoryEntity(name = "Shopping", icon = "shopping"))
            val catPersonalId = categoryDao.insertCategory(CategoryEntity(name = "Personal", icon = "personal"))
            val catBillsId = categoryDao.insertCategory(CategoryEntity(name = "Bills", icon = "bills"))
            categoryDao.insertCategory(CategoryEntity(name = "Other", icon = "other"))

            // 2. Subcategories
            // Food
            val subFood = listOf("Breakfast", "Lunch", "Dinner", "Snacks", "Tea/Coffee", "Canteen", "Restaurant")
            subFood.forEach { name ->
                subcategoryDao.insertSubcategory(SubcategoryEntity(categoryId = catFoodId, name = name))
            }

            // Travel
            val subTravel = listOf("Bus", "Train", "Auto", "Cab", "Fuel", "Parking")
            subTravel.forEach { name ->
                subcategoryDao.insertSubcategory(SubcategoryEntity(categoryId = catTravelId, name = name))
            }

            // Education
            val subEducation = listOf("College", "Books", "Stationery", "Course", "Exam", "Project")
            subEducation.forEach { name ->
                subcategoryDao.insertSubcategory(SubcategoryEntity(categoryId = catEducationId, name = name))
            }

            // Shopping
            val subShopping = listOf("Clothes", "Shoes", "Electronics", "Accessories")
            subShopping.forEach { name ->
                subcategoryDao.insertSubcategory(SubcategoryEntity(categoryId = catShoppingId, name = name))
            }

            // Personal
            val subPersonal = listOf("Grooming", "Entertainment", "Hobbies", "Other")
            subPersonal.forEach { name ->
                subcategoryDao.insertSubcategory(SubcategoryEntity(categoryId = catPersonalId, name = name))
            }

            // Bills
            val subBills = listOf("Mobile", "Internet", "Electricity", "Subscription")
            subBills.forEach { name ->
                subcategoryDao.insertSubcategory(SubcategoryEntity(categoryId = catBillsId, name = name))
            }

            // 3. Accounts (SBI is default)
            accountDao.insertAccount(AccountEntity(name = "SBI", type = "Bank"))
            accountDao.insertAccount(AccountEntity(name = "IPPB", type = "Bank"))
            accountDao.insertAccount(AccountEntity(name = "Airtel", type = "Payment Bank"))
            accountDao.insertAccount(AccountEntity(name = "MGB", type = "Bank"))
            accountDao.insertAccount(AccountEntity(name = "Cash", type = "Cash"))

            // 4. Payment Methods
            paymentMethodDao.insertPaymentMethod(PaymentMethodEntity(name = "UPI"))
            paymentMethodDao.insertPaymentMethod(PaymentMethodEntity(name = "Cash"))
            paymentMethodDao.insertPaymentMethod(PaymentMethodEntity(name = "Debit Card"))
            paymentMethodDao.insertPaymentMethod(PaymentMethodEntity(name = "Credit Card"))
            paymentMethodDao.insertPaymentMethod(PaymentMethodEntity(name = "Bank Transfer"))
            paymentMethodDao.insertPaymentMethod(PaymentMethodEntity(name = "Other"))

            // NOTE: NO DEFAULT EXPENSES ADDED.
            // Starts cleanly with 0 expenses as requested by user.
        }
    }
}

// Backward compatibility alias
typealias NidhiDatabase = AppDatabase
