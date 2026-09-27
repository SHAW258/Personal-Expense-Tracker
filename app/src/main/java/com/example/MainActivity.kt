package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.financetracker.data.local.FinanceDatabase
import com.example.financetracker.data.repository.FinanceRepository
import com.example.financetracker.ui.FinanceApp
import com.example.financetracker.viewmodel.FinanceViewModel
import com.example.financetracker.viewmodel.FinanceViewModelFactory
import com.example.ui.theme.FinanceTrackerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = FinanceDatabase.getDatabase(applicationContext)
        val repository = FinanceRepository(
            transactionDao = database.transactionDao(),
            budgetDao = database.budgetDao()
        )
        val factory = FinanceViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[FinanceViewModel::class.java]

        setContent {
            FinanceTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FinanceApp(viewModel = viewModel)
                }
            }
        }
    }
}
