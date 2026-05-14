package com.example.reshmenammapride.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.reshmenammapride.ui.screens.DashboardScreen
import com.example.reshmenammapride.ui.screens.HomeScreen
import com.example.reshmenammapride.ui.screens.NewBatchScreen
import com.example.reshmenammapride.viewmodel.BatchViewModel
import com.example.reshmenammapride.viewmodel.DashboardViewModel
import com.example.reshmenammapride.ui.screens.WelcomeScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    batchViewModel: BatchViewModel,
    dashboardViewModelFactory: DashboardViewModel.Factory
) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController)
        }

        // ── Home ─────────────────────────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel       = batchViewModel,
                onCreateBatch   = { navController.navigate(Screen.NewBatch.route) },
                onOpenDashboard = { batchId, batchName, breedType, epochDay ->
                    navController.navigate(
                        Screen.Dashboard.createRoute(batchId, batchName, breedType, epochDay)
                    )
                }
            )
        }

        // ── New Batch ─────────────────────────────────────────────────────────
        composable(Screen.NewBatch.route) {
            NewBatchScreen(
                viewModel    = batchViewModel,
                onBack       = { navController.popBackStack() },
                onBatchSaved = { saved ->
                    navController.popBackStack()
                    navController.navigate(
                        Screen.Dashboard.createRoute(
                            saved.batchId, saved.batchName, saved.breedType, saved.createdEpochDay
                        )
                    )
                }
            )
        }

        // ── Dashboard ─────────────────────────────────────────────────────────
        composable(
            route     = Screen.Dashboard.route,
            arguments = listOf(
                navArgument("batchId")         { type = NavType.IntType    },
                navArgument("batchName")        { type = NavType.StringType },
                navArgument("breedType")        { type = NavType.StringType },
                navArgument("createdEpochDay")  { type = NavType.LongType  }
            )
        ) { backStack ->
            val batchId  = backStack.arguments?.getInt("batchId")            ?: 0
            val name     = backStack.arguments?.getString("batchName")        ?: ""
            val breed    = backStack.arguments?.getString("breedType")        ?: ""
            val epochDay = backStack.arguments?.getLong("createdEpochDay")    ?: 0L

            DashboardScreen(
                batchId             = batchId,
                batchName           = name,
                breedType           = breed,
                createdDateEpochDay = epochDay,
                onBack              = { navController.popBackStack() },
                viewModelFactory    = dashboardViewModelFactory
            )
        }
    }
}
