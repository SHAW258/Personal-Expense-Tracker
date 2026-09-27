package com.example.financetracker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financetracker.data.model.Transaction
import com.example.financetracker.data.model.TransactionType
import com.example.financetracker.ui.dialogs.AddEditTransactionDialog
import com.example.financetracker.ui.dialogs.SetBudgetDialog
import com.example.financetracker.ui.screens.AnalyticsScreen
import com.example.financetracker.ui.screens.BudgetsScreen
import com.example.financetracker.ui.screens.DashboardScreen
import com.example.financetracker.ui.screens.ProjectSpecScreen
import com.example.financetracker.ui.screens.TransactionsScreen
import com.example.financetracker.viewmodel.FinanceViewModel
import kotlinx.coroutines.flow.collectLatest

enum class FinanceScreen(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    TRANSACTIONS("Transactions", Icons.Default.ReceiptLong),
    BUDGETS("Budgets", Icons.Default.Tune),
    ANALYTICS("Analytics", Icons.Default.PieChart),
    SPEC_REVIEW("Spec & Audit", Icons.Default.Assignment)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceApp(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(FinanceScreen.DASHBOARD) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog states
    var showAddEditDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    var defaultTypeForAdd by remember { mutableStateOf(TransactionType.EXPENSE) }

    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var budgetCategoryToEdit by remember { mutableStateOf<String?>(null) }
    var budgetLimitToEdit by remember { mutableStateOf<Double?>(null) }

    // ViewModel states
    val monthlySummary by viewModel.monthlySummary.collectAsStateWithLifecycle()
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val availableMonths by viewModel.allRecordedMonths.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()
    val filterCategory by viewModel.filterCategory.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()

    // Listen to ViewModel snackbar events
    LaunchedEffect(viewModel) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // BackHandler: return to Dashboard if on another screen
    if (currentScreen != FinanceScreen.DASHBOARD) {
        BackHandler {
            currentScreen = FinanceScreen.DASHBOARD
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("finance_app_scaffold"),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentScreen) {
                            FinanceScreen.DASHBOARD -> "Personal Finance"
                            FinanceScreen.TRANSACTIONS -> "Transactions"
                            FinanceScreen.BUDGETS -> "Monthly Budgets"
                            FinanceScreen.ANALYTICS -> "Analytics & Reports"
                            FinanceScreen.SPEC_REVIEW -> "Project Spec & Audit"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.seedDemoData() },
                        modifier = Modifier.testTag("topbar_seed_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Simulate Demo Data",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                FinanceScreen.entries.forEach { screen ->
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label) },
                        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                FinanceScreen.DASHBOARD -> {
                    DashboardScreen(
                        monthlySummary = monthlySummary,
                        recentTransactions = transactions,
                        selectedMonth = selectedMonth,
                        availableMonths = availableMonths,
                        onSelectMonth = { viewModel.setSelectedMonth(it) },
                        onAddExpense = {
                            defaultTypeForAdd = TransactionType.EXPENSE
                            transactionToEdit = null
                            showAddEditDialog = true
                        },
                        onAddIncome = {
                            defaultTypeForAdd = TransactionType.INCOME
                            transactionToEdit = null
                            showAddEditDialog = true
                        },
                        onSetBudget = {
                            budgetCategoryToEdit = null
                            budgetLimitToEdit = null
                            showSetBudgetDialog = true
                        },
                        onViewAllTransactions = {
                            currentScreen = FinanceScreen.TRANSACTIONS
                        },
                        onEditTransaction = { tx ->
                            transactionToEdit = tx
                            showAddEditDialog = true
                        },
                        onDeleteTransaction = { tx ->
                            viewModel.deleteTransaction(tx)
                        },
                        onSeedDemoData = {
                            viewModel.seedDemoData()
                        },
                        onEditBudget = { cat, limit ->
                            budgetCategoryToEdit = cat
                            budgetLimitToEdit = limit
                            showSetBudgetDialog = true
                        }
                    )
                }

                FinanceScreen.TRANSACTIONS -> {
                    TransactionsScreen(
                        transactions = transactions,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        filterType = filterType,
                        onFilterTypeChange = { viewModel.setFilterType(it) },
                        filterCategory = filterCategory,
                        onFilterCategoryChange = { viewModel.setFilterCategory(it) },
                        sortBy = sortBy,
                        onSortByChange = { viewModel.setSortBy(it) },
                        onClearFilters = { viewModel.clearFilters() },
                        onAddTransaction = {
                            transactionToEdit = null
                            showAddEditDialog = true
                        },
                        onEditTransaction = { tx ->
                            transactionToEdit = tx
                            showAddEditDialog = true
                        },
                        onDeleteTransaction = { tx ->
                            viewModel.deleteTransaction(tx)
                        }
                    )
                }

                FinanceScreen.BUDGETS -> {
                    BudgetsScreen(
                        monthlySummary = monthlySummary,
                        selectedMonth = selectedMonth,
                        availableMonths = availableMonths,
                        onSelectMonth = { viewModel.setSelectedMonth(it) },
                        onSetBudgetClick = {
                            budgetCategoryToEdit = null
                            budgetLimitToEdit = null
                            showSetBudgetDialog = true
                        },
                        onEditBudget = { cat, limit ->
                            budgetCategoryToEdit = cat
                            budgetLimitToEdit = limit
                            showSetBudgetDialog = true
                        },
                        onPopulateDefaults = {
                            viewModel.seedDemoData()
                        }
                    )
                }

                FinanceScreen.ANALYTICS -> {
                    AnalyticsScreen(
                        monthlySummary = monthlySummary,
                        selectedMonth = selectedMonth,
                        availableMonths = availableMonths,
                        onSelectMonth = { viewModel.setSelectedMonth(it) }
                    )
                }

                FinanceScreen.SPEC_REVIEW -> {
                    ProjectSpecScreen(
                        onSeedRealisticData = { viewModel.seedDemoData() },
                        onSeedOverrunCase = { viewModel.seedBudgetOverrunTest() },
                        onClearAllData = { viewModel.clearAllData() }
                    )
                }
            }
        }
    }

    // Add / Edit Transaction Dialog
    if (showAddEditDialog) {
        AddEditTransactionDialog(
            transactionToEdit = transactionToEdit,
            onDismiss = {
                showAddEditDialog = false
                transactionToEdit = null
            },
            onSave = { id, title, amount, type, category, dateMillis, notes ->
                val result = viewModel.saveTransaction(
                    id = id,
                    title = title,
                    amountStr = amount,
                    type = type,
                    category = category,
                    dateMillis = dateMillis,
                    notes = notes
                )
                if (result.isValid) null else result.errorMessage
            }
        )
    }

    // Set Budget Dialog
    if (showSetBudgetDialog) {
        SetBudgetDialog(
            initialCategory = budgetCategoryToEdit,
            initialLimit = budgetLimitToEdit,
            monthYear = selectedMonth,
            onDismiss = {
                showSetBudgetDialog = false
                budgetCategoryToEdit = null
                budgetLimitToEdit = null
            },
            onSave = { category, limitStr ->
                val result = viewModel.saveBudget(
                    category = category,
                    limitStr = limitStr,
                    monthYear = selectedMonth
                )
                if (result.isValid) null else result.errorMessage
            }
        )
    }
}
