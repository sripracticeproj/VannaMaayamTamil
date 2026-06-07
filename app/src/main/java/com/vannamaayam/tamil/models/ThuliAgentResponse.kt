package com.vannamaayam.tamil.models

/**
 * Revised ThuliAgentResponse for 3D and Dynamic Audio.
 */
data class ThuliAgentResponse(
    val status: ThuliStatus,
    val target_mesh_part: String? = null,
    val hex_color_code: String? = null,
    val dynamic_speech_payload: String? = null,
    val animation_state: String,
    val voice_clip_id: String
)

enum class ThuliStatus {
    SUCCESS,
    HINT
}
