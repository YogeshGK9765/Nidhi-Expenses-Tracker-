package com.example.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PaymentMethodEntity
import com.example.data.model.FilterDateRange
import com.example.data.model.FilterState
import com.example.data.model.SortOrder

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    currentState: FilterState,
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity>,
    paymentMethods: List<PaymentMethodEntity>,
    onApply: (FilterState) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDateRange by remember { mutableStateOf(currentState.dateRange) }
    var selectedCategoryId by remember { mutableStateOf(currentState.categoryId) }
    var selectedAccountId by remember { mutableStateOf(currentState.accountId) }
    var selectedPaymentMethodId by remember { mutableStateOf(currentState.paymentMethodId) }
    var selectedSortOrder by remember { mutableStateOf(currentState.sortOrder) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Expenses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = {
                        selectedDateRange = FilterDateRange.ALL
                        selectedCategoryId = null
                        selectedAccountId = null
                        selectedPaymentMethodId = null
                        selectedSortOrder = SortOrder.NEWEST
                        onReset()
                    }) {
                        Text("Reset All")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Date Filters
                SectionTitle(title = "DATE")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateChip(
                        title = "All",
                        selected = selectedDateRange == FilterDateRange.ALL,
                        onClick = { selectedDateRange = FilterDateRange.ALL }
                    )
                    DateChip(
                        title = "Today",
                        selected = selectedDateRange == FilterDateRange.TODAY,
                        onClick = { selectedDateRange = FilterDateRange.TODAY }
                    )
                    DateChip(
                        title = "This Week",
                        selected = selectedDateRange == FilterDateRange.THIS_WEEK,
                        onClick = { selectedDateRange = FilterDateRange.THIS_WEEK }
                    )
                    DateChip(
                        title = "This Month",
                        selected = selectedDateRange == FilterDateRange.THIS_MONTH,
                        onClick = { selectedDateRange = FilterDateRange.THIS_MONTH }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sort By
                SectionTitle(title = "SORT BY")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SortChip(
                        title = "Newest",
                        selected = selectedSortOrder == SortOrder.NEWEST,
                        onClick = { selectedSortOrder = SortOrder.NEWEST }
                    )
                    SortChip(
                        title = "Oldest",
                        selected = selectedSortOrder == SortOrder.OLDEST,
                        onClick = { selectedSortOrder = SortOrder.OLDEST }
                    )
                    SortChip(
                        title = "Highest Amount",
                        selected = selectedSortOrder == SortOrder.HIGHEST_AMOUNT,
                        onClick = { selectedSortOrder = SortOrder.HIGHEST_AMOUNT }
                    )
                    SortChip(
                        title = "Lowest Amount",
                        selected = selectedSortOrder == SortOrder.LOWEST_AMOUNT,
                        onClick = { selectedSortOrder = SortOrder.LOWEST_AMOUNT }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category
                SectionTitle(title = "CATEGORY")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateChip(
                        title = "All Categories",
                        selected = selectedCategoryId == null,
                        onClick = { selectedCategoryId = null }
                    )
                    categories.forEach { cat ->
                        DateChip(
                            title = cat.name,
                            selected = selectedCategoryId == cat.categoryId,
                            onClick = {
                                selectedCategoryId = if (selectedCategoryId == cat.categoryId) null else cat.categoryId
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Account
                SectionTitle(title = "ACCOUNT")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateChip(
                        title = "All Accounts",
                        selected = selectedAccountId == null,
                        onClick = { selectedAccountId = null }
                    )
                    accounts.forEach { acc ->
                        DateChip(
                            title = acc.name,
                            selected = selectedAccountId == acc.accountId,
                            onClick = {
                                selectedAccountId = if (selectedAccountId == acc.accountId) null else acc.accountId
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Method
                SectionTitle(title = "PAYMENT METHOD")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateChip(
                        title = "All Methods",
                        selected = selectedPaymentMethodId == null,
                        onClick = { selectedPaymentMethodId = null }
                    )
                    paymentMethods.forEach { pm ->
                        DateChip(
                            title = pm.name,
                            selected = selectedPaymentMethodId == pm.paymentMethodId,
                            onClick = {
                                selectedPaymentMethodId = if (selectedPaymentMethodId == pm.paymentMethodId) null else pm.paymentMethodId
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val newFilter = currentState.copy(
                                dateRange = selectedDateRange,
                                categoryId = selectedCategoryId,
                                accountId = selectedAccountId,
                                paymentMethodId = selectedPaymentMethodId,
                                sortOrder = selectedSortOrder
                            )
                            onApply(newFilter)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("apply_filter_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Apply", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun DateChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
private fun SortChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    DateChip(title = title, selected = selected, onClick = onClick)
}
