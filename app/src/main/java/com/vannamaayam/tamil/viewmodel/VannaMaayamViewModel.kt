package com.vannamaayam.tamil.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vannamaayam.tamil.audio.DynamicVoiceComposer
import com.vannamaayam.tamil.speech.TamilSpeechManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class AnimalColorRound(
    val animal: String,           
    val modelPath: String,        
    val meshName: String,         
    val targetColorTamil: String, 
    val colorHex: Long,           
    val transliterations: List<String>
)

class VannaMaayamViewModel(application: Application) : AndroidViewModel(application) {

    private val speechManager = TamilSpeechManager(application)
    private val voiceComposer = DynamicVoiceComposer(application)

    private val rounds = listOf(
        AnimalColorRound("singam", "", "", "மஞ்சள்", 0xFFFFF099, listOf("மஞ்சள்", "manjal", "yellow")),
        AnimalColorRound("thavalai", "", "", "பச்சை", 0xFFB5F2D2, listOf("பச்சை", "pachai", "green")),
        AnimalColorRound("paravai", "", "", "நீலம்", 0xFFAFE4FF, listOf("நீலம்", "neelam", "blue")),
        AnimalColorRound("muyal", "", "", "சிவப்பு", 0xFFFF9E80, listOf("சிவப்பு", "sivappu", "red"))
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
        if (currentRoundIndex < rounds.size) {
            val round = rounds[currentRoundIndex]
            _currentAnimal.value = round.animal
            _targetColorTamil.value = round.targetColorTamil
            _guessedColorHex.value = null
            _thuliAnimationState.value = "idle"
            _transcript.value = "இந்த விலங்கிற்கு '${round.targetColorTamil}' வண்ணம் பூசுவோமா?"
        } else {
            _gameFinished.value = true
            _thuliAnimationState.value = "celebrate"
            _transcript.value = "அற்புதம்! நீ ஒரு சிறந்த ஓவியர்!"
        }
    }

    private fun playWelcomeMessage() {
        _thuliAnimationState.value = "talking"
        val colorName = if (currentRoundIndex < rounds.size) rounds[currentRoundIndex].targetColorTamil else ""
        
        // As requested: Only use intro.mp3 for the agent voice
        // We play intro.mp3, and then use TTS for the dynamic color name if needed.
        // For simplicity and kid-friendliness, we play the intro carrier clip.
        voiceComposer.playDynamicInstruction("intro", colorName, null) {
            _thuliAnimationState.value = "idle"
        }
    }

    fun startListening() {
        if (_isListening.value) return
        _transcript.value = "உன் மந்திரக் குரலைச் சொல்..."
        _thuliAnimationState.value = "thinking"
        speechManager.startListening(
            onResult = { processSpeechResult(it) },
            onPartialResult = { _transcript.value = it },
            onRmsChanged = { _audioRms.value = it },
            onListeningStateChanged = { _isListening.value = it },
            onError = { 
                _isListening.value = false
                _thuliAnimationState.value = "idle"
                _transcript.value = "மீண்டும் ஒருமுறை சொல் தங்கம்!"
            }
        )
    }

    fun stopListening() = speechManager.stopListening()

    private fun processSpeechResult(transcript: String) {
        _transcript.value = transcript
        val cleanTranscript = transcript.trim().lowercase(Locale.ROOT)
        val currentRound = rounds[currentRoundIndex]

        val isCorrect = currentRound.transliterations.any { keyword ->
            cleanTranscript.contains(keyword.lowercase(Locale.ROOT))
        }

        if (isCorrect) {
            handleSuccess(currentRound)
        } else {
            handleHint(currentRound)
        }
    }

    private fun handleSuccess(round: AnimalColorRound) {
        _thuliAnimationState.value = "celebrate"
        _guessedColorHex.value = round.colorHex
        _transcript.value = "மிகச்சரியாக சொன்னாய்! ${round.targetColorTamil} வண்ணம் பூசியாகிவிட்டது!"

        // Use TTS for success feedback to keep the voice consistent without success.mp3
        viewModelScope.launch {
            delay(3000)
            currentRoundIndex++
            loadRound()
            playWelcomeMessage()
        }
    }

    private fun handleHint(round: AnimalColorRound) {
        _thuliAnimationState.value = "talking"
        _transcript.value = "தவறு தங்கம்! '${round.targetColorTamil}' என்று சொல் பார்க்கலாம்!"
        // Guide back to idle
        viewModelScope.launch {
            delay(2000)
            _thuliAnimationState.value = "idle"
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
        _debugLogs.value = (_debugLogs.value + message).takeLast(3)
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        voiceComposer.destroy()
    }
}
