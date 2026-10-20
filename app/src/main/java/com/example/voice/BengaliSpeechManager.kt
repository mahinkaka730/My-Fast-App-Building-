package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class SpeechState {
    object Idle : SpeechState()
    object Listening : SpeechState()
    object Processing : SpeechState()
    data class Success(val spokenText: String) : SpeechState()
    data class Error(val errorMessage: String) : SpeechState()
}

class BengaliSpeechManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private val _rmsDb = MutableStateFlow(0f)
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(localeTag: String = "bn-BD") {
        if (!isAvailable) {
            _speechState.value = SpeechState.Error("আপনার ডিভাইসে স্পিচ রিকগনিশন সার্ভিস সক্রিয় নেই।")
            return
        }

        stopListening()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, localeTag)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "কাকা এআই শুনছে... বলুন")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            speechRecognizer?.startListening(intent)
            _speechState.value = SpeechState.Listening
        } catch (e: Exception) {
            _speechState.value = SpeechState.Error("ভয়েস রিকগনিশন শুরু করা যায়নি: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        if (_speechState.value is SpeechState.Listening) {
            _speechState.value = SpeechState.Idle
        }
    }

    fun resetState() {
        _speechState.value = SpeechState.Idle
        _rmsDb.value = 0f
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _speechState.value = SpeechState.Listening
            }

            override fun onBeginningOfSpeech() {
                _speechState.value = SpeechState.Listening
            }

            override fun onRmsChanged(rmsdB: Float) {
                _rmsDb.value = rmsdB
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _speechState.value = SpeechState.Processing
            }

            override fun onError(error: Int) {
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "মাইক্রোফোন অডিও রেকর্ডিংয়ে সমস্যা হয়েছে।"
                    SpeechRecognizer.ERROR_CLIENT -> "ক্লায়েন্ট অ্যাপ্লিকেশন ত্রুটি।"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "মাইক্রোফোন অনুমতি প্রয়োজন।"
                    SpeechRecognizer.ERROR_NETWORK -> "ইন্টারনেট সংযোগ বিচ্ছিন্ন।"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "ইন্টারনেট সময়সীমা শেষ।"
                    SpeechRecognizer.ERROR_NO_MATCH -> "কোনো কথা স্পষ্ট বোঝা যায়নি, অনুগ্রহ করে আবার বলুন।"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "ভয়েস ইঞ্জিন ব্যস্ত রয়েছে, একটু পর চেষ্টা করুন।"
                    SpeechRecognizer.ERROR_SERVER -> "সার্ভার প্রতিক্রিয়া দেয়নি।"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "কোনো কথা শোনা যায়নি।"
                    else -> "ভয়েস ইনপুট ত্রুটি (কোড $error)"
                }
                _speechState.value = SpeechState.Error(errorMsg)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull()?.trim()
                if (!spokenText.isNullOrBlank()) {
                    _speechState.value = SpeechState.Success(spokenText)
                } else {
                    _speechState.value = SpeechState.Error("কোনো কথা শোনা যায়নি।")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Keep listening active
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}
