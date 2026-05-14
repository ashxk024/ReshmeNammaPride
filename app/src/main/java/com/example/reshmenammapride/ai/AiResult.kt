package com.example.reshmenammapride.ai

/**
 * Sealed class representing every possible outcome of an AI analysis call.
 * The ViewModel observes this and maps it to UI state.
 */
sealed class AiResult {

    /** Analysis completed successfully. [advice] is the farmer-friendly text. */
    data class Success(val advice: String) : AiResult()

    /** Network or server error. [message] is user-displayable. */
    data class Error(val message: String) : AiResult()

    /** API key not configured — guide the user to set it up. */
    data object ApiKeyMissing : AiResult()

    /** Not enough climate data to generate a meaningful analysis. */
    data object InsufficientData : AiResult()

    /** Request is in progress. */
    data object Loading : AiResult()

    /** Rate limit hit — user should wait before retrying. */
    data class RateLimited(val retryAfterSeconds: Int) : AiResult()
}
