package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.PaymentMethodEntity
import com.example.data.local.entity.SubcategoryEntity
import com.example.data.model.ExpenseWithDetails
import com.example.data.model.FilterDateRange
import com.example.data.model.FilterState
import com.example.data.model.ReportData
import com.example.data.model.ReportPeriod
import com.example.data.model.SortOrder
import com.example.data.model.ThemeMode
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    // Total expenses
    val totalExpensePaise: StateFlow<Long> = repository.getTotalExpensePaise()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val todayExpensePaise: StateFlow<Long> = repository.getTodayExpensePaise()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val thisMonthExpensePaise: StateFlow<Long> = repository.getThisMonthExpensePaise()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // Recent 5 expenses
    val recentExpenses: StateFlow<List<ExpenseWithDetails>> = repository.getRecentExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Master lists
    val categories: StateFlow<List<CategoryEntity>> = repository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<AccountEntity>> = repository.getAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentMethods: StateFlow<List<PaymentMethodEntity>> = repository.getPaymentMethods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filters and Search
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredExpenses: StateFlow<List<ExpenseWithDetails>> = _filterState
        .flatMapLatest { filter -> repository.getFilteredExpenses(filter) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Subcategories cache for a selected category
    private val _selectedCategoryIdForSubs = MutableStateFlow<Long?>(null)
    @OptIn(ExperimentalCoroutinesApi::class)
    val subcategoriesForSelectedCategory: StateFlow<List<SubcategoryEntity>> = _selectedCategoryIdForSubs
        .flatMapLatest { catId ->
            if (catId != null) repository.getSubcategories(catId)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reports
    private val _reportPeriod = MutableStateFlow(ReportPeriod.MONTHLY)
    val reportPeriod: StateFlow<ReportPeriod> = _reportPeriod.asStateFlow()

    private val _reportAccountId = MutableStateFlow<Long?>(null) // null = All accounts
    val reportAccountId: StateFlow<Long?> = _reportAccountId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val reportData: StateFlow<ReportData> = combine(
        _reportPeriod,
        _reportAccountId
    ) { period, accId ->
        Pair(period, accId)
    }.flatMapLatest { (period, accId) ->
        repository.getReportData(period, accId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportData())

    // User Settings
    private val _themeMode = MutableStateFlow(ThemeMode.DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _userName = MutableStateFlow("Alex")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _defaultAccountId = MutableStateFlow<Long?>(null)
    val defaultAccountId: StateFlow<Long?> = _defaultAccountId.asStateFlow()

    // Prevent duplicate saves on rapid button clicks
    private var isSaving = false

    init {
        // Find default account (SBI)
        viewModelScope.launch {
            repository.getAccounts().collect { accs ->
                if (_defaultAccountId.value == null && accs.isNotEmpty()) {
                    val sbi = accs.find { it.name.equals("SBI", ignoreCase = true) } ?: accs.first()
                    _defaultAccountId.value = sbi.accountId
                }
            }
        }
    }

    fun setSelectedCategoryForSubcategories(categoryId: Long?) {
        _selectedCategoryIdForSubs.value = categoryId
    }

    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun updateDateRangeFilter(range: FilterDateRange, customStart: Long? = null, customEnd: Long? = null) {
        _filterState.value = _filterState.value.copy(
            dateRange = range,
            customStartDate = customStart,
            customEndDate = customEnd
        )
    }

    fun updateCategoryFilter(categoryId: Long?) {
        _filterState.value = _filterState.value.copy(categoryId = categoryId)
    }

    fun updateAccountFilter(accountId: Long?) {
        _filterState.value = _filterState.value.copy(accountId = accountId)
    }

    fun updatePaymentMethodFilter(methodId: Long?) {
        _filterState.value = _filterState.value.copy(paymentMethodId = methodId)
    }

    fun updateSortOrder(order: SortOrder) {
        _filterState.value = _filterState.value.copy(sortOrder = order)
    }

    fun resetFilters() {
        _filterState.value = FilterState()
    }

    fun setReportPeriod(period: ReportPeriod) {
        _reportPeriod.value = period
    }

    fun setReportAccountId(accountId: Long?) {
        _reportAccountId.value = accountId
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun setUserName(name: String) {
        _userName.value = name
    }

    fun setDefaultAccountId(accountId: Long) {
        _defaultAccountId.value = accountId
    }

    suspend fun saveExpense(
        expenseId: Long = 0L,
        amountPaise: Long,
        title: String,
        categoryId: Long,
        subcategoryId: Long?,
        accountId: Long,
        paymentMethodId: Long?,
        note: String?,
        expenseDate: Long,
        expenseTime: String
    ): Result<Unit> {
        if (isSaving) return Result.failure(Exception("Save in progress"))
        if (amountPaise <= 0L) {
            return Result.failure(IllegalArgumentException("Enter an amount."))
        }
        if (categoryId <= 0L) {
            return Result.failure(IllegalArgumentException("Select a category."))
        }
        if (accountId <= 0L) {
            return Result.failure(IllegalArgumentException("Select an account."))
        }
        if (expenseDate <= 0L) {
            return Result.failure(IllegalArgumentException("Select a date."))
        }

        isSaving = true
        return try {
            val expenseEntity = ExpenseEntity(
                expenseId = expenseId,
                amount = amountPaise,
                title = title.ifBlank { "Expense" },
                categoryId = categoryId,
                subcategoryId = subcategoryId,
                accountId = accountId,
                paymentMethodId = paymentMethodId,
                note = note?.trim()?.ifBlank { null },
                expenseDate = expenseDate,
                expenseTime = expenseTime.ifBlank { "12:00 PM" },
                updatedAt = System.currentTimeMillis()
            )

            if (expenseId > 0L) {
                repository.updateExpense(expenseEntity)
            } else {
                repository.insertExpense(expenseEntity)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            isSaving = false
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteExpenseById(id)
        }
    }

    fun addCategory(name: String, icon: String = "other") {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.insertCategory(CategoryEntity(name = name.trim(), icon = icon, isDefault = false))
            }
        }
    }

    fun addSubcategory(categoryId: Long, name: String) {
        viewModelScope.launch {
            if (name.isNotBlank() && categoryId > 0L) {
                repository.insertSubcategory(SubcategoryEntity(categoryId = categoryId, name = name.trim()))
            }
        }
    }

    fun addAccount(name: String, type: String = "Bank") {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.insertAccount(AccountEntity(name = name.trim(), type = type))
            }
        }
    }

    fun addPaymentMethod(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.insertPaymentMethod(PaymentMethodEntity(name = name.trim()))
            }
        }
    }
}

class MainViewModelFactory(
    private val repository: ExpenseRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
