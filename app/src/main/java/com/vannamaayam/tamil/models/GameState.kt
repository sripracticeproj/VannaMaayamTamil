package com.vannamaayam.tamil.models

/**
 * Represents the current game/interaction state, sent as input to the AI logic.
 */
data class GameState(
    val current_animal: String,
    val target_color_tamil: String,
    val child_audio_transcript: String
)
