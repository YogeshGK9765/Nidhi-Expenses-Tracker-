package com.example.ui.add

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PaymentMethodEntity
import com.example.data.local.entity.SubcategoryEntity
import com.example.data.model.ExpenseWithDetails
import com.example.domain.DateUtils
import com.example.domain.MoneyFormatter
import com.example.ui.MainViewModel
import com.example.ui.components.NidhiIcons
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ExpenseRed
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    viewModel: MainViewModel,
    existingExpense: ExpenseWithDetails? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val paymentMethods by viewModel.paymentMethods.collectAsStateWithLifecycle()
    val subcategories by viewModel.subcategoriesForSelectedCategory.collectAsStateWithLifecycle()
    val defaultAccountId by viewModel.defaultAccountId.collectAsStateWithLifecycle()

    val isEditing = existingExpense != null

    // Form States
    var amountInput by remember {
        mutableStateOf(
            if (existingExpense != null) {
                val rupees = existingExpense.expense.amount / 100L
                val paise = existingExpense.expense.amount % 100L
                if (paise > 0L) "$rupees.${paise.toString().padStart(2, '0')}" else rupees.toString()
            } else ""
        )
    }

    var selectedCategoryId by remember {
        mutableStateOf<Long?>(existingExpense?.expense?.categoryId)
    }

    var selectedSubcategoryId by remember {
        mutableStateOf<Long?>(existingExpense?.expense?.subcategoryId)
    }

    var selectedAccountId by remember {
        mutableStateOf<Long?>(existingExpense?.expense?.accountId)
    }

    var selectedPaymentMethodId by remember {
        mutableStateOf<Long?>(existingExpense?.expense?.paymentMethodId)
    }

    var expenseDateMillis by remember {
        mutableLongStateOf(existingExpense?.expense?.expenseDate ?: System.currentTimeMillis())
    }

    var expenseTimeText by remember {
        mutableStateOf(existingExpense?.expense?.expenseTime ?: DateUtils.getCurrentTimeFormatted())
    }

    var noteText by remember {
        mutableStateOf(existingExpense?.expense?.note ?: "")
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // Dropdown expanded states
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var subcategoryMenuExpanded by remember { mutableStateOf(false) }
    var accountMenuExpanded by remember { mutableStateOf(false) }
    var paymentMethodMenuExpanded by remember { mutableStateOf(false) }

    // Initialize defaults if adding new
    LaunchedEffect(categories, accounts, paymentMethods, defaultAccountId) {
        if (!isEditing) {
            if (selectedCategoryId == null && categories.isNotEmpty()) {
                val defaultCat = categories.find { it.name.equals("Food", ignoreCase = true) } ?: categories.first()
                selectedCategoryId = defaultCat.categoryId
            }
            if (selectedAccountId == null) {
                selectedAccountId = defaultAccountId ?: accounts.find { it.name.equals("SBI", ignoreCase = true) }?.accountId ?: accounts.firstOrNull()?.accountId
            }
            if (selectedPaymentMethodId == null && paymentMethods.isNotEmpty()) {
                val defaultMethod = paymentMethods.find { it.name.equals("UPI", ignoreCase = true) } ?: paymentMethods.first()
                selectedPaymentMethodId = defaultMethod.paymentMethodId
            }
        }
    }

    // Refresh subcategories when selected category changes
    LaunchedEffect(selectedCategoryId) {
        viewModel.setSelectedCategoryForSubcategories(selectedCategoryId)
    }

    // Auto-select first subcategory if not set or if editing
    LaunchedEffect(subcategories) {
        if (selectedSubcategoryId == null && subcategories.isNotEmpty() && !isEditing) {
            val defaultSub = subcategories.find { it.name.equals("Canteen", ignoreCase = true) } ?: subcategories.first()
            selectedSubcategoryId = defaultSub.subcategoryId
        }
    }

    val selectedCategory = categories.find { it.categoryId == selectedCategoryId }
    val selectedSubcategory = subcategories.find { it.subcategoryId == selectedSubcategoryId }
    val selectedAccount = accounts.find { it.accountId == selectedAccountId }
    val selectedPaymentMethod = paymentMethods.find { it.paymentMethodId == selectedPaymentMethodId }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (isEditing) "Edit Expense" else "Add Expense",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Error Banner if validation fails
            errorMessage?.let { error ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // 1. Amount Field
            Text(
                text = "Amount",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = amountInput,
                onValueChange = { input ->
                    // Allow only digits and at most one decimal point
                    if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1) {
                        amountInput = input
                        errorMessage = null
                    }
                },
                placeholder = { Text("0", style = MaterialTheme.typography.displayMedium) },
                prefix = {
                    Text(
                        text = "₹ ",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                textStyle = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )

            // Quick Add Amount Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(50L, 100L, 200L, 500L).forEach { quickAmt ->
                    QuickAmountChip(
                        amount = quickAmt,
                        onClick = {
                            val currentPaise = MoneyFormatter.parseInputToPaise(amountInput)
                            val newPaise = currentPaise + (quickAmt * 100L)
                            val rupees = newPaise / 100L
                            val paise = newPaise % 100L
                            amountInput = if (paise > 0L) "$rupees.${paise.toString().padStart(2, '0')}" else rupees.toString()
                        }
                    )
                }
            }

            // 2. Category Selector
            SelectorField(
                label = "Category",
                value = selectedCategory?.name ?: "Select Category",
                icon = selectedCategory?.let { NidhiIcons.getCategoryIcon(it.icon) },
                onClick = { categoryMenuExpanded = true },
                tag = "category_selector"
            ) {
                DropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = NidhiIcons.getCategoryIcon(cat.icon),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (cat.categoryId == selectedCategoryId) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            onClick = {
                                selectedCategoryId = cat.categoryId
                                selectedSubcategoryId = null
                                categoryMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // 3. Subcategory Selector
            SelectorField(
                label = "Subcategory",
                value = selectedSubcategory?.name ?: if (subcategories.isEmpty()) "None" else "Select Subcategory",
                onClick = { subcategoryMenuExpanded = true },
                tag = "subcategory_selector"
            ) {
                DropdownMenu(
                    expanded = subcategoryMenuExpanded,
                    onDismissRequest = { subcategoryMenuExpanded = false }
                ) {
                    subcategories.forEach { sub ->
                        DropdownMenuItem(
                            text = { Text(sub.name) },
                            trailingIcon = {
                                if (sub.subcategoryId == selectedSubcategoryId) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            onClick = {
                                selectedSubcategoryId = sub.subcategoryId
                                subcategoryMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // 4. Account Selector (Default SBI)
            SelectorField(
                label = "Account",
                value = selectedAccount?.name ?: "SBI",
                icon = selectedAccount?.let { NidhiIcons.getAccountIcon(it.name, it.type) },
                onClick = { accountMenuExpanded = true },
                tag = "account_selector"
            ) {
                DropdownMenu(
                    expanded = accountMenuExpanded,
                    onDismissRequest = { accountMenuExpanded = false }
                ) {
                    accounts.forEach { acc ->
                        DropdownMenuItem(
                            text = { Text(acc.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = NidhiIcons.getAccountIcon(acc.name, acc.type),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (acc.accountId == selectedAccountId) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            onClick = {
                                selectedAccountId = acc.accountId
                                accountMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // 5. Payment Method Selector (Default UPI)
            SelectorField(
                label = "Payment Method",
                value = selectedPaymentMethod?.name ?: "UPI",
                icon = selectedPaymentMethod?.let { NidhiIcons.getPaymentMethodIcon(it.name) },
                onClick = { paymentMethodMenuExpanded = true },
                tag = "payment_method_selector"
            ) {
                DropdownMenu(
                    expanded = paymentMethodMenuExpanded,
                    onDismissRequest = { paymentMethodMenuExpanded = false }
                ) {
                    paymentMethods.forEach { pm ->
                        DropdownMenuItem(
                            text = { Text(pm.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = NidhiIcons.getPaymentMethodIcon(pm.name),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (pm.paymentMethodId == selectedPaymentMethodId) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            onClick = {
                                selectedPaymentMethodId = pm.paymentMethodId
                                paymentMethodMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // 6. Date & Time Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date Picker trigger
                Box(modifier = Modifier.weight(1f)) {
                    SelectorField(
                        label = "Date",
                        value = DateUtils.formatShortDate(expenseDateMillis),
                        icon = Icons.Default.CalendarToday,
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = expenseDateMillis }
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    }
                                    expenseDateMillis = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        tag = "date_picker_button"
                    )
                }

                // Time Picker trigger
                Box(modifier = Modifier.weight(1f)) {
                    SelectorField(
                        label = "Time",
                        value = expenseTimeText,
                        icon = Icons.Default.AccessTime,
                        onClick = {
                            val cal = Calendar.getInstance()
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    val isAm = hourOfDay < 12
                                    val hour12 = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                                    val amPm = if (isAm) "AM" else "PM"
                                    expenseTimeText = String.format("%02d:%02d %s", hour12, minute, amPm)
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                false
                            ).show()
                        },
                        tag = "time_picker_button"
                    )
                }
            }

            // 7. Description Field
            Text(
                text = "Description",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = { Text("Enter description or note (optional)") },
                label = { Text("Description") },
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("description_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 8. Save Expense Button
            Button(
                onClick = {
                    val paise = MoneyFormatter.parseInputToPaise(amountInput)
                    if (paise <= 0L) {
                        errorMessage = "Enter an amount."
                        return@Button
                    }
                    val catId = selectedCategoryId
                    if (catId == null || catId <= 0L) {
                        errorMessage = "Select a category."
                        return@Button
                    }
                    val accId = selectedAccountId
                    if (accId == null || accId <= 0L) {
                        errorMessage = "Select an account."
                        return@Button
                    }

                    if (isSaving) return@Button
                    isSaving = true

                    val titleToUse = selectedSubcategory?.name
                        ?: selectedCategory?.name
                        ?: "Expense"

                    coroutineScope.launch {
                        val result = viewModel.saveExpense(
                            expenseId = existingExpense?.expense?.expenseId ?: 0L,
                            amountPaise = paise,
                            title = titleToUse,
                            categoryId = catId,
                            subcategoryId = selectedSubcategoryId,
                            accountId = accId,
                            paymentMethodId = selectedPaymentMethodId,
                            note = noteText,
                            expenseDate = expenseDateMillis,
                            expenseTime = expenseTimeText
                        )

                        isSaving = false
                        if (result.isSuccess) {
                            onNavigateBack()
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to save expense."
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_expense_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (isEditing) "Update Expense" else "Save Expense",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}

@Composable
private fun QuickAmountChip(
    amount: Long,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+₹$amount",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SelectorField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
    tag: String,
    content: @Composable () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 14.dp)
                .testTag(tag)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            content()
        }
    }
}
