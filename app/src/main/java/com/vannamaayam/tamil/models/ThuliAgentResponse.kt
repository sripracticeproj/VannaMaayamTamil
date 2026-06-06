package com.vannamaayam.tamil.models

/**
 * Represents the response from the AI Agent "Thuli", outlining UI animations,
 * cues, and voice clip resource IDs.
 */
data class ThuliAgentResponse(
    val status: ThuliStatus,
    val animation_state: String,     // E.g. "celebrate", "idle", "talking", "thinking"
    val visual_cue_asset: String,    // E.g. name of the color or image resource
    val voice_clip_id: String        // E.g. "success", "hint", "intro"
)

enum class ThuliStatus {
    SUCCESS,
    HINT
}
