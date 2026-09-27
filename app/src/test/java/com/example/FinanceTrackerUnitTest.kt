package com.example

import com.example.financetracker.data.model.Budget
import com.example.financetracker.data.model.PredefinedCategories
import com.example.financetracker.data.model.Transaction
import com.example.financetracker.data.model.TransactionType
import com.example.financetracker.ui.components.formatCurrency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinanceTrackerUnitTest {

    @Test
    fun testPredefinedCategories_hasExpensesAndIncome() {
        val expenses = PredefinedCategories.expenseCategories
        val income = PredefinedCategories.incomeCategories

        assertTrue(expenses.isNotEmpty())
        assertTrue(income.isNotEmpty())

        val food = PredefinedCategories.getCategoryByName("Food & Dining")
        assertNotNull(food)
        assertEquals("Food & Dining", food.name)
        assertTrue(food.isExpense)
        assertFalse(food.isIncome)

        val salary = PredefinedCategories.getCategoryByName("Salary & Wages")
        assertNotNull(salary)
        assertEquals("Salary & Wages", salary.name)
        assertTrue(salary.isIncome)
        assertFalse(salary.isExpense)
    }

    @Test
    fun testPredefinedCategories_fallbackForUnknownCategory() {
        val unknown = PredefinedCategories.getCategoryByName("Cryptocurrency Staking")
        assertEquals("Cryptocurrency Staking", unknown.name)
        assertEquals("category", unknown.iconKey)
    }

    @Test
    fun testCurrencyFormatting() {
        val formatted = formatCurrency(1234.50)
        assertTrue(formatted.contains("1,234.50") || formatted.contains("1234.50"))
    }

    @Test
    fun testTransactionCalculation_netBalanceAndSavingsRate() {
        val transactions = listOf(
            Transaction(
                id = 1,
                title = "Salary",
                amount = 4000.0,
                type = TransactionType.INCOME,
                category = "Salary & Wages",
                dateMillis = 1000L,
                monthYear = "2026-09"
            ),
            Transaction(
                id = 2,
                title = "Freelance",
                amount = 1000.0,
                type = TransactionType.INCOME,
                category = "Freelance & Consulting",
                dateMillis = 2000L,
                monthYear = "2026-09"
            ),
            Transaction(
                id = 3,
                title = "Rent",
                amount = 1500.0,
                type = TransactionType.EXPENSE,
                category = "Rent & Housing",
                dateMillis = 3000L,
                monthYear = "2026-09"
            ),
            Transaction(
                id = 4,
                title = "Groceries",
                amount = 500.0,
                type = TransactionType.EXPENSE,
                category = "Groceries",
                dateMillis = 4000L,
                monthYear = "2026-09"
            )
        )

        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense
        val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome) * 100 else 0.0

        assertEquals(5000.0, totalIncome, 0.001)
        assertEquals(2000.0, totalExpense, 0.001)
        assertEquals(3000.0, netBalance, 0.001)
        assertEquals(60.0, savingsRate, 0.001)
    }

    @Test
    fun testBudgetThreshold_overbudgetDetection() {
        val budget = Budget(id = 1, category = "Groceries", monthlyLimit = 400.0, monthYear = "2026-09")
        val spentNormal = 300.0
        val spentWarning = 350.0 // > 80% (320)
        val spentOver = 450.0

        assertFalse(spentNormal > budget.monthlyLimit)
        assertFalse((spentNormal / budget.monthlyLimit) * 100 >= 80.0)

        assertTrue((spentWarning / budget.monthlyLimit) * 100 >= 80.0)
        assertFalse(spentWarning > budget.monthlyLimit)

        assertTrue(spentOver > budget.monthlyLimit)
    }
}
