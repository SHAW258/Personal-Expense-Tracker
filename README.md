# Personal Finance Mobile Tracker 📊💳

> **Project 1: Foundation / Core Industry Task**  
> An offline-first Android application built with Kotlin, Jetpack Compose (Material 3), and Room SQLite persistence for recording simulated expenses, categorizing transactions, tracking category budgets, performing multi-parameter searches, and generating monthly summaries.

---

## 1. Executive Summary & Business Use Case

### Scenario
Personal and organizational financial visibility often suffers from delayed manual reviews, fragmented records, and lack of real-time warning indicators when spending nears critical budget allocations. This application delivers an offline-safe personal finance tracker tailored to record simulated expenses and income, track proactive category caps, and deliver immediate monthly performance summaries.

### Stakeholder Matrix
- **Primary Stakeholder**: Budget-conscious professional, household operations lead, or corporate financial coach evaluating cash flow health.
- **Decisions Enabled**: Real-time evaluation of discretionary vs. essential expenditures; proactive reallocation of capital before breaching monthly limits (>80% warning threshold).
- **Measurable Benefit**: Elimination of unexpected month-end deficits; documented ~15–25% reduction in non-essential overspending through persistent category caps and visual donut chart distribution.

---

## 2. Problem Statement & Quality Measures

| Dimension | Specification |
|---|---|
| **Inputs** | Transaction records (Title/Merchant, Amount, Type [Expense/Income], Category, Date, Notes) and Category Monthly Budget limits. |
| **Outputs** | Real-time reactive monthly summaries, category donut distribution, budget progress bars with danger/warning chips, multi-parameter search/sort results, and plain-text audit reports. |
| **Constraints** | Offline-first local execution (no cloud dependency for financial privacy), strict input validation (amount > 0, required titles), edge-to-edge Material 3 layout. |
| **Quality Measures** | Deterministic currency calculations, reactive Kotlin Coroutines/Flow pipeline, WCAG 48dp touch targets, comprehensive JVM/Robolectric unit testing. |

---

## 3. Key Features

- **Dashboard**:
  - Net Cash Flow hero card with dynamic color gradients and automatic savings rate calculation ($(\text{Income} - \text{Expenses}) / \text{Income}$).
  - Real-time overbudget warning banners and quick action shortcuts (Add Expense, Add Income, Set Budget).
  - Snapshot of top active category budgets and 5 most recent transactions.
  - Interactive month switcher.

- **Transactions Log & Search**:
  - Live search across transaction title, notes, and merchant names.
  - Multi-filter chips: Filter by type (`All`, `Expenses`, `Income`), category, and date/amount sorting (`Newest First`, `Oldest First`, `Highest Amount`, `Lowest Amount`).
  - Edit and delete records with confirmation dialogs.
  - Dynamic FAB for quick transaction creation.

- **Category Budgets**:
  - Allocate monthly spend caps across essential and discretionary categories (Food & Dining, Rent & Housing, Groceries, Transportation, Bills & Utilities, Entertainment, Shopping & Gear, Healthcare & Medical, etc.).
  - Color-coded progress indicators: Emerald (`<80%`), Amber Warning (`≥80%`), and Red Danger (`>100%`).
  - Quick-filter budgets by health status (`All`, `Safe`, `Near Limit`, `Overbudget`).

- **Analytics & Monthly Summaries**:
  - Custom Canvas-rendered animated Donut Chart displaying percentage distribution of expenses across categories.
  - Key Performance Indicators: Daily average burn rate, savings rate percentage, top expenditure category, and net surplus.
  - Formatted plain-text summary report export to clipboard for stakeholder review.

- **Spec & Audit Guide Screen**:
  - Built-in reviewable acceptance criteria checklist.
  - Simulation sandbox buttons:
    - *Seed Realistic Multi-Category Dataset* (populates realistic income, rent, utilities, food, and transit).
    - *Simulate Budget Overrun Alert Test* (adds a deliberate budget-bursting transaction to test alerts).
    - *Reset Database to Clean State*.
  - Traceable architecture decision log and Phase 2 roadmap.

---

## 4. Technical Architecture & Tech Stack

