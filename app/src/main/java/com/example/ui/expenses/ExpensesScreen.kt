package com.example.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExpenseWithDetails
import com.example.domain.DateUtils
import com.example.ui.MainViewModel
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ExpenseDetailsDialog
import com.example.ui.components.ExpenseRowItem

@Composable
fun ExpensesScreen(
    viewModel: MainViewModel,
    onNavigateToAdd: () -> Unit,
    onEditExpense: (ExpenseWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredExpenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val paymentMethods by viewModel.paymentMethods.collectAsStateWithLifecycle()

    var isSearchExpanded by remember { mutableStateOf(filterState.searchQuery.isNotBlank()) }
    var showFilterDialog by remember { mutableStateOf(false) }

    var selectedExpenseForDetails by remember { mutableStateOf<ExpenseWithDetails?>(null) }
    var expenseToDelete by remember { mutableStateOf<ExpenseWithDetails?>(null) }

    // Group expenses by date string (e.g. TODAY, YESTERDAY, 02 OCT 2026)
    val groupedExpenses = remember(filteredExpenses) {
        val groups = linkedMapOf<String, MutableList<ExpenseWithDetails>>()
        filteredExpenses.forEach { exp ->
            val header = DateUtils.formatDateGroupHeader(exp.expense.expenseDate)
            groups.getOrPut(header) { mutableListOf() }.add(exp)
        }
        groups
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // App Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Expenses",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Search toggle button
                IconButton(
                    onClick = {
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) {
                            viewModel.updateSearchQuery("")
                        }
                    },
                    modifier = Modifier
                        .testTag("search_toggle_button")
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isSearchExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (isSearchExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Filter button with badge if filter active
                IconButton(
                    onClick = { showFilterDialog = true },
                    modifier = Modifier
                        .testTag("filter_button")
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (filterState.isFiltered) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = if (filterState.isFiltered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Inline Search Bar
        if (isSearchExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = filterState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search title, category, account, note...") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (filterState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_text_field"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )
            }
        }

        // Active Filter indicator chip row
        if (filterState.isFiltered) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active filters applied",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                TextButton(onClick = { viewModel.resetFilters() }) {
                    Text("Clear", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Expense Grouped List
        if (filteredExpenses.isEmpty()) {
            EmptyStateView(
                title = if (filterState.isFiltered) "No matching expenses" else "No expenses yet",
                subtitle = if (filterState.isFiltered) "Try adjusting or clearing your filters." else "Start tracking your spending.",
                buttonText = if (!filterState.isFiltered) "Add Expense" else null,
                onButtonClick = onNavigateToAdd,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                groupedExpenses.forEach { (headerDate, itemsForDate) ->
                    item(key = "header_$headerDate") {
                        Text(
                            text = headerDate,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)
                        )
                    }

                    items(itemsForDate, key = { it.expense.expenseId }) { item ->
                        ExpenseRowItem(
                            item = item,
                            onClick = { selectedExpenseForDetails = item }
                        )
                    }
                }
            }
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            currentState = filterState,
            categories = categories,
            accounts = accounts,
            paymentMethods = paymentMethods,
            onApply = { newFilter ->
                viewModel.updateDateRangeFilter(newFilter.dateRange, newFilter.customStartDate, newFilter.customEndDate)
                viewModel.updateCategoryFilter(newFilter.categoryId)
                viewModel.updateAccountFilter(newFilter.accountId)
                viewModel.updatePaymentMethodFilter(newFilter.paymentMethodId)
                viewModel.updateSortOrder(newFilter.sortOrder)
                showFilterDialog = false
            },
            onReset = {
                viewModel.resetFilters()
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }

    // Detail Dialog
    selectedExpenseForDetails?.let { item ->
        ExpenseDetailsDialog(
            item = item,
            onEdit = {
                selectedExpenseForDetails = null
                onEditExpense(item)
            },
            onDelete = {
                selectedExpenseForDetails = null
                expenseToDelete = item
            },
            onDismiss = { selectedExpenseForDetails = null }
        )
    }

    // Delete Confirmation Dialog
    expenseToDelete?.let { item ->
        DeleteConfirmationDialog(
            item = item,
            onConfirm = {
                viewModel.deleteExpense(item.expense.expenseId)
                expenseToDelete = null
            },
            onDismiss = { expenseToDelete = null }
        )
    }
}
