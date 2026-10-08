package com.example.data.model

enum class FilterDateRange {
    ALL,
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    CUSTOM
}

enum class SortOrder {
    NEWEST,
    OLDEST,
    HIGHEST_AMOUNT,
    LOWEST_AMOUNT
}

data class FilterState(
    val dateRange: FilterDateRange = FilterDateRange.ALL,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val paymentMethodId: Long? = null,
    val sortOrder: SortOrder = SortOrder.NEWEST,
    val searchQuery: String = ""
) {
    val isFiltered: Boolean
        get() = dateRange != FilterDateRange.ALL ||
                categoryId != null ||
                accountId != null ||
                paymentMethodId != null ||
                sortOrder != SortOrder.NEWEST ||
                searchQuery.isNotBlank()
}

enum class ReportPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

data class CategoryReportItem(
    val categoryId: Long,
    val categoryName: String,
    val icon: String,
    val totalAmount: Long, // in paise
    val count: Int,
    val percentage: Float
)

data class TimeReportItem(
    val label: String,
    val amount: Long, // in paise
    val dateMillis: Long
)

data class ReportData(
    val period: ReportPeriod = ReportPeriod.MONTHLY,
    val totalExpense: Long = 0L,
    val numberOfExpenses: Int = 0,
    val averageExpense: Long = 0L,
    val highestExpense: Long = 0L,
    val categoryBreakdown: List<CategoryReportItem> = emptyList(),
    val timeBreakdown: List<TimeReportItem> = emptyList(),
    val selectedAccountId: Long? = null // null means All Accounts
)

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}
