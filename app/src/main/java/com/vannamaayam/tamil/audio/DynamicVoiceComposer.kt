package com.vannamaayam.tamil.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.SUCCESS
import android.speech.tts.TextToSpeech.LANG_MISSING_DATA
import android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED
import android.speech.tts.TextToSpeech.QUEUE_FLUSH
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * Expert Audio Engine: Composes dynamic audio by stitching raw voice clips with TTS.
 */
class DynamicVoiceComposer(private val context: Context) {

    private var tts: TextToSpeech? = null
    private val voicePlayerManager = VoicePlayerManager(context)
    private var isTtsReady = false

    private class TtsInitListener(private val composer: DynamicVoiceComposer) : TextToSpeech.OnInitListener {
        override fun onInit(status: Int) {
            if (status == SUCCESS) {
                val currentTts = composer.tts
                if (currentTts != null) {
                    val tamilLocale = Locale("ta", "IN")
                    val result = currentTts.setLanguage(tamilLocale)
                    if (result == LANG_MISSING_DATA || result == LANG_NOT_SUPPORTED) {
                        Log.e("DynamicVoiceComposer", "Tamil language not supported for TTS")
                    } else {
                        composer.isTtsReady = true
                    }
                }
            } else {
                Log.e("DynamicVoiceComposer", "TTS Initialization failed")
            }
        }
    }

    init {
        tts = TextToSpeech(context, TtsInitListener(this))
    }

    fun playDynamicInstruction(
        introClip: String,
        targetColorTamil: String,
        tailClip: String? = null,
        onComplete: () -> Unit = {}
    ) {
        voicePlayerManager.playVoiceClip(introClip) {
            if (isTtsReady) {
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        if (tailClip != null) {
                            voicePlayerManager.playVoiceClip(tailClip, onComplete)
                        } else {
                            onComplete()
                        }
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        onComplete()
                    }
                    override fun onError(utteranceId: String?, errorCode: Int) {
                        onComplete()
                    }
                })
                tts?.speak(targetColorTamil, QUEUE_FLUSH, null, "color_injection")
            } else {
                if (tailClip != null) {
                    voicePlayerManager.playVoiceClip(tailClip, onComplete)
                } else {
                    onComplete()
                }
            }
        }
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        voicePlayerManager.destroy()
    }
}