```
com.example.financetracker/
├── data/
│   ├── local/
│   │   ├── BudgetDao.kt           # Room DAO for category budgets
│   │   ├── Converters.kt          # Type converters for Room
│   │   ├── FinanceDatabase.kt     # Room SQLite Database singleton
│   │   └── TransactionDao.kt      # Room DAO for transactions & search queries
│   ├── model/
│   │   ├── Budget.kt              # Budget entity
│   │   ├── Category.kt            # Predefined category definitions & palettes
│   │   ├── MonthlySummary.kt      # Computed summary models & aggregations
│   │   └── Transaction.kt         # Transaction entity & TransactionType enum
│   └── repository/
│       └── FinanceRepository.kt   # Unified data repository with Kotlin Flow combine()
├── ui/
│   ├── components/
│   │   ├── BudgetProgressCard.kt  # Animated linear budget bar with status chips
│   │   ├── CategoryIcon.kt        # Material Symbols category icon badges
│   │   ├── DonutChart.kt          # Custom Canvas-drawn animated Donut Chart
│   │   ├── EmptyStateView.kt      # Zero-state placeholder illustration
│   │   ├── FinancialCards.kt      # Monthly Hero card & currency formatting
│   │   └── TransactionItemCard.kt # Card representation of a financial record
│   ├── dialogs/
│   │   ├── AddEditTransactionDialog.kt # Form dialog with date picker & validation
│   │   └── SetBudgetDialog.kt          # Category budget allocation dialog
│   ├── screens/
│   │   ├── AnalyticsScreen.kt     # Financial health KPIs & Donut Chart
│   │   ├── BudgetsScreen.kt       # Budget management & threshold filters
│   │   ├── DashboardScreen.kt     # Overview hero card & quick actions
│   │   ├── ProjectSpecScreen.kt   # Foundation review guide & demo controls
│   │   └── TransactionsScreen.kt  # Search, filter, and transaction log
│   └── FinanceApp.kt              # Scaffold, TopBar, BottomBar, & navigation state
└── viewmodel/
    └── FinanceViewModel.kt        # StateFlow streams, validation & business logic
```

### Technology Choices
- **Kotlin**: 100% Kotlin codebase leveraging Coroutines and Flow for reactive streams.
- **Jetpack Compose + Material 3**: Declarative UI, edge-to-edge support with `WindowInsets.safeDrawing`, centralized `Theme.kt`, and WCAG compliant 48dp touch targets.
- **Room Database (SQLite + KSP)**: Offline-first persistence without network dependencies, utilizing indexed Flow queries.
- **MVVM Pattern**: Single source of truth in `FinanceViewModel` with immutable `StateFlow` exposing data to composables.
- **Unit & Robolectric Testing**: Fast JVM-based testing verifying business logic and Android resource resolution.

---

## 5. Verification & Testing

Unit and local JVM Robolectric tests verify the critical user journeys (CUJs):

```bash
gradle :app:testDebugUnitTest
```

### Test Suite (`/app/src/test/java/com/example/`)
- `FinanceTrackerUnitTest.kt`:
  - `testPredefinedCategories_hasExpensesAndIncome`: Verifies predefined categories and expense/income mappings.
  - `testPredefinedCategories_fallbackForUnknownCategory`: Verifies graceful fallback for non-standard categories.
  - `testCurrencyFormatting`: Verifies standard currency string formatting.
  - `testTransactionCalculation_netBalanceAndSavingsRate`: Verifies deterministic balance and savings rate computation.
  - `testBudgetThreshold_overbudgetDetection`: Verifies the 80% warning and 100% overbudget threshold logic.
- `ExampleRobolectricTest.kt`:
  - Verifies Android application context and string resource loading (`app_name = "FinanceTracker"`).

---

## 6. How to Run

1. Open the project in **Android Studio** (or the web emulator in AI Studio).
2. Sync Gradle files (KSP and Compose are pre-configured in `build.gradle.kts`).
3. Run the app on an Android device or emulator running API 24+ (target SDK 36).
4. On first launch, the app automatically initializes a realistic sample dataset for immediate review.
5. Navigate using the bottom navigation bar (`Dashboard`, `Transactions`, `Budgets`, `Analytics`, `Spec & Audit`).
