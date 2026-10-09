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

data class DaySpendingItem(
    val dayOfMonth: Int,
    val dayLabel: String,
    val dateMillis: Long,
    val totalPaise: Long,
    val count: Int
)

data class MonthSpendingItem(
    val monthIndex: Int,
    val monthName: String,
    val year: Int,
    val totalPaise: Long,
    val count: Int
)

data class MonthlyPatternState(
    val selectedYear: Int = 2026,
    val selectedMonthIndex: Int = 9, // 0-based, 9 = October
    val monthName: String = "October 2026",
    val totalMonthPaise: Long = 0L,
    val totalYearPaise: Long = 0L,
    val dailyItems: List<DaySpendingItem> = emptyList(),
    val monthlyItems: List<MonthSpendingItem> = emptyList(),
    val peakDay: DaySpendingItem? = null,
    val dailyAveragePaise: Long = 0L,
    val activeDaysCount: Int = 0
)
