package com.example.financetracker.data.model

data class CategoryDef(
    val id: String,
    val name: String,
    val iconKey: String,
    val colorHex: Long,
    val isExpense: Boolean,
    val isIncome: Boolean
)

object PredefinedCategories {
    val expenseCategories = listOf(
        CategoryDef("food", "Food & Dining", "restaurant", 0xFFF97316, isExpense = true, isIncome = false),
        CategoryDef("housing", "Rent & Housing", "home", 0xFF6366F1, isExpense = true, isIncome = false),
        CategoryDef("transport", "Transportation", "directions_car", 0xFF0284C7, isExpense = true, isIncome = false),
        CategoryDef("groceries", "Groceries", "shopping_cart", 0xFF10B981, isExpense = true, isIncome = false),
        CategoryDef("utilities", "Bills & Utilities", "bolt", 0xFFF59E0B, isExpense = true, isIncome = false),
        CategoryDef("entertainment", "Entertainment", "movie", 0xFF8B5CF6, isExpense = true, isIncome = false),
        CategoryDef("healthcare", "Healthcare & Medical", "local_hospital", 0xFFEC4899, isExpense = true, isIncome = false),
        CategoryDef("shopping", "Shopping & Gear", "shopping_bag", 0xFF06B6D4, isExpense = true, isIncome = false),
        CategoryDef("education", "Education & Courses", "school", 0xFF3B82F6, isExpense = true, isIncome = false),
        CategoryDef("other_exp", "Other Expense", "category", 0xFF64748B, isExpense = true, isIncome = false)
    )

    val incomeCategories = listOf(
        CategoryDef("salary", "Salary & Wages", "payments", 0xFF10B981, isExpense = false, isIncome = true),
        CategoryDef("freelance", "Freelance & Consulting", "laptop_mac", 0xFF0284C7, isExpense = false, isIncome = true),
        CategoryDef("investments", "Investments & Dividends", "trending_up", 0xFF8B5CF6, isExpense = false, isIncome = true),
        CategoryDef("bonuses", "Bonus & Awards", "card_giftcard", 0xFFF59E0B, isExpense = false, isIncome = true),
        CategoryDef("other_inc", "Other Income", "account_balance_wallet", 0xFF14B8A6, isExpense = false, isIncome = true)
    )

    val all = expenseCategories + incomeCategories

    fun getCategoryByName(name: String): CategoryDef {
        return all.find { it.name.equals(name, ignoreCase = true) }
            ?: CategoryDef("other", name, "category", 0xFF64748B, isExpense = true, isIncome = true)
    }
}
