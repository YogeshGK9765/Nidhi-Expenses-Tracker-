package com.example.data.repository

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
import com.example.data.model.CategoryReportItem
import com.example.data.model.ExpenseWithDetails
import com.example.data.model.FilterDateRange
import com.example.data.model.FilterState
import com.example.data.model.ReportData
import com.example.data.model.ReportPeriod
import com.example.data.model.SortOrder
import com.example.data.model.TimeReportItem
import com.example.domain.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<ExpenseWithDetails>>
    fun getRecentExpenses(): Flow<List<ExpenseWithDetails>>
    suspend fun getExpenseById(id: Long): ExpenseWithDetails?
    suspend fun insertExpense(expense: ExpenseEntity): Long
    suspend fun updateExpense(expense: ExpenseEntity)
    suspend fun deleteExpenseById(id: Long)

    fun getTotalExpensePaise(): Flow<Long>
    fun getTodayExpensePaise(): Flow<Long>
    fun getThisMonthExpensePaise(): Flow<Long>

    fun getCategories(): Flow<List<CategoryEntity>>
    suspend fun insertCategory(category: CategoryEntity): Long
    fun getSubcategories(categoryId: Long): Flow<List<SubcategoryEntity>>
    suspend fun insertSubcategory(subcategory: SubcategoryEntity): Long

    fun getAccounts(): Flow<List<AccountEntity>>
    suspend fun insertAccount(account: AccountEntity): Long

    fun getPaymentMethods(): Flow<List<PaymentMethodEntity>>
    suspend fun insertPaymentMethod(paymentMethod: PaymentMethodEntity): Long

    fun getFilteredExpenses(filterState: FilterState): Flow<List<ExpenseWithDetails>>
    fun getReportData(period: ReportPeriod, accountId: Long?): Flow<ReportData>
}

class ExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val subcategoryDao: SubcategoryDao,
    private val accountDao: AccountDao,
    private val paymentMethodDao: PaymentMethodDao
) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<ExpenseWithDetails>> =
        expenseDao.getAllExpensesWithDetails()

    override fun getRecentExpenses(): Flow<List<ExpenseWithDetails>> =
        expenseDao.getRecentExpensesWithDetails()

    override suspend fun getExpenseById(id: Long): ExpenseWithDetails? =
        expenseDao.getExpenseById(id)

    override suspend fun insertExpense(expense: ExpenseEntity): Long =
        expenseDao.insertExpense(expense)

    override suspend fun updateExpense(expense: ExpenseEntity) =
        expenseDao.updateExpense(expense)

    override suspend fun deleteExpenseById(id: Long) =
        expenseDao.deleteExpenseById(id)

    override fun getTotalExpensePaise(): Flow<Long> =
        expenseDao.getTotalExpensePaise()

    override fun getTodayExpensePaise(): Flow<Long> {
        val start = DateUtils.getTodayStartMillis()
        val end = DateUtils.getTodayEndMillis()
        return expenseDao.getExpensePaiseBetween(start, end)
    }

    override fun getThisMonthExpensePaise(): Flow<Long> {
        val start = DateUtils.getStartOfMonthMillis()
        val end = System.currentTimeMillis() + (31L * 24 * 60 * 60 * 1000L)
        return expenseDao.getExpensePaiseBetween(start, end)
    }

    override fun getCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllActiveCategories()

    override suspend fun insertCategory(category: CategoryEntity): Long =
        categoryDao.insertCategory(category)

    override fun getSubcategories(categoryId: Long): Flow<List<SubcategoryEntity>> =
        subcategoryDao.getSubcategoriesForCategory(categoryId)

    override suspend fun insertSubcategory(subcategory: SubcategoryEntity): Long =
        subcategoryDao.insertSubcategory(subcategory)

    override fun getAccounts(): Flow<List<AccountEntity>> =
        accountDao.getAllActiveAccounts()

    override suspend fun insertAccount(account: AccountEntity): Long =
        accountDao.insertAccount(account)

    override fun getPaymentMethods(): Flow<List<PaymentMethodEntity>> =
        paymentMethodDao.getAllActivePaymentMethods()

    override suspend fun insertPaymentMethod(paymentMethod: PaymentMethodEntity): Long =
        paymentMethodDao.insertPaymentMethod(paymentMethod)

    override fun getFilteredExpenses(filterState: FilterState): Flow<List<ExpenseWithDetails>> {
        return expenseDao.getAllExpensesWithDetails().map { list ->
            var result = list

            // 1. Account Filter
            if (filterState.accountId != null) {
                result = result.filter { it.expense.accountId == filterState.accountId }
            }

            // 2. Category Filter
            if (filterState.categoryId != null) {
                result = result.filter { it.expense.categoryId == filterState.categoryId }
            }

            // 3. Payment Method Filter
            if (filterState.paymentMethodId != null) {
                result = result.filter { it.expense.paymentMethodId == filterState.paymentMethodId }
            }

            // 4. Date Range Filter
            result = when (filterState.dateRange) {
                FilterDateRange.ALL -> result
                FilterDateRange.TODAY -> {
                    val start = DateUtils.getTodayStartMillis()
                    val end = DateUtils.getTodayEndMillis()
                    result.filter { it.expense.expenseDate in start..end }
                }
                FilterDateRange.THIS_WEEK -> {
                    val start = DateUtils.getStartOfWeekMillis()
                    val end = System.currentTimeMillis()
                    result.filter { it.expense.expenseDate in start..end }
                }
                FilterDateRange.THIS_MONTH -> {
                    val start = DateUtils.getStartOfMonthMillis()
                    val end = System.currentTimeMillis()
                    result.filter { it.expense.expenseDate in start..end }
                }
                FilterDateRange.CUSTOM -> {
                    val start = filterState.customStartDate ?: 0L
                    val end = filterState.customEndDate ?: Long.MAX_VALUE
                    result.filter { it.expense.expenseDate in start..end }
                }
            }

            // 5. Search Query
            if (filterState.searchQuery.isNotBlank()) {
                val q = filterState.searchQuery.trim().lowercase()
                result = result.filter { item ->
                    item.displayTitle.lowercase().contains(q) ||
                            item.categoryName.lowercase().contains(q) ||
                            item.accountName.lowercase().contains(q) ||
                            item.paymentMethodName.lowercase().contains(q) ||
                            (item.expense.note?.lowercase()?.contains(q) == true)
                }
            }

            // 6. Sort Order
            result = when (filterState.sortOrder) {
                SortOrder.NEWEST -> result.sortedWith(compareByDescending<ExpenseWithDetails> { it.expense.expenseDate }.thenByDescending { it.expense.createdAt })
                SortOrder.OLDEST -> result.sortedWith(compareBy<ExpenseWithDetails> { it.expense.expenseDate }.thenBy { it.expense.createdAt })
                SortOrder.HIGHEST_AMOUNT -> result.sortedByDescending { it.expense.amount }
                SortOrder.LOWEST_AMOUNT -> result.sortedBy { it.expense.amount }
            }

            result
        }
    }

    override fun getReportData(period: ReportPeriod, accountId: Long?): Flow<ReportData> {
        return expenseDao.getAllExpensesWithDetails().map { allList ->
            val now = System.currentTimeMillis()
            val startMillis = when (period) {
                ReportPeriod.DAILY -> DateUtils.getTodayStartMillis()
                ReportPeriod.WEEKLY -> DateUtils.getStartOfWeekMillis()
                ReportPeriod.MONTHLY -> DateUtils.getStartOfMonthMillis()
                ReportPeriod.YEARLY -> DateUtils.getStartOfYearMillis()
            }

            val filteredList = allList.filter { item ->
                val matchesDate = item.expense.expenseDate in startMillis..now
                val matchesAccount = accountId == null || item.expense.accountId == accountId
                matchesDate && matchesAccount
            }

            val totalExpense = filteredList.sumOf { it.expense.amount }
            val count = filteredList.size
            val average = if (count > 0) totalExpense / count else 0L
            val highest = filteredList.maxOfOrNull { it.expense.amount } ?: 0L

            // Category breakdown
            val categoryMap = mutableMapOf<Long, MutableList<ExpenseWithDetails>>()
            filteredList.forEach { exp ->
                categoryMap.getOrPut(exp.expense.categoryId) { mutableListOf() }.add(exp)
            }

            val categoryItems = categoryMap.map { (catId, items) ->
                val catSum = items.sumOf { it.expense.amount }
                val catName = items.firstOrNull()?.categoryName ?: "Other"
                val catIcon = items.firstOrNull()?.category?.icon ?: "other"
                val percentage = if (totalExpense > 0L) (catSum.toFloat() / totalExpense.toFloat()) * 100f else 0f
                CategoryReportItem(
                    categoryId = catId,
                    categoryName = catName,
                    icon = catIcon,
                    totalAmount = catSum,
                    count = items.size,
                    percentage = percentage
                )
            }.sortedByDescending { it.totalAmount }

            // Time breakdown for bar chart
            val timeItems = generateTimeBuckets(filteredList, period)

            ReportData(
                period = period,
                totalExpense = totalExpense,
                numberOfExpenses = count,
                averageExpense = average,
                highestExpense = highest,
                categoryBreakdown = categoryItems,
                timeBreakdown = timeItems,
                selectedAccountId = accountId
            )
        }
    }

    private fun generateTimeBuckets(
        expenses: List<ExpenseWithDetails>,
        period: ReportPeriod
    ): List<TimeReportItem> {
        val calendar = Calendar.getInstance()
        return when (period) {
            ReportPeriod.DAILY -> {
                // Buckets by 4-hour intervals
                val intervals = listOf(
                    "Morning" to (6..11),
                    "Afternoon" to (12..16),
                    "Evening" to (17..20),
                    "Night" to (21..23)
                )
                intervals.map { (label, range) ->
                    val sum = expenses.filter { exp ->
                        calendar.timeInMillis = exp.expense.expenseDate
                        calendar.get(Calendar.HOUR_OF_DAY) in range
                    }.sumOf { it.expense.amount }
                    TimeReportItem(label = label, amount = sum, dateMillis = 0L)
                }
            }
            ReportPeriod.WEEKLY -> {
                // Buckets for 7 days of this week: Mon, Tue, Wed, Thu, Fri, Sat, Sun
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val cal = Calendar.getInstance()
                cal.firstDayOfWeek = Calendar.MONDAY
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)

                days.mapIndexed { index, dayName ->
                    val dayStart = cal.timeInMillis + (index * 24 * 3600 * 1000L)
                    val dayEnd = dayStart + (24 * 3600 * 1000L) - 1
                    val sum = expenses.filter { it.expense.expenseDate in dayStart..dayEnd }.sumOf { it.expense.amount }
                    TimeReportItem(label = dayName, amount = sum, dateMillis = dayStart)
                }
            }
            ReportPeriod.MONTHLY -> {
                // Buckets for 4 weeks of the month: W1, W2, W3, W4
                val weeks = listOf("Week 1", "Week 2", "Week 3", "Week 4")
                val startOfMonth = DateUtils.getStartOfMonthMillis()
                weeks.mapIndexed { index, wName ->
                    val wStart = startOfMonth + (index * 7L * 24 * 3600 * 1000L)
                    val wEnd = if (index == 3) startOfMonth + (31L * 24 * 3600 * 1000L) else wStart + (7L * 24 * 3600 * 1000L) - 1
                    val sum = expenses.filter { it.expense.expenseDate in wStart..wEnd }.sumOf { it.expense.amount }
                    TimeReportItem(label = wName, amount = sum, dateMillis = wStart)
                }
            }
            ReportPeriod.YEARLY -> {
                // Buckets for months of the year: Jan, Feb, Mar, ... Dec
                val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                val cal = Calendar.getInstance()
                months.mapIndexed { monthIdx, mName ->
                    val sum = expenses.filter { exp ->
                        cal.timeInMillis = exp.expense.expenseDate
                        cal.get(Calendar.MONTH) == monthIdx
                    }.sumOf { it.expense.amount }
                    TimeReportItem(label = mName, amount = sum, dateMillis = 0L)
                }
            }
        }
    }
}
