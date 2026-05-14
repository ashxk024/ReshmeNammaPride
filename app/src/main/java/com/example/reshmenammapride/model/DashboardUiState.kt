package com.example.reshmenammapride.model

import java.time.LocalDate

enum class ClimateStatus { GOOD, MODERATE, BAD }
enum class InstarStage   { EARLY, MID, MATURE }

/** Represents the full state of the AI advice panel. */
sealed class AiAdviceState {
    data object Idle        : AiAdviceState()
    data object Loading     : AiAdviceState()
    data class  Success(val text: String) : AiAdviceState()
    data class  Error(val message: String) : AiAdviceState()
    data object ApiKeyMissing    : AiAdviceState()
    data object InsufficientData : AiAdviceState()
    data class  RateLimited(val waitSeconds: Int) : AiAdviceState()
}

data class DashboardUiState(
    // Batch identity
    val batchId: Int = 0,
    val batchName: String = "",
    val breedType: String = "",
    val createdDate: LocalDate = LocalDate.now(),

    // Climate
    val climateEntries: List<ClimateEntry> = emptyList(),
    val climateStatus: ClimateStatus = ClimateStatus.GOOD,

    // Input dialog
    val showClimateDialog: Boolean = false,
    val dialogTemp: String = "",
    val dialogHumidity: String = "",

    // AI advice — now a sealed state instead of a plain String
    val aiAdviceState: AiAdviceState = AiAdviceState.Idle,

    // Derived / computed by ViewModel
    val instarStage: InstarStage = InstarStage.EARLY,
    val daysElapsed: Int = 0,
    val instarProgress: Float = 0f,
    val harvestProgress: Float = 0f,
    val estimatedHarvestMonth: String = "",

    // Loading state for initial Room data fetch
    val isLoading: Boolean = false
)
