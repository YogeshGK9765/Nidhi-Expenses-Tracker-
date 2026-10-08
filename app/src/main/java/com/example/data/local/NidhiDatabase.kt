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
import com.example.domain.DateUtils
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
    version = 1,
    exportSchema = false
)
abstract class NidhiDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun subcategoryDao(): SubcategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun paymentMethodDao(): PaymentMethodDao

    companion object {
        @Volatile
        private var INSTANCE: NidhiDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): NidhiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NidhiDatabase::class.java,
                    "nidhi_expense_tracker.db"
                )
                    .addCallback(NidhiDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class NidhiDatabaseCallback(
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

        private suspend fun populateInitialData(db: NidhiDatabase) {
            val categoryDao = db.categoryDao()
            val subcategoryDao = db.subcategoryDao()
            val accountDao = db.accountDao()
            val paymentMethodDao = db.paymentMethodDao()
            val expenseDao = db.expenseDao()

            // 1. Categories
            val catFoodId = categoryDao.insertCategory(CategoryEntity(name = "Food", icon = "food"))
            val catTravelId = categoryDao.insertCategory(CategoryEntity(name = "Travel", icon = "travel"))
            val catEducationId = categoryDao.insertCategory(CategoryEntity(name = "Education", icon = "education"))
            val catShoppingId = categoryDao.insertCategory(CategoryEntity(name = "Shopping", icon = "shopping"))
            val catPersonalId = categoryDao.insertCategory(CategoryEntity(name = "Personal", icon = "personal"))
            val catBillsId = categoryDao.insertCategory(CategoryEntity(name = "Bills", icon = "bills"))
            val catOtherId = categoryDao.insertCategory(CategoryEntity(name = "Other", icon = "other"))

            // 2. Subcategories
            // Food
            val subFood = listOf("Breakfast", "Lunch", "Dinner", "Snacks", "Tea/Coffee", "Canteen", "Restaurant")
            val subFoodIds = mutableMapOf<String, Long>()
            subFood.forEach { name ->
                subFoodIds[name] = subcategoryDao.insertSubcategory(SubcategoryEntity(categoryId = catFoodId, name = name))
            }

            // Travel
            val subTravel = listOf("Bus", "Train", "Auto", "Cab", "Fuel", "Parking")
            val subTravelIds = mutableMapOf<String, Long>()
            subTravel.forEach { name ->
                subTravelIds[name] = subcategoryDao.insertSubcategory(SubcategoryEntity(categoryId = catTravelId, name = name))
            }

            // Education
            val subEducation = listOf("College", "Books", "Stationery", "Course", "Exam", "Project")
            val subEducationIds = mutableMapOf<String, Long>()
            subEducation.forEach { name ->
                subEducationIds[name] = subcategoryDao.insertSubcategory(SubcategoryEntity(categoryId = catEducationId, name = name))
            }

            // Shopping
            val subShopping = listOf("Clothes", "Shoes", "Electronics", "Accessories")
            val subShoppingIds = mutableMapOf<String, Long>()
            subShopping.forEach { name ->
                subShoppingIds[name] = subcategoryDao.insertSubcategory(SubcategoryEntity(categoryId = catShoppingId, name = name))
            }

            // Personal
            val subPersonal = listOf("Grooming", "Entertainment", "Hobbies", "Other")
            val subPersonalIds = mutableMapOf<String, Long>()
            subPersonal.forEach { name ->
                subPersonalIds[name] = subcategoryDao.insertSubcategory(SubcategoryEntity(categoryId = catPersonalId, name = name))
            }

            // Bills
            val subBills = listOf("Mobile", "Internet", "Electricity", "Subscription")
            val subBillsIds = mutableMapOf<String, Long>()
            subBills.forEach { name ->
                subBillsIds[name] = subcategoryDao.insertSubcategory(SubcategoryEntity(categoryId = catBillsId, name = name))
            }

            // Other
            val subOther = listOf("General", "Miscellaneous", "Other")
            val subOtherIds = mutableMapOf<String, Long>()
            subOther.forEach { name ->
                subOtherIds[name] = subcategoryDao.insertSubcategory(SubcategoryEntity(categoryId = catOtherId, name = name))
            }

            // 3. Accounts (SBI is default)
            val accSbiId = accountDao.insertAccount(AccountEntity(name = "SBI", type = "Bank"))
            val accIppbId = accountDao.insertAccount(AccountEntity(name = "IPPB", type = "Bank"))
            val accAirtelId = accountDao.insertAccount(AccountEntity(name = "Airtel", type = "Payment Bank"))
            val accMgbId = accountDao.insertAccount(AccountEntity(name = "MGB", type = "Bank"))
            val accCashId = accountDao.insertAccount(AccountEntity(name = "Cash", type = "Cash"))

            // 4. Payment Methods
            val pmUpiId = paymentMethodDao.insertPaymentMethod(PaymentMethodEntity(name = "UPI"))
            val pmCashId = paymentMethodDao.insertPaymentMethod(PaymentMethodEntity(name = "Cash"))
            val pmDebitCardId = paymentMethodDao.insertPaymentMethod(PaymentMethodEntity(name = "Debit Card"))
            val pmCreditCardId = paymentMethodDao.insertPaymentMethod(PaymentMethodEntity(name = "Credit Card"))
            val pmBankTransferId = paymentMethodDao.insertPaymentMethod(PaymentMethodEntity(name = "Bank Transfer"))
            val pmOtherId = paymentMethodDao.insertPaymentMethod(PaymentMethodEntity(name = "Other"))

            // 5. Seed Initial Expenses
            val todayStart = DateUtils.getTodayStartMillis()
            val nowMillis = System.currentTimeMillis()
            val yesterdayStart = todayStart - (24 * 60 * 60 * 1000L)
            val fiveDaysAgo = todayStart - (5 * 24 * 60 * 60 * 1000L)
            val tenDaysAgo = todayStart - (10 * 24 * 60 * 60 * 1000L)
            val twentyDaysAgo = todayStart - (20 * 24 * 60 * 60 * 1000L)

            // Today expenses: ₹80 (Canteen) + ₹30 (Bus) + ₹110 (Breakfast) + ₹300 (Snacks) = ₹520
            val initialExpenses = listOf(
                ExpenseEntity(
                    amount = 8000L, // ₹80
                    title = "Canteen",
                    categoryId = catFoodId,
                    subcategoryId = subFoodIds["Canteen"],
                    accountId = accSbiId,
                    paymentMethodId = pmUpiId,
                    note = "College canteen lunch",
                    expenseDate = todayStart + (10 * 3600 * 1000L) + (30 * 60 * 1000L),
                    expenseTime = "10:30 AM",
                    createdAt = nowMillis - 100000L
                ),
                ExpenseEntity(
                    amount = 3000L, // ₹30
                    title = "Bus",
                    categoryId = catTravelId,
                    subcategoryId = subTravelIds["Bus"],
                    accountId = accSbiId,
                    paymentMethodId = pmCashId,
                    note = "Daily transit",
                    expenseDate = todayStart + (9 * 3600 * 1000L) + (20 * 60 * 1000L),
                    expenseTime = "09:20 AM",
                    createdAt = nowMillis - 200000L
                ),
                ExpenseEntity(
                    amount = 11000L, // ₹110
                    title = "Breakfast",
                    categoryId = catFoodId,
                    subcategoryId = subFoodIds["Breakfast"],
                    accountId = accSbiId,
                    paymentMethodId = pmUpiId,
                    note = "Morning snacks",
                    expenseDate = todayStart + (8 * 3600 * 1000L) + (45 * 60 * 1000L),
                    expenseTime = "08:45 AM",
                    createdAt = nowMillis - 300000L
                ),
                ExpenseEntity(
                    amount = 30000L, // ₹300
                    title = "Snacks",
                    categoryId = catFoodId,
                    subcategoryId = subFoodIds["Snacks"],
                    accountId = accIppbId,
                    paymentMethodId = pmUpiId,
                    note = "Evening snacks with friends",
                    expenseDate = todayStart + (12 * 3600 * 1000L),
                    expenseTime = "12:00 PM",
                    createdAt = nowMillis - 50000L
                ),
                // Yesterday expenses: Stationery ₹250, Mobile Recharge ₹299
                ExpenseEntity(
                    amount = 25000L, // ₹250
                    title = "Stationery",
                    categoryId = catEducationId,
                    subcategoryId = subEducationIds["Stationery"],
                    accountId = accSbiId,
                    paymentMethodId = pmUpiId,
                    note = "Notebooks and pens",
                    expenseDate = yesterdayStart + (18 * 3600 * 1000L) + (30 * 60 * 1000L),
                    expenseTime = "06:30 PM",
                    createdAt = nowMillis - 86400000L
                ),
                ExpenseEntity(
                    amount = 29900L, // ₹299
                    title = "Mobile",
                    categoryId = catBillsId,
                    subcategoryId = subBillsIds["Mobile"],
                    accountId = accAirtelId,
                    paymentMethodId = pmUpiId,
                    note = "Monthly recharge",
                    expenseDate = yesterdayStart + (14 * 3600 * 1000L),
                    expenseTime = "02:00 PM",
                    createdAt = nowMillis - 90000000L
                ),
                // Additional expenses this month:
                ExpenseEntity(
                    amount = 125000L, // ₹1,250 (Highest expense example from prompt)
                    title = "Course",
                    categoryId = catEducationId,
                    subcategoryId = subEducationIds["Course"],
                    accountId = accSbiId,
                    paymentMethodId = pmDebitCardId,
                    note = "Online certification course",
                    expenseDate = fiveDaysAgo + (11 * 3600 * 1000L),
                    expenseTime = "11:00 AM",
                    createdAt = nowMillis - 400000000L
                ),
                ExpenseEntity(
                    amount = 90000L, // ₹900
                    title = "Clothes",
                    categoryId = catShoppingId,
                    subcategoryId = subShoppingIds["Clothes"],
                    accountId = accSbiId,
                    paymentMethodId = pmUpiId,
                    note = "Weekend shopping",
                    expenseDate = tenDaysAgo + (17 * 3600 * 1000L),
                    expenseTime = "05:00 PM",
                    createdAt = nowMillis - 800000000L
                ),
                ExpenseEntity(
                    amount = 55100L, // ₹551
                    title = "Electricity",
                    categoryId = catBillsId,
                    subcategoryId = subBillsIds["Electricity"],
                    accountId = accMgbId,
                    paymentMethodId = pmBankTransferId,
                    note = "Power bill",
                    expenseDate = twentyDaysAgo + (10 * 3600 * 1000L),
                    expenseTime = "10:00 AM",
                    createdAt = nowMillis - 1600000000L
                ),
                ExpenseEntity(
                    amount = 147000L, // ₹1,470
                    title = "Train",
                    categoryId = catTravelId,
                    subcategoryId = subTravelIds["Train"],
                    accountId = accSbiId,
                    paymentMethodId = pmDebitCardId,
                    note = "Home trip ticket",
                    expenseDate = twentyDaysAgo + (15 * 3600 * 1000L),
                    expenseTime = "03:00 PM",
                    createdAt = nowMillis - 1700000000L
                ),
                ExpenseEntity(
                    amount = 301000L, // ₹3,010
                    title = "Restaurant",
                    categoryId = catFoodId,
                    subcategoryId = subFoodIds["Restaurant"],
                    accountId = accSbiId,
                    paymentMethodId = pmCreditCardId,
                    note = "Family dinner",
                    expenseDate = tenDaysAgo + (20 * 3600 * 1000L),
                    expenseTime = "08:00 PM",
                    createdAt = nowMillis - 850000000L
                )
            )

            expenseDao.insertExpenses(initialExpenses)
        }
    }
}
