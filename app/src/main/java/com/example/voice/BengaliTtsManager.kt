package com.example.voice

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class BengaliTtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isBengaliSupported = MutableStateFlow(true)
    val isBengaliSupported: StateFlow<Boolean> = _isBengaliSupported.asStateFlow()

    private var activeUtteranceId: String? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            _isReady.value = true
            setupBengaliLanguage()

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    activeUtteranceId = utteranceId
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    activeUtteranceId = null
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    activeUtteranceId = null
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    activeUtteranceId = null
                }
            })
        } else {
            _isReady.value = false
        }
    }

    private fun setupBengaliLanguage() {
        val bengaliBD = Locale.forLanguageTag("bn-BD")
        val bengaliIN = Locale.forLanguageTag("bn-IN")

        val resBD = tts?.isLanguageAvailable(bengaliBD) ?: TextToSpeech.LANG_NOT_SUPPORTED
        if (resBD >= TextToSpeech.LANG_AVAILABLE) {
            tts?.language = bengaliBD
            _isBengaliSupported.value = true
            return
        }

        val resIN = tts?.isLanguageAvailable(bengaliIN) ?: TextToSpeech.LANG_NOT_SUPPORTED
        if (resIN >= TextToSpeech.LANG_AVAILABLE) {
            tts?.language = bengaliIN
            _isBengaliSupported.value = true
            return
        }

        // Bengali not installed or missing
        _isBengaliSupported.value = false
        tts?.language = Locale.getDefault()
    }

    fun speak(text: String, utteranceId: String = "kaka_speech_${System.currentTimeMillis()}") {
        if (!_isReady.value || tts == null) return

        stop()

        val cleanText = sanitizeForSpeech(text)
        if (cleanText.isBlank()) return

        val params = android.os.Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
        activeUtteranceId = null
    }

    fun openTtsSettings() {
        try {
            val intent = Intent("com.android.settings.TTS_SETTINGS").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                try {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
        }
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        _isReady.value = false
    }

    private fun sanitizeForSpeech(input: String): String {
        return input
            .replace(Regex("https?://\\S+"), "") // remove URLs
            .replace(Regex("[#*`_~>\\[\\]]"), "") // remove Markdown styling
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
