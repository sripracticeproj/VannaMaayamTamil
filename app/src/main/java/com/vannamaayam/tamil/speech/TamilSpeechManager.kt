package com.vannamaayam.tamil.speech

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat

/**
 * Speech Manager to handle speech-to-text recognition specifically for Tamil (ta-IN).
 * Built with resilience and real-time audio volume callbacks suitable for children.
 */
class TamilSpeechManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognitionIntent: Intent? = null
    private var isListening = false

    init {
        initializeRecognizer()
    }

    private fun initializeRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ta-IN")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ta-IN")
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_IN_SIDEBAND, "ta-IN")
                // Resilience settings for toddler/child speech patterns:
                // Request partial results for instant responsive feedback,
                // and configure longer silence limits where supported.
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            }
        }
    }

    /**
     * Helper to verify audio recording permission
     */
    fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Start speech recognition with callbacks
     */
    fun startListening(
        onResult: (String) -> Unit,
        onPartialResult: (String) -> Unit,
        onRmsChanged: (Float) -> Unit,
        onListeningStateChanged: (Boolean) -> Unit,
        onError: (Int) -> Unit
    ) {
        if (!hasRecordAudioPermission()) {
            onError(SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS)
            return
        }

        if (speechRecognizer == null) {
            initializeRecognizer()
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                onListeningStateChanged(true)
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
                // Return sound levels to allow visually animating the mic button (great feedback for kids)
                onRmsChanged(rmsdB)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
                onListeningStateChanged(false)
            }

            override fun onError(error: Int) {
                isListening = false
                onListeningStateChanged(false)
                onError(error)
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                onListeningStateChanged(false)
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onResult(matches[0])
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onPartialResult(matches[0])
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(recognitionIntent)
    }

    /**
     * Stop listening and attempt to process current captured audio
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
    }

    /**
     * Cancel current recognition request completely
     */
    fun cancelListening() {
        speechRecognizer?.cancel()
        isListening = false
    }

    /**
     * Destroy speech recognizer resources
     */
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
