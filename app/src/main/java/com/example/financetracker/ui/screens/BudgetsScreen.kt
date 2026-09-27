package com.example.financetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.financetracker.data.model.CategorySpending
import com.example.financetracker.data.model.MonthlySummary
import com.example.financetracker.ui.components.BudgetProgressCard
import com.example.financetracker.ui.components.EmptyStateView
import com.example.financetracker.ui.components.formatCurrency
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.WarningAmber
import java.util.Locale

@Composable
fun BudgetsScreen(
    monthlySummary: MonthlySummary,
    selectedMonth: String,
    availableMonths: List<String>,
    onSelectMonth: (String) -> Unit,
    onSetBudgetClick: () -> Unit,
    onEditBudget: (category: String, currentLimit: Double) -> Unit,
    onPopulateDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    var statusFilter by remember { mutableStateOf<String>("ALL") }

    val budgetedCategories = remember(monthlySummary, statusFilter) {
        val list = monthlySummary.categoryBreakdowns.filter { it.budgetLimit != null }
        when (statusFilter) {
            "OVER" -> list.filter { it.isOverBudget }
            "WARNING" -> list.filter { it.isNearBudgetLimit }
            "SAFE" -> list.filter { !it.isOverBudget && !it.isNearBudgetLimit }
            else -> list
        }
    }

    val totalBudget = monthlySummary.totalBudget
    val totalExpense = monthlySummary.totalExpense
    val remaining = totalBudget - totalExpense
    val percentUsed = if (totalBudget > 0) (totalExpense / totalBudget).toFloat().coerceIn(0f, 1f) else 0f

    Box(modifier = modifier.fillMaxSize().testTag("budgets_screen")) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Month Selector
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableMonths) { m ->
                        FilterChip(
                            selected = m == selectedMonth,
                            onClick = { onSelectMonth(m) },
                            label = { Text(formatMonthDisplay(m)) }
                        )
                    }
                }
            }

            // Overall Budget Health Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Monthly Budget Utilization",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${formatMonthDisplay(selectedMonth)} Overview",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Savings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { percentUsed },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = when {
                                totalExpense > totalBudget -> ExpenseRed
                                totalExpense >= totalBudget * 0.8 -> WarningAmber
                                else -> PrimaryGreen
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Spent",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatCurrency(totalExpense),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Total Budget",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatCurrency(totalBudget),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (remaining >= 0) "Remaining" else "Over Budget",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (remaining >= 0) PrimaryGreen else ExpenseRed
                                )
                                Text(
                                    text = formatCurrency(kotlin.math.abs(remaining)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remaining >= 0) PrimaryGreen else ExpenseRed
                                )
                            }
                        }
                    }
                }
            }

            // Filter Chips (All, Safe, Near Limit, Overbudget)
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = statusFilter == "ALL",
                        onClick = { statusFilter = "ALL" },
                        label = { Text("All (${monthlySummary.categoryBreakdowns.count { it.budgetLimit != null }})") }
                    )
                    FilterChip(
                        selected = statusFilter == "SAFE",
                        onClick = { statusFilter = "SAFE" },
                        label = { Text("Safe") }
                    )
                    FilterChip(
                        selected = statusFilter == "WARNING",
                        onClick = { statusFilter = "WARNING" },
                        label = { Text("Near Limit (${monthlySummary.warningBudgetCount})") }
                    )
                    FilterChip(
                        selected = statusFilter == "OVER",
                        onClick = { statusFilter = "OVER" },
                        label = { Text("Overbudget (${monthlySummary.overBudgetCount})") }
                    )
                }
            }

            // Budget list
            if (budgetedCategories.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Tune,
                        title = "No Budgets in this View",
                        description = if (statusFilter == "ALL") {
                            "You haven't set up any category budgets for this month yet. Setting budgets helps prevent overspending!"
                        } else {
                            "No category budgets match the \"$statusFilter\" status filter."
                        },
                        primaryButtonText = if (statusFilter == "ALL") "Add Category Budget" else "Show All Budgets",
                        onPrimaryClick = if (statusFilter == "ALL") onSetBudgetClick else { { statusFilter = "ALL" } },
                        secondaryButtonText = if (statusFilter == "ALL") "Load Default Category Budgets" else null,
                        onSecondaryClick = if (statusFilter == "ALL") onPopulateDefaults else null
                    )
                }
            } else {
                items(budgetedCategories, key = { it.categoryName }) { item ->
                    BudgetProgressCard(
                        categorySpending = item,
                        onEditBudget = {
                            onEditBudget(item.categoryName, item.budgetLimit ?: 0.0)
                        }
                    )
                }
            }
        }

        // Add Budget FAB
        FloatingActionButton(
            onClick = onSetBudgetClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .testTag("add_budget_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Set Category Budget")
        }
    }
}
