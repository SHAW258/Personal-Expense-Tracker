package com.example.financetracker.data.model

data class CategorySpending(
    val categoryName: String,
    val totalAmount: Double,
    val transactionCount: Int,
    val percentageOfExpense: Double,
    val budgetLimit: Double? = null,
    val budgetSpentPercentage: Double? = null,
    val isOverBudget: Boolean = false,
    val isNearBudgetLimit: Boolean = false // > 80%
)

data class MonthlySummary(
    val monthYear: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val netBalance: Double,
    val savingsRate: Double, // %
    val totalBudget: Double,
    val totalBudgetUsedPercentage: Double,
    val categoryBreakdowns: List<CategorySpending>,
    val overBudgetCount: Int,
    val warningBudgetCount: Int,
    val transactionCount: Int
)
