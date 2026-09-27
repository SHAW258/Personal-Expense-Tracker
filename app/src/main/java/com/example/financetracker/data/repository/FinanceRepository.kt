package com.example.financetracker.data.repository

import com.example.financetracker.data.local.BudgetDao
import com.example.financetracker.data.local.TransactionDao
import com.example.financetracker.data.model.Budget
import com.example.financetracker.data.model.CategorySpending
import com.example.financetracker.data.model.MonthlySummary
import com.example.financetracker.data.model.PredefinedCategories
import com.example.financetracker.data.model.Transaction
import com.example.financetracker.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allRecordedMonths: Flow<List<String>> = transactionDao.getAllRecordedMonths()

    fun getTransactionsByMonth(monthYear: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByMonth(monthYear)
    }

    fun getBudgetsForMonth(monthYear: String): Flow<List<Budget>> {
        return budgetDao.getBudgetsForMonth(monthYear)
    }

    fun searchTransactions(
        query: String,
        monthYear: String?,
        type: String?,
        category: String?,
        sortBy: String = "DATE_DESC"
    ): Flow<List<Transaction>> {
        return transactionDao.searchTransactions(
            query = query,
            monthYear = monthYear,
            type = type,
            category = category,
            sortBy = sortBy
        )
    }

    fun getMonthlySummary(monthYear: String): Flow<MonthlySummary> {
        return combine(
            transactionDao.getTransactionsByMonth(monthYear),
            budgetDao.getBudgetsForMonth(monthYear)
        ) { transactions, budgets ->
            val budgetMap = budgets.associateBy { it.category }

            var totalIncome = 0.0
            var totalExpense = 0.0

            val expenseCategoryMap = mutableMapOf<String, Pair<Double, Int>>()

            transactions.forEach { tx ->
                when (tx.type) {
                    TransactionType.INCOME -> totalIncome += tx.amount
                    TransactionType.EXPENSE -> {
                        totalExpense += tx.amount
                        val current = expenseCategoryMap[tx.category] ?: Pair(0.0, 0)
                        expenseCategoryMap[tx.category] = Pair(current.first + tx.amount, current.second + 1)
                    }
                }
            }

            // Also include categories that have a budget set even if spent is 0
            budgets.forEach { budget ->
                if (!expenseCategoryMap.containsKey(budget.category)) {
                    expenseCategoryMap[budget.category] = Pair(0.0, 0)
                }
            }

            var overBudgetCount = 0
            var warningBudgetCount = 0

            val categoryBreakdowns = expenseCategoryMap.map { (catName, pair) ->
                val spent = pair.first
                val count = pair.second
                val percentOfExpense = if (totalExpense > 0) (spent / totalExpense) * 100 else 0.0
                val budget = budgetMap[catName]
                val budgetLimit = budget?.monthlyLimit
                val budgetSpentPercent = budgetLimit?.let {
                    if (it > 0) (spent / it) * 100 else 0.0
                }

                val isOver = budgetLimit != null && spent > budgetLimit
                val isNear = budgetLimit != null && !isOver && (budgetSpentPercent ?: 0.0) >= 80.0

                if (isOver) overBudgetCount++
                if (isNear) warningBudgetCount++

                CategorySpending(
                    categoryName = catName,
                    totalAmount = spent,
                    transactionCount = count,
                    percentageOfExpense = percentOfExpense,
                    budgetLimit = budgetLimit,
                    budgetSpentPercentage = budgetSpentPercent,
                    isOverBudget = isOver,
                    isNearBudgetLimit = isNear
                )
            }.sortedByDescending { it.totalAmount }

            val totalBudget = budgets.sumOf { it.monthlyLimit }
            val totalBudgetUsedPercentage = if (totalBudget > 0) (totalExpense / totalBudget) * 100 else 0.0
            val netBalance = totalIncome - totalExpense
            val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome) * 100 else 0.0

            MonthlySummary(
                monthYear = monthYear,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                netBalance = netBalance,
                savingsRate = savingsRate.coerceIn(-100.0, 100.0),
                totalBudget = totalBudget,
                totalBudgetUsedPercentage = totalBudgetUsedPercentage,
                categoryBreakdowns = categoryBreakdowns,
                overBudgetCount = overBudgetCount,
                warningBudgetCount = warningBudgetCount,
                transactionCount = transactions.size
            )
        }
    }

    suspend fun insertTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun insertOrUpdateBudget(budget: Budget): Long {
        val existing = budgetDao.getBudgetForCategory(budget.monthYear, budget.category)
        return if (existing != null) {
            budgetDao.updateBudget(budget.copy(id = existing.id))
            existing.id
        } else {
            budgetDao.insertBudget(budget)
        }
    }

    suspend fun deleteBudgetById(id: Long) {
        budgetDao.deleteBudgetById(id)
    }

    suspend fun clearAllData() {
        transactionDao.deleteAll()
        budgetDao.deleteAll()
    }

    suspend fun seedRealisticDemoData(currentMonthYear: String) {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val currentMonth = currentMonthYear

        cal.add(Calendar.MONTH, -1)
        val prevMonth = sdf.format(cal.time)
        cal.add(Calendar.MONTH, 1) // reset to current

        // Default Budgets for current month
        val defaultBudgets = listOf(
            Budget(category = "Food & Dining", monthlyLimit = 450.0, monthYear = currentMonth),
            Budget(category = "Groceries", monthlyLimit = 550.0, monthYear = currentMonth),
            Budget(category = "Rent & Housing", monthlyLimit = 1400.0, monthYear = currentMonth),
            Budget(category = "Transportation", monthlyLimit = 250.0, monthYear = currentMonth),
            Budget(category = "Bills & Utilities", monthlyLimit = 280.0, monthYear = currentMonth),
            Budget(category = "Entertainment", monthlyLimit = 180.0, monthYear = currentMonth),
            Budget(category = "Shopping & Gear", monthlyLimit = 200.0, monthYear = currentMonth),
            Budget(category = "Healthcare & Medical", monthlyLimit = 150.0, monthYear = currentMonth)
        )
        budgetDao.insertAll(defaultBudgets)

        // Seed current month transactions
        val now = System.currentTimeMillis()
        val dayMillis = 86_400_000L

        val demoTransactions = listOf(
            // Incomes
            Transaction(
                title = "Monthly Tech Salary",
                amount = 4850.00,
                type = TransactionType.INCOME,
                category = "Salary & Wages",
                dateMillis = now - (25 * dayMillis),
                notes = "Direct deposit from Acquired Corp",
                monthYear = currentMonth
            ),
            Transaction(
                title = "Mobile App Consulting",
                amount = 950.00,
                type = TransactionType.INCOME,
                category = "Freelance & Consulting",
                dateMillis = now - (12 * dayMillis),
                notes = "UI/UX Architecture Sprint milestone",
                monthYear = currentMonth
            ),
            Transaction(
                title = "Quarterly Index Dividend",
                amount = 125.40,
                type = TransactionType.INCOME,
                category = "Investments & Dividends",
                dateMillis = now - (5 * dayMillis),
                notes = "S&P 500 ETF dividend payout",
                monthYear = currentMonth
            ),

            // Fixed Housing & Utilities
            Transaction(
                title = "Apartment Rent",
                amount = 1350.00,
                type = TransactionType.EXPENSE,
                category = "Rent & Housing",
                dateMillis = now - (24 * dayMillis),
                notes = "Automated bank ACH transfer",
                monthYear = currentMonth
            ),
            Transaction(
                title = "Fiber Gigabit Internet",
                amount = 75.00,
                type = TransactionType.EXPENSE,
                category = "Bills & Utilities",
                dateMillis = now - (20 * dayMillis),
                notes = "Monthly broadband service",
                monthYear = currentMonth
            ),
            Transaction(
                title = "Electric & Energy Bill",
                amount = 142.30,
                type = TransactionType.EXPENSE,
                category = "Bills & Utilities",
                dateMillis = now - (15 * dayMillis),
                notes = "City utilities invoice",
                monthYear = currentMonth
            ),

            // Groceries & Food
            Transaction(
                title = "Whole Foods Organic Groceries",
                amount = 168.45,
                type = TransactionType.EXPENSE,
                category = "Groceries",
                dateMillis = now - (22 * dayMillis),
                notes = "Weekly fresh produce and pantry staples",
                monthYear = currentMonth
            ),
            Transaction(
                title = "Trader Joe's Snack Run",
                amount = 94.20,
                type = TransactionType.EXPENSE,
                category = "Groceries",
                dateMillis = now - (14 * dayMillis),
                notes = "Healthy snacks and frozen items",
                monthYear = currentMonth
            ),
            Transaction(
                title = "Farmers Market Fresh Produce",
                amount = 62.80,
                type = TransactionType.EXPENSE,
                category = "Groceries",
                dateMillis = now - (6 * dayMillis),
                notes = "Fresh vegetables, sourdough, fruit",
                monthYear = currentMonth
            ),
            Transaction(
                title = "Supermarket Restock",
                amount = 115.10,
                type = TransactionType.EXPENSE,
                category = "Groceries",
                dateMillis = now - (2 * dayMillis),
                notes = "Household supplies and essentials",
                monthYear = currentMonth
            ),

            // Dining Out & Cafes
            Transaction(
                title = "Bistro Dinner with Team",
                amount = 135.00,
                type = TransactionType.EXPENSE,
                category = "Food & Dining",
                dateMillis = now - (18 * dayMillis),
                notes = "Italian dinner celebrate sprint release",
                monthYear = currentMonth
            ),
            Transaction(
                title = "Blue Bottle Artisanal Coffee",
                amount = 18.50,
                type = TransactionType.EXPENSE,
                category = "Food & Dining",
                dateMillis = now - (10 * dayMillis),
                notes = "Pour-over and croissants",
                monthYear = currentMonth
            ),
            Transaction(
                title = "Sushi Omakase Lunch",
                amount = 88.00,
                type = TransactionType.EXPENSE,
                category = "Food & Dining",
                dateMillis = now - (7 * dayMillis),
                notes = "Client catchup meal",
                monthYear = currentMonth
            ),
            Transaction(
                title = "Weekend Thai Takeout",
                amount = 46.25,
                type = TransactionType.EXPENSE,
                category = "Food & Dining",
                dateMillis = now - (3 * dayMillis),
                notes = "Pad see ew and green curry",
                monthYear = currentMonth
            ),

            // Transportation
            Transaction(
                title = "Metro Transit Monthly Pass",
                amount = 90.00,
                type = TransactionType.EXPENSE,
                category = "Transportation",
                dateMillis = now - (26 * dayMillis),
                notes = "Subway and light rail card refill",
                monthYear = currentMonth
            ),
            Transaction(
                title = "Chevron Fuel Station",
                amount = 54.80,
                type = TransactionType.EXPENSE,
                category = "Transportation",
                dateMillis = now - (13 * dayMillis),
                notes = "Full tank refill",
                monthYear = currentMonth
            ),
            Transaction(
                title = "Airport Ride Hail",
                amount = 42.60,
                type = TransactionType.EXPENSE,
                category = "Transportation",
                dateMillis = now - (4 * dayMillis),
                notes = "Terminal ride return trip",
                monthYear = currentMonth
            ),

            // Entertainment & Subscriptions
            Transaction(
                title = "Streaming Cloud Subscriptions",
                amount = 32.98,
                type = TransactionType.EXPENSE,
                category = "Entertainment",
                dateMillis = now - (21 * dayMillis),
                notes = "Video & Music premium services",
                monthYear = currentMonth
            ),
            Transaction(
                title = "Indie Theater Film Tickets",
                amount = 38.00,
                type = TransactionType.EXPENSE,
                category = "Entertainment",
                dateMillis = now - (9 * dayMillis),
                notes = "Weekend cinema double screening",
                monthYear = currentMonth
            ),

            // Healthcare & Fitness
            Transaction(
                title = "Climbing & Gym Membership",
                amount = 95.00,
                type = TransactionType.EXPENSE,
                category = "Healthcare & Medical",
                dateMillis = now - (23 * dayMillis),
                notes = "Bouldering gym monthly pass",
                monthYear = currentMonth
            ),
            Transaction(
                title = "Pharmacy Prescription & Vitamins",
                amount = 36.40,
                type = TransactionType.EXPENSE,
                category = "Healthcare & Medical",
                dateMillis = now - (11 * dayMillis),
                notes = "Supplements and allergy meds",
                monthYear = currentMonth
            ),

            // Shopping (Notice: simulates hitting overbudget threshold for review!)
            Transaction(
                title = "Ergonomic Mechanical Keyboard",
                amount = 219.00,
                type = TransactionType.EXPENSE,
                category = "Shopping & Gear",
                dateMillis = now - (8 * dayMillis),
                notes = "Split ergonomic keyboard for workspace",
                monthYear = currentMonth
            )
        )

        transactionDao.insertAll(demoTransactions)
    }

    suspend fun seedBudgetOverrunTest(currentMonthYear: String) {
        val now = System.currentTimeMillis()
        val testTx = Transaction(
            title = "Flagship Noise Canceling Headphones",
            amount = 350.00,
            type = TransactionType.EXPENSE,
            category = "Shopping & Gear",
            dateMillis = now,
            notes = "Deliberate test transaction to trigger overbudget alert",
            monthYear = currentMonthYear
        )
        transactionDao.insertTransaction(testTx)
    }
}
