package com.vannamaayam.tamil.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

/**
 * Dynamically plays voice and audio feedback clips from res/raw folder.
 */
class VoicePlayerManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    /**
     * Plays a resource dynamic by string identifier.
     * E.g., voice_clip_id = "success" resolves to res/raw/success.mp3.
     */
    fun playVoiceClip(voiceClipId: String, onComplete: () -> Unit = {}) {
        stopPlaying()

        try {
            // Find resource ID dynamically from string name
            val resId = context.resources.getIdentifier(
                voiceClipId,
                "raw",
                context.packageName
            )

            if (resId != 0) {
                // Initialize media player
                mediaPlayer = MediaPlayer.create(context, resId).apply {
                    setOnCompletionListener {
                        onComplete()
                        releasePlayer()
                    }
                    setOnErrorListener { mp, what, extra ->
                        Log.e("VoicePlayerManager", "Error playing audio: what=$what, extra=$extra")
                        releasePlayer()
                        true
                    }
                    start()
                }
            } else {
                Log.e("VoicePlayerManager", "Audio resource not found: $voiceClipId")
                onComplete()
            }
        } catch (e: Exception) {
            Log.e("VoicePlayerManager", "Exception during playback of $voiceClipId: ${e.message}")
            onComplete()
        }
    }

    /**
     * Stop current audio playback
     */
    fun stopPlaying() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
            }
        } catch (e: Exception) {
            Log.e("VoicePlayerManager", "Exception while stopping media player: ${e.message}")
        } finally {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    /**
     * Call when context/viewmodel is destroyed to clean resources
     */
    fun destroy() {
        releasePlayer()
    }
}
