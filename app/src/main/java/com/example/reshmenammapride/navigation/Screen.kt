package com.example.reshmenammapride.navigation

sealed class Screen(val route: String) {

    object Welcome : Screen("welcome")

    object Home : Screen("home")

    object NewBatch : Screen("new_batch")

    /**
     * Dashboard arguments:
     *   batchId          Int
     *   batchName        String
     *   breedType        String
     *   createdEpochDay  Long
     */
    object Dashboard :
        Screen("dashboard/{batchId}/{batchName}/{breedType}/{createdEpochDay}") {

        fun createRoute(
            batchId: Int,
            batchName: String,
            breedType: String,
            epochDay: Long
        ): String {

            return "dashboard/$batchId/$batchName/$breedType/$epochDay"
        }
    }
}