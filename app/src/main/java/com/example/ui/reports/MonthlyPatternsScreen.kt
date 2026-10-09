package com.example.ui.reports

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DaySpendingItem
import com.example.data.model.MonthSpendingItem
import com.example.domain.MoneyFormatter
import com.example.ui.MainViewModel
import com.example.ui.components.NidhiCard
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.ElectricBlueVibrant
import com.example.ui.theme.ExpenseRed

@Composable
fun MonthlyPatternsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.monthlyPatternState.collectAsStateWithLifecycle()
    var selectedChartMode by remember { mutableIntStateOf(0) } // 0: Daily in Month, 1: Annual Months

    var activeDailyIndex by remember { mutableStateOf<Int?>(null) }
    var activeMonthlyIndex by remember { mutableStateOf<Int?>(null) }

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
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .testTag("monthly_back_button")
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Spending Patterns",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Interactive Monthly Expenses Bar Chart",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Month Navigator Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                activeDailyIndex = null
                                activeMonthlyIndex = null
                                viewModel.previousPatternMonth()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous Month",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.monthName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Total: ${MoneyFormatter.formatPaise(state.totalMonthPaise)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = {
                                activeDailyIndex = null
                                activeMonthlyIndex = null
                                viewModel.nextPatternMonth()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next Month",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 2. View Mode Toggle (Daily vs Annual)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedChartMode == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                selectedChartMode = 0
                                activeDailyIndex = null
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Daily Breakdown",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedChartMode == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedChartMode == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                selectedChartMode = 1
                                activeMonthlyIndex = null
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "12-Month Year View",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedChartMode == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 3. Recharts-style Interactive Bar Chart Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            RoundedCornerShape(20.dp)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedChartMode == 0) "Daily Spending Pattern" else "Annual Monthly Curve",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Tap bar for details",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (selectedChartMode == 0) {
                            RechartsDailyBarChart(
                                items = state.dailyItems,
                                activeIndex = activeDailyIndex,
                                onSelectIndex = { activeDailyIndex = it }
                            )
                        } else {
                            RechartsAnnualBarChart(
                                items = state.monthlyItems,
                                selectedMonthIndex = state.selectedMonthIndex,
                                activeIndex = activeMonthlyIndex,
                                onSelectIndex = {
                                    activeMonthlyIndex = it
                                    viewModel.setPatternMonth(state.selectedYear, it)
                                }
                            )
                        }
                    }
                }
            }

            // 4. Pattern Insights & Metrics
            item {
                Text(
                    text = "SPENDING PATTERN INSIGHTS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InsightCard(
                        title = "Peak Spend Day",
                        value = state.peakDay?.let { "Day ${it.dayOfMonth}" } ?: "None",
                        sub = state.peakDay?.let { MoneyFormatter.formatPaise(it.totalPaise) } ?: "₹0",
                        accentColor = ExpenseRed,
                        modifier = Modifier.weight(1f)
                    )

                    InsightCard(
                        title = "Daily Average",
                        value = MoneyFormatter.formatPaise(state.dailyAveragePaise),
                        sub = "across ${state.dailyItems.size} days",
                        accentColor = ElectricBlueLight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InsightCard(
                        title = "Active Spend Days",
                        value = "${state.activeDaysCount} Days",
                        sub = "${state.dailyItems.size - state.activeDaysCount} zero-spend days",
                        accentColor = AccentCyan,
                        modifier = Modifier.weight(1f)
                    )

                    InsightCard(
                        title = "Yearly Total",
                        value = MoneyFormatter.formatPaise(state.totalYearPaise),
                        sub = "in ${state.selectedYear}",
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RechartsDailyBarChart(
    items: List<DaySpendingItem>,
    activeIndex: Int?,
    onSelectIndex: (Int) -> Unit
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No data", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxAmount = items.maxOfOrNull { it.totalPaise }?.coerceAtLeast(100L) ?: 100L
    val scrollState = rememberScrollState()

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(items) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Active selection Tooltip Card (Recharts style floating pill)
        activeIndex?.let { idx ->
            val selected = items.getOrNull(idx)
            if (selected != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(
                            1.dp,
                            ElectricBlueLight.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Day ${selected.dayOfMonth}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${selected.count} transactions recorded",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = MoneyFormatter.formatPaise(selected.totalPaise),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (selected.totalPaise > 0) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Scrollable horizontal container for 28-31 daily bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = activeIndex == index
                val hasSpend = item.totalPaise > 0
                val ratio = (item.totalPaise.toFloat() / maxAmount.toFloat()).coerceIn(if (hasSpend) 0.08f else 0.02f, 1f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .clickable { onSelectIndex(index) }
                        .padding(horizontal = 2.dp)
                ) {
                    // Bar
                    Canvas(
                        modifier = Modifier
                            .width(16.dp)
                            .height(180.dp)
                    ) {
                        val barHeight = size.height * ratio * animProgress.value
                        val barWidth = size.width

                        // Subtle background track
                        drawRoundRect(
                            color = Color(0xFF162544).copy(alpha = 0.5f),
                            topLeft = Offset(0f, 0f),
                            size = Size(barWidth, size.height),
                            cornerRadius = CornerRadius(6f, 6f)
                        )

                        // Recharts gradient fill
                        val gradientBrush = if (isSelected) {
                            Brush.verticalGradient(listOf(Color(0xFF60A5FA), Color(0xFF2563EB)))
                        } else if (hasSpend) {
                            Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF1D4ED8)))
                        } else {
                            Brush.verticalGradient(listOf(Color(0xFF253454), Color(0xFF18233C)))
                        }

                        drawRoundRect(
                            brush = gradientBrush,
                            topLeft = Offset(0f, size.height - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) ElectricBlueLight else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RechartsAnnualBarChart(
    items: List<MonthSpendingItem>,
    selectedMonthIndex: Int,
    activeIndex: Int?,
    onSelectIndex: (Int) -> Unit
) {
    if (items.isEmpty()) return

    val maxAmount = items.maxOfOrNull { it.totalPaise }?.coerceAtLeast(100L) ?: 100L
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(items) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            items.forEachIndexed { index, item ->
                val isCurrentSelectedMonth = selectedMonthIndex == index
                val hasSpend = item.totalPaise > 0
                val ratio = (item.totalPaise.toFloat() / maxAmount.toFloat()).coerceIn(if (hasSpend) 0.08f else 0.02f, 1f)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectIndex(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Canvas(
                        modifier = Modifier
                            .width(18.dp)
                            .height(150.dp)
                    ) {
                        val barHeight = size.height * ratio * animProgress.value
                        val barWidth = size.width

                        // Background track
                        drawRoundRect(
                            color = Color(0xFF162544).copy(alpha = 0.5f),
                            topLeft = Offset(0f, 0f),
                            size = Size(barWidth, size.height),
                            cornerRadius = CornerRadius(8f, 8f)
                        )

                        // Bar fill
                        val gradientBrush = if (isCurrentSelectedMonth) {
                            Brush.verticalGradient(listOf(Color(0xFF60A5FA), Color(0xFF2563EB)))
                        } else if (hasSpend) {
                            Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF1E3A8A)))
                        } else {
                            Brush.verticalGradient(listOf(Color(0xFF253454), Color(0xFF1B2B4C)))
                        }

                        drawRoundRect(
                            brush = gradientBrush,
                            topLeft = Offset(0f, size.height - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.monthName,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = if (isCurrentSelectedMonth) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrentSelectedMonth) ElectricBlueLight else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightCard(
    title: String,
    value: String,
    sub: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
            RoundedCornerShape(14.dp)
        ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sub,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
