package com.example.reshmenammapride.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reshmenammapride.database.entity.BatchEntity
import com.example.reshmenammapride.repository.BatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

// ── Form state ────────────────────────────────────────────────────────────────

data class BatchFormState(
    val batchName: String = "",
    val breedType: String = ""
)

data class SavedBatch(
    val batchId: Int,
    val batchName: String,
    val breedType: String,
    val createdEpochDay: Long
)

// ── ViewModel ────────────────────────────────────────────────────────────────

class BatchViewModel(
    private val repository: BatchRepository
) : ViewModel() {

    // New-batch form state
    private val _formState = MutableStateFlow(BatchFormState())
    val formState: StateFlow<BatchFormState> = _formState.asStateFlow()

    // All saved batches (Room Flow → StateFlow for Compose)
    val allBatches: StateFlow<List<BatchEntity>> = repository
        .getAllBatches()
        .stateIn(
            scope         = viewModelScope,
            started       = SharingStarted.WhileSubscribed(5_000),
            initialValue  = emptyList()
        )

    // Emits once after a successful save so the nav layer can react
    private val _savedBatch = MutableStateFlow<SavedBatch?>(null)
    val savedBatch: StateFlow<SavedBatch?> = _savedBatch.asStateFlow()

    fun onBatchNameChange(name: String) = _formState.update { it.copy(batchName = name) }
    fun onBreedTypeChange(breed: String) = _formState.update { it.copy(breedType = breed) }

    /**
     * Validates, persists the batch, and emits via [savedBatch].
     * Returns false immediately if validation fails so the UI can show a Toast.
     */
    fun saveBatch(): Boolean {
        val state = _formState.value
        if (state.batchName.isBlank() || state.breedType.isBlank()) return false

        val epochDay = LocalDate.now().toEpochDay()
        viewModelScope.launch {
            val newId = repository.insertBatch(
                batchName       = state.batchName.trim(),
                breedType       = state.breedType.trim(),
                createdEpochDay = epochDay
            )
            _savedBatch.value = SavedBatch(
                batchId         = newId,
                batchName       = state.batchName.trim(),
                breedType       = state.breedType.trim(),
                createdEpochDay = epochDay
            )
            _formState.value = BatchFormState()   // reset form
        }
        return true
    }

    /** Call after navigation has consumed the save event. */
    fun onSaveConsumed() { _savedBatch.value = null }

    // ── Factory ──────────────────────────────────────────────────────────────

    class Factory(private val repository: BatchRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BatchViewModel(repository) as T
    }
}
