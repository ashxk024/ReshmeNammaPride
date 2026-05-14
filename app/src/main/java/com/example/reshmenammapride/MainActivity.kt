package com.example.reshmenammapride

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.example.reshmenammapride.ai.service.GeminiRetrofitClient
import com.example.reshmenammapride.database.AppDatabase
import com.example.reshmenammapride.navigation.AppNavGraph
import com.example.reshmenammapride.repository.BatchRepository
import com.example.reshmenammapride.repository.GeminiRepository
import com.example.reshmenammapride.ui.theme.ReshmeNammaPrideTheme
import com.example.reshmenammapride.viewmodel.BatchViewModel
import com.example.reshmenammapride.viewmodel.DashboardViewModel

class MainActivity : ComponentActivity() {

    // ── Dependency chain (lazy = created only when first accessed) ────────────
    private val database by lazy {
        AppDatabase.getInstance(applicationContext)
    }

    private val batchRepository by lazy {
        BatchRepository(database.batchDao(), database.climateEntryDao())
    }

    private val geminiRepository by lazy {
        GeminiRepository(GeminiRetrofitClient.apiService)
    }

    // BatchViewModel lives at Activity scope (survives config changes)
    private val batchViewModel: BatchViewModel by viewModels {
        BatchViewModel.Factory(batchRepository)
    }

    // DashboardViewModel factory — new instance per Dashboard destination
    private val dashboardViewModelFactory by lazy {
        DashboardViewModel.Factory(batchRepository, geminiRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReshmeNammaPrideTheme {
                val navController = rememberNavController()
                AppNavGraph(
                    navController             = navController,
                    batchViewModel            = batchViewModel,
                    dashboardViewModelFactory = dashboardViewModelFactory
                )
            }
        }
    }
}
