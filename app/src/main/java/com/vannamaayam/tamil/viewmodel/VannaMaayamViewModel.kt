package com.vannamaayam.tamil.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vannamaayam.tamil.audio.VoicePlayerManager
import com.vannamaayam.tamil.models.GameState
import com.vannamaayam.tamil.models.ThuliAgentResponse
import com.vannamaayam.tamil.models.ThuliStatus
import com.vannamaayam.tamil.speech.TamilSpeechManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class AnimalColorRound(
    val animal: String,           // E.g., "singam" (Lion), "yaanai" (Elephant)
    val targetColorTamil: String, // E.g., "மஞ்சள்" (Yellow), "பச்சை" (Green)
    val colorHex: Long,           // Target color to apply in Compose UI
    val transliterations: List<String> // Transliterated/alternative names for matches
)

class VannaMaayamViewModel(application: Application) : AndroidViewModel(application) {

    private val speechManager = TamilSpeechManager(application)
    private val voicePlayerManager = VoicePlayerManager(application)

    // Game round dataset
    private val rounds = listOf(
        AnimalColorRound("singam", "மஞ்சள்", 0xFFFFF099, listOf("மஞ்சள்", "manjal", "yellow", "manchal")),
        AnimalColorRound("thavalai", "பச்சை", 0xFFB5F2D2, listOf("பச்சை", "pachai", "green", "pachcha")),
        AnimalColorRound("paravai", "நீலம்", 0xFFAFE4FF, listOf("நீலம்", "neelam", "blue")),
        AnimalColorRound("muyal", "சிவப்பு", 0xFFFF9E80, listOf("சிவப்பு", "sivappu", "red", "civappu"))
    )

    private var currentRoundIndex = 0

    // Live UI States
    private val _currentAnimal = MutableStateFlow("")
    val currentAnimal: StateFlow<String> = _currentAnimal.asStateFlow()

    private val _targetColorTamil = MutableStateFlow("")
    val targetColorTamil: StateFlow<String> = _targetColorTamil.asStateFlow()

    private val _guessedColorHex = MutableStateFlow<Long?>(null)
    val guessedColorHex: StateFlow<Long?> = _guessedColorHex.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _thuliAnimationState = MutableStateFlow("idle") // "idle", "talking", "thinking", "celebrate"
    val thuliAnimationState: StateFlow<String> = _thuliAnimationState.asStateFlow()

    private val _gameFinished = MutableStateFlow(false)
    val gameFinished: StateFlow<Boolean> = _gameFinished.asStateFlow()

    init {
        loadRound()
        playWelcomeMessage()
    }

    private fun loadRound() {
        if (currentRoundIndex < rounds.size) {
            val round = rounds[currentRoundIndex]
            _currentAnimal.value = round.animal
            _targetColorTamil.value = round.targetColorTamil
            _guessedColorHex.value = null
            _thuliAnimationState.value = "idle"
        } else {
            _gameFinished.value = true
            _thuliAnimationState.value = "celebrate"
        }
    }

    private fun playWelcomeMessage() {
        _thuliAnimationState.value = "talking"
        voicePlayerManager.playVoiceClip("intro") {
            _thuliAnimationState.value = "idle"
        }
    }

    fun startListening() {
        if (_isListening.value) return

        _thuliAnimationState.value = "thinking"
        speechManager.startListening(
            onResult = { transcript ->
                processSpeechResult(transcript)
            },
            onPartialResult = { partial ->
                // Visual feedback of speech transcripts
            },
            onRmsChanged = { rms ->
                _audioRms.value = rms
            },
            onListeningStateChanged = { listening ->
                _isListening.value = listening
                if (!listening && _thuliAnimationState.value == "thinking") {
                    _thuliAnimationState.value = "idle"
                }
            },
            onError = { errorCode ->
                _isListening.value = false
                _thuliAnimationState.value = "idle"
            }
        )
    }

    fun stopListening() {
        speechManager.stopListening()
    }

    private fun processSpeechResult(transcript: String) {
        val cleanTranscript = transcript.trim().lowercase(Locale.ROOT)
        val currentRound = rounds[currentRoundIndex]

        // Map game state to schema
        val state = GameState(
            current_animal = currentRound.animal,
            target_color_tamil = currentRound.targetColorTamil,
            child_audio_transcript = transcript
        )

        // Check if guess matches target or transliterations
        val isCorrect = currentRound.transliterations.any { translit ->
            cleanTranscript.contains(translit.lowercase(Locale.ROOT))
        }

        val agentResponse = if (isCorrect) {
            ThuliAgentResponse(
                status = ThuliStatus.SUCCESS,
                animation_state = "celebrate",
                visual_cue_asset = currentRound.animal,
                voice_clip_id = "success"
            )
        } else {
            ThuliAgentResponse(
                status = ThuliStatus.HINT,
                animation_state = "talking",
                visual_cue_asset = currentRound.animal,
                voice_clip_id = "hint"
            )
        }

        applyAgentResponse(agentResponse, currentRound)
    }

    private fun applyAgentResponse(response: ThuliAgentResponse, round: AnimalColorRound) {
        _thuliAnimationState.value = response.animation_state

        if (response.status == ThuliStatus.SUCCESS) {
            _guessedColorHex.value = round.colorHex
            voicePlayerManager.playVoiceClip(response.voice_clip_id) {
                viewModelScope.launch {
                    delay(1500) // Show celebration details briefly
                    currentRoundIndex++
                    loadRound()
                }
            }
        } else {
            // Give visual-audio hint
            voicePlayerManager.playVoiceClip(response.voice_clip_id) {
                _thuliAnimationState.value = "idle"
            }
        }
    }

    fun restartGame() {
        currentRoundIndex = 0
        _gameFinished.value = false
        loadRound()
        playWelcomeMessage()
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        voicePlayerManager.destroy()
    }
}
