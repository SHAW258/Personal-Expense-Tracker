package com.example.financetracker.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.financetracker.data.model.PredefinedCategories
import com.example.financetracker.data.model.Transaction
import com.example.financetracker.data.model.TransactionType
import com.example.financetracker.ui.components.EmptyStateView
import com.example.financetracker.ui.components.TransactionItemCard
import com.example.financetracker.ui.components.formatCurrency
import com.example.ui.theme.ExpenseRed

@Composable
fun TransactionsScreen(
    transactions: List<Transaction>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filterType: TransactionType?,
    onFilterTypeChange: (TransactionType?) -> Unit,
    filterCategory: String?,
    onFilterCategoryChange: (String?) -> Unit,
    sortBy: String,
    onSortByChange: (String) -> Unit,
    onClearFilters: () -> Unit,
    onAddTransaction: () -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    // Confirmation dialog before deleting
    transactionToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete Transaction?") },
            text = {
                Text("Are you sure you want to delete \"${tx.title}\" (${formatCurrency(tx.amount)})? This cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(tx)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize().testTag("transactions_screen")) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Search Input
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search by merchant, note, or category...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transactions_search_bar")
                )
            }

            // Filter Chips Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = filterType == null,
                        onClick = { onFilterTypeChange(null) },
                        label = { Text("All Types") }
                    )
                    FilterChip(
                        selected = filterType == TransactionType.EXPENSE,
                        onClick = { onFilterTypeChange(TransactionType.EXPENSE) },
                        label = { Text("Expenses") }
                    )
                    FilterChip(
                        selected = filterType == TransactionType.INCOME,
                        onClick = { onFilterTypeChange(TransactionType.INCOME) },
                        label = { Text("Income") }
                    )

                    // Category Filter Dropdown
                    Box {
                        FilterChip(
                            selected = filterCategory != null,
                            onClick = { categoryMenuExpanded = true },
                            label = { Text(filterCategory ?: "Category: All") }
                        )
                        DropdownMenu(
                            expanded = categoryMenuExpanded,
                            onDismissRequest = { categoryMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Categories") },
                                onClick = {
                                    onFilterCategoryChange(null)
                                    categoryMenuExpanded = false
                                }
                            )
                            PredefinedCategories.all.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        onFilterCategoryChange(cat.name)
                                        categoryMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Sort order Dropdown
                    Box {
                        FilterChip(
                            selected = true,
                            onClick = { sortMenuExpanded = true },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Sort,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = {
                                val sortLabel = when (sortBy) {
                                    "DATE_DESC" -> "Newest First"
                                    "DATE_ASC" -> "Oldest First"
                                    "AMOUNT_DESC" -> "Highest Amount"
                                    "AMOUNT_ASC" -> "Lowest Amount"
                                    else -> "Sort"
                                }
                                Text(sortLabel)
                            }
                        )
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Date (Newest First)") },
                                onClick = {
                                    onSortByChange("DATE_DESC")
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Date (Oldest First)") },
                                onClick = {
                                    onSortByChange("DATE_ASC")
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Amount (Highest First)") },
                                onClick = {
                                    onSortByChange("AMOUNT_DESC")
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Amount (Lowest First)") },
                                onClick = {
                                    onSortByChange("AMOUNT_ASC")
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Summary info row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${transactions.size} transactions found",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (searchQuery.isNotEmpty() || filterType != null || filterCategory != null) {
                        TextButton(onClick = onClearFilters) {
                            Text("Clear Filters")
                        }
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.ReceiptLong,
                        title = "No Transactions Found",
                        description = if (searchQuery.isNotEmpty() || filterCategory != null || filterType != null) {
                            "No transactions matched your current filters. Try changing or clearing them."
                        } else {
                            "You haven't recorded any transactions for this period yet."
                        },
                        primaryButtonText = if (searchQuery.isNotEmpty() || filterCategory != null || filterType != null) {
                            "Clear Filters"
                        } else {
                            "Add Transaction"
                        },
                        onPrimaryClick = if (searchQuery.isNotEmpty() || filterCategory != null || filterType != null) {
                            onClearFilters
                        } else {
                            onAddTransaction
                        }
                    )
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        onEdit = { onEditTransaction(tx) },
                        onDelete = { transactionToDelete = tx }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddTransaction,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .testTag("add_transaction_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction")
        }
    }
}
