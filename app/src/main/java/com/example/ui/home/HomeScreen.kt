package com.example.ui.home

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExpenseWithDetails
import com.example.domain.MoneyFormatter
import com.example.ui.MainViewModel
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ExpenseDetailsDialog
import com.example.ui.components.ExpenseRowItem
import com.example.ui.components.StatCard
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueContainer
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onEditExpense: (ExpenseWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpensePaise.collectAsStateWithLifecycle()
    val todayExpense by viewModel.todayExpensePaise.collectAsStateWithLifecycle()
    val thisMonthExpense by viewModel.thisMonthExpensePaise.collectAsStateWithLifecycle()
    val recentExpenses by viewModel.recentExpenses.collectAsStateWithLifecycle()

    var selectedExpenseForDetails by remember { mutableStateOf<ExpenseWithDetails?>(null) }
    var expenseToDelete by remember { mutableStateOf<ExpenseWithDetails?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header: Nidhi, Good Morning, [User Name], Settings icon
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Nidhi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Good Morning, $userName",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .testTag("settings_icon_button")
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Main Card: TOTAL EXPENSE
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("total_expense_card")
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "TOTAL EXPENSE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = MoneyFormatter.formatPaise(totalExpense),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 3. Compact Summary Cards: Today and This Month
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Today",
                    amountPaise = todayExpense,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("today_stat_card")
                )
                StatCard(
                    title = "This Month",
                    amountPaise = thisMonthExpense,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("month_stat_card")
                )
            }
        }

        // 4. Quick Action "+ Add Expense"
        item {
            Button(
                onClick = onNavigateToAdd,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("home_add_expense_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+ Add Expense",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 5. Recent Expenses Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT EXPENSES",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                if (recentExpenses.isNotEmpty()) {
                    TextButton(
                        onClick = onNavigateToExpenses,
                        modifier = Modifier.testTag("view_all_expenses_button")
                    ) {
                        Text(
                            text = "View All Expenses",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 6. Recent 5 Expenses List or Empty State
        if (recentExpenses.isEmpty()) {
            item {
                EmptyStateView(
                    title = "No expenses yet",
                    subtitle = "Start tracking your spending.",
                    buttonText = "Add Expense",
                    onButtonClick = onNavigateToAdd
                )
            }
        } else {
            items(recentExpenses, key = { it.expense.expenseId }) { item ->
                ExpenseRowItem(
                    item = item,
                    onClick = { selectedExpenseForDetails = item }
                )
            }
        }
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
