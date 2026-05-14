package com.example.reshmenammapride.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reshmenammapride.ai.AiResult
import com.example.reshmenammapride.model.AiAdviceState
import com.example.reshmenammapride.model.ClimateEntry
import com.example.reshmenammapride.model.ClimateStatus
import com.example.reshmenammapride.model.DashboardUiState
import com.example.reshmenammapride.model.InstarStage
import com.example.reshmenammapride.repository.BatchRepository
import com.example.reshmenammapride.repository.GeminiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.min

class DashboardViewModel(
    private val batchRepository: BatchRepository,
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var aiJob: Job? = null   // lets us cancel any in-flight AI call

    // ── Initialise ────────────────────────────────────────────────────────────

    fun initBatch(
        batchId: Int,
        batchName: String,
        breedType: String,
        createdDateEpochDay: Long
    ) {
        val createdDate = LocalDate.ofEpochDay(createdDateEpochDay)
        _uiState.update { state ->
            state.copy(
                batchId     = batchId,
                batchName   = batchName,
                breedType   = breedType,
                createdDate = createdDate,
                isLoading   = true
            ).withDerivedFields()
        }

        viewModelScope.launch {
            batchRepository.getClimateEntries(batchId).collect { entities ->
                val entries = entities.map { e ->
                    ClimateEntry(
                        id          = e.id,
                        temperature = e.temperature,
                        humidity    = e.humidity,
                        recordedAt  = ClimateEntry.fromEpochMillis(e.recordedAt)
                    )
                }
                val latestStatus = entries.firstOrNull()?.let {
                    computeClimateStatus(it.temperature, it.humidity)
                } ?: ClimateStatus.GOOD

                _uiState.update {
                    it.copy(
                        climateEntries = entries,
                        climateStatus  = latestStatus,
                        isLoading      = false
                    )
                }
            }
        }
    }

    // ── Climate dialog ────────────────────────────────────────────────────────

    fun openClimateDialog() =
        _uiState.update { it.copy(showClimateDialog = true, dialogTemp = "", dialogHumidity = "") }

    fun closeClimateDialog() =
        _uiState.update { it.copy(showClimateDialog = false) }

    fun onDialogTempChange(value: String) =
        _uiState.update { it.copy(dialogTemp = value) }

    fun onDialogHumidityChange(value: String) =
        _uiState.update { it.copy(dialogHumidity = value) }

    fun saveClimateEntry(): String? {
        val state = _uiState.value
        val temp  = state.dialogTemp.toFloatOrNull()     ?: return "Please enter a valid temperature."
        val hum   = state.dialogHumidity.toFloatOrNull() ?: return "Please enter a valid humidity."
        if (temp < 0f || temp > 60f)  return "Temperature must be between 0–60 °C."
        if (hum  < 0f || hum  > 100f) return "Humidity must be between 0–100 %."

        viewModelScope.launch {
            batchRepository.insertClimateEntry(
                batchId     = state.batchId,
                temperature = temp,
                humidity    = hum
            )
        }
        _uiState.update { it.copy(showClimateDialog = false) }
        return null
    }

    // ── AI Analysis ───────────────────────────────────────────────────────────

    /**
     * Triggers the full AI pipeline:
     *   Room entries → preprocessing → Gemini API → UI state update
     *
     * Cancels any previous in-flight call before starting a new one.
     */
    fun analyzeWithAI() {
        Log.d("AI_FLOW", "Analyze button clicked")
        aiJob?.cancel()

        _uiState.update { it.copy(aiAdviceState = AiAdviceState.Loading) }

        aiJob = viewModelScope.launch {
            val state = _uiState.value

            // Fetch the raw entities (one-shot, not Flow)
            val rawEntries = batchRepository.getClimateEntriesOnce(state.batchId)

            val result = geminiRepository.analyzeClimate(
                entries       = rawEntries,
                batchName     = state.batchName,
                breedType     = state.breedType,
                batchAgeDays  = state.daysElapsed,
                instarStage   = state.instarStage
            )

            val newAdviceState = when (result) {
                is AiResult.Success        -> AiAdviceState.Success(result.advice)
                is AiResult.Error          -> AiAdviceState.Error(result.message)
                is AiResult.RateLimited    -> AiAdviceState.RateLimited(result.retryAfterSeconds)
                is AiResult.ApiKeyMissing  -> AiAdviceState.ApiKeyMissing
                is AiResult.InsufficientData -> AiAdviceState.InsufficientData
                is AiResult.Loading        -> AiAdviceState.Loading  // shouldn't happen here
            }

            _uiState.update { it.copy(aiAdviceState = newAdviceState) }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun computeClimateStatus(temp: Float, humidity: Float): ClimateStatus {
        val tempGood = temp in 24f..28f
        val humGood  = humidity in 70f..85f
        val tempMod  = temp in 20f..32f
        val humMod   = humidity in 60f..90f
        return when {
            tempGood && humGood -> ClimateStatus.GOOD
            tempMod  && humMod  -> ClimateStatus.MODERATE
            else                -> ClimateStatus.BAD
        }
    }

    private fun DashboardUiState.withDerivedFields(): DashboardUiState {
        val today = LocalDate.now()
        val days  = createdDate.until(today).days.coerceAtLeast(0)
        val (stage, instarProg) = when {
            days <= 10 -> InstarStage.EARLY  to (days / 10f)
            days <= 20 -> InstarStage.MID    to (days / 20f)
            else       -> InstarStage.MATURE to min(days / 28f, 1f)
        }
        val harvestProg  = min(days / 28f, 1f)
        val harvestDate  = createdDate.plusDays(28)
        val harvestMonth = harvestDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH) +
                " " + harvestDate.year
        return copy(
            daysElapsed           = days,
            instarStage           = stage,
            instarProgress        = instarProg,
            harvestProgress       = harvestProg,
            estimatedHarvestMonth = harvestMonth
        )
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory(
        private val batchRepository: BatchRepository,
        private val geminiRepository: GeminiRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DashboardViewModel(batchRepository, geminiRepository) as T
    }
}
