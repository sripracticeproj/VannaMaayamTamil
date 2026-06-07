package com.vannamaayam.tamil.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vannamaayam.tamil.audio.DynamicVoiceComposer
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
    val animal: String,           
    val modelPath: String,        // 3D model asset path (.glb)
    val meshName: String,         // Specific mesh to tint
    val targetColorTamil: String, 
    val colorHex: Long,           
    val transliterations: List<String>
)

class VannaMaayamViewModel(application: Application) : AndroidViewModel(application) {

    private val speechManager = TamilSpeechManager(application)
    private val voiceComposer = DynamicVoiceComposer(application)

    private val rounds = listOf(
        AnimalColorRound("singam", "models/lion.glb", "lion_body", "மஞ்சள்", 0xFFFFF099, listOf("மஞ்சள்", "manjal", "yellow")),
        AnimalColorRound("thavalai", "models/frog.glb", "frog_skin", "பச்சை", 0xFFB5F2D2, listOf("பச்சை", "pachai", "green")),
        AnimalColorRound("paravai", "models/bird.glb", "bird_feathers", "நீலம்", 0xFFAFE4FF, listOf("நீலம்", "neelam", "blue")),
        AnimalColorRound("muyal", "models/rabbit.glb", "rabbit_fur", "சிவப்பு", 0xFFFF9E80, listOf("சிவப்பு", "sivappu", "red"))
    )

    private var currentRoundIndex = 0

    // Live UI States
    private val _currentAnimal = MutableStateFlow("")
    val currentAnimal: StateFlow<String> = _currentAnimal.asStateFlow()

    private val _currentModelPath = MutableStateFlow("")
    val currentModelPath: StateFlow<String> = _currentModelPath.asStateFlow()

    private val _targetMeshName = MutableStateFlow<String?>(null)
    val targetMeshName: StateFlow<String?> = _targetMeshName.asStateFlow()

    private val _targetColorTamil = MutableStateFlow("")
    val targetColorTamil: StateFlow<String> = _targetColorTamil.asStateFlow()

    private val _guessedColorHex = MutableStateFlow<Long?>(null)
    val guessedColorHex: StateFlow<Long?> = _guessedColorHex.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _thuliAnimationState = MutableStateFlow("idle")
    val thuliAnimationState: StateFlow<String> = _thuliAnimationState.asStateFlow()

    private val _gameFinished = MutableStateFlow(false)
    val gameFinished: StateFlow<Boolean> = _gameFinished.asStateFlow()

    private val _debugLogs = MutableStateFlow<List<String>>(emptyList())
    val debugLogs: StateFlow<List<String>> = _debugLogs.asStateFlow()

    init {
        loadRound()
        playWelcomeMessage()
    }

    private fun loadRound() {
        addLog("Loading round: $currentRoundIndex")
        if (currentRoundIndex < rounds.size) {
            val round = rounds[currentRoundIndex]
            _currentAnimal.value = round.animal
            _currentModelPath.value = round.modelPath
            _targetColorTamil.value = round.targetColorTamil
            _guessedColorHex.value = null
            _targetMeshName.value = null
            _thuliAnimationState.value = "idle"
        } else {
            _gameFinished.value = true
            _thuliAnimationState.value = "celebrate"
        }
    }

    private fun playWelcomeMessage() {
        _thuliAnimationState.value = "talking"
        // Use DynamicVoiceComposer for mixed playback
        voiceComposer.playDynamicInstruction("intro", _targetColorTamil.value, "hint") {
            _thuliAnimationState.value = "idle"
        }
    }

    fun startListening() {
        if (_isListening.value) return
        _transcript.value = ""
        _thuliAnimationState.value = "thinking"
        speechManager.startListening(
            onResult = { processSpeechResult(it) },
            onPartialResult = { _transcript.value = it },
            onRmsChanged = { _audioRms.value = it },
            onListeningStateChanged = { _isListening.value = it },
            onError = { _isListening.value = false; _thuliAnimationState.value = "idle" }
        )
    }

    fun stopListening() = speechManager.stopListening()

    private fun processSpeechResult(transcript: String) {
        _transcript.value = transcript
        val cleanTranscript = transcript.trim().lowercase(Locale.ROOT)
        val currentRound = rounds[currentRoundIndex]

        val state = GameState(
            current_3d_model = currentRound.modelPath,
            target_color_tamil = currentRound.targetColorTamil,
            child_audio_transcript = transcript
        )

        val isCorrect = currentRound.transliterations.any { cleanTranscript.contains(it) }

        val agentResponse = if (isCorrect) {
            ThuliAgentResponse(
                status = ThuliStatus.SUCCESS,
                target_mesh_part = currentRound.meshName,
                hex_color_code = String.format("#%06X", (0xFFFFFF and currentRound.colorHex.toInt())),
                dynamic_speech_payload = currentRound.targetColorTamil,
                animation_state = "celebrate",
                voice_clip_id = "success"
            )
        } else {
            ThuliAgentResponse(
                status = ThuliStatus.HINT,
                animation_state = "talking",
                voice_clip_id = "hint"
            )
        }

        applyAgentResponse(agentResponse, currentRound)
    }

    private fun applyAgentResponse(response: ThuliAgentResponse, round: AnimalColorRound) {
        _thuliAnimationState.value = response.animation_state

        if (response.status == ThuliStatus.SUCCESS) {
            _guessedColorHex.value = round.colorHex
            _targetMeshName.value = response.target_mesh_part
            
            // Dynamic Audio: Play success clip and reinforce the color name
            voiceComposer.playDynamicInstruction(response.voice_clip_id, response.dynamic_speech_payload ?: "") {
                viewModelScope.launch {
                    delay(2000)
                    currentRoundIndex++
                    loadRound()
                }
            }
        } else {
            voiceComposer.playDynamicInstruction("hint", round.targetColorTamil) {
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

    private fun addLog(message: String) {
        Log.d("VannaMaayam", message)
        _debugLogs.value = (_debugLogs.value + message).takeLast(5)
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        voiceComposer.destroy()
    }
}
