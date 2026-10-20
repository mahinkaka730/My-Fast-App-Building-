package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.controller.ActionResult
import com.example.controller.IntentActionController
import com.example.data.api.Content
import com.example.data.api.GeminiRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.InlineData
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.api.Tool
import com.example.data.api.WebSource
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.media.ImageProcessor
import com.example.media.PhotoFilter
import com.example.voice.BengaliSpeechManager
import com.example.voice.BengaliTtsManager
import com.example.voice.SpeechState
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val chatDao = db.chatDao()

    val speechManager = BengaliSpeechManager(application)
    val ttsManager = BengaliTtsManager(application)
    val actionController = IntentActionController(application)

    private val prefs = application.getSharedPreferences("by_kaka_settings", Context.MODE_PRIVATE)

    // Room persistent messages flow
    val chatMessages: StateFlow<List<ChatMessageEntity>> = chatDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // UI state
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingStatusText = MutableStateFlow("কাকা এআই চিন্তা করছে...")
    val loadingStatusText: StateFlow<String> = _loadingStatusText.asStateFlow()

    private val _selectedChatImage = MutableStateFlow<Bitmap?>(null)
    val selectedChatImage: StateFlow<Bitmap?> = _selectedChatImage.asStateFlow()

    // SMS Draft Dialog State
    private val _smsDraft = MutableStateFlow<Pair<String, String>?>(null) // recipient to message body
    val smsDraft: StateFlow<Pair<String, String>?> = _smsDraft.asStateFlow()

    // Confirmation Dialog State (action type to description)
    private val _confirmationState = MutableStateFlow<String?>(null)
    val confirmationState: StateFlow<String?> = _confirmationState.asStateFlow()

    // Settings State
    private val _autoSpeakTts = MutableStateFlow(prefs.getBoolean("auto_speak_tts", true))
    val autoSpeakTts: StateFlow<Boolean> = _autoSpeakTts.asStateFlow()

    private val _speechLocale = MutableStateFlow(prefs.getString("speech_locale", "bn-BD") ?: "bn-BD")
    val speechLocale: StateFlow<String> = _speechLocale.asStateFlow()

    private val _customApiKey = MutableStateFlow(prefs.getString("custom_api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    // Media Studio State
    private val _mediaOriginalBitmap = MutableStateFlow<Bitmap?>(null)
    val mediaOriginalBitmap: StateFlow<Bitmap?> = _mediaOriginalBitmap.asStateFlow()

    private val _mediaEditedBitmap = MutableStateFlow<Bitmap?>(null)
    val mediaEditedBitmap: StateFlow<Bitmap?> = _mediaEditedBitmap.asStateFlow()

    private val _activeFilter = MutableStateFlow(PhotoFilter.ORIGINAL)
    val activeFilter: StateFlow<PhotoFilter> = _activeFilter.asStateFlow()

    private val _activeRotation = MutableStateFlow(0f)
    val activeRotation: StateFlow<Float> = _activeRotation.asStateFlow()

    private val _selectedVideoUri = MutableStateFlow<Uri?>(null)
    val selectedVideoUri: StateFlow<Uri?> = _selectedVideoUri.asStateFlow()

    private val _aiImagePrompt = MutableStateFlow("")
    val aiImagePrompt: StateFlow<String> = _aiImagePrompt.asStateFlow()

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage: StateFlow<Boolean> = _isGeneratingImage.asStateFlow()

    private val _generatedAiBitmap = MutableStateFlow<Bitmap?>(null)
    val generatedAiBitmap: StateFlow<Bitmap?> = _generatedAiBitmap.asStateFlow()

    // Toast/Snackbar notifications in Bengali
    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents: SharedFlow<String> = _uiEvents.asSharedFlow()

    init {
        // Collect speech recognizer results
        viewModelScope.launch {
            speechManager.speechState.collect { state ->
                when (state) {
                    is SpeechState.Success -> {
                        _inputText.value = state.spokenText
                        processUserInput(state.spokenText, isVoice = true)
                        speechManager.resetState()
                    }
                    is SpeechState.Error -> {
                        _uiEvents.emit(state.errorMessage)
                    }
                    else -> {}
                }
            }
        }

        // Welcome message if chat is empty
        viewModelScope.launch {
            if (chatMessages.value.isEmpty()) {
                val count = withContext(Dispatchers.IO) {
                    // Check if DB has any message
                    AppDatabase.getDatabase(application).openHelper.readableDatabase
                }
                // Insert initial welcome
                val welcome = "নমস্কার/সালাম! আমি 'KAKA AI' (কাকা এআই), আপনার ব্যক্তিগত বাংলা ও ইংরেজি এআই ভয়েস অ্যাসিস্ট্যান্ট।\n\nআপনি আমাকে যেকোনো প্রশ্ন করতে পারেন, ভয়েস কমান্ড দিতে পারেন (যেমন: 'সময় বলো', 'YouTube খোলো', 'TikTok খোলো', 'মেসেজ লিখে দাও'), কিংবা ইন্টারনেটে যেকোনো তথ্য খুঁজতে বলতে পারেন।"
                chatDao.insertMessage(ChatMessageEntity(sender = "ai", content = welcome))
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun setChatImage(bitmap: Bitmap?) {
        _selectedChatImage.value = bitmap
    }

    fun toggleListening() {
        val current = speechManager.speechState.value
        if (current is SpeechState.Listening) {
            speechManager.stopListening()
        } else {
            ttsManager.stop()
            speechManager.startListening(_speechLocale.value)
        }
    }

    fun processUserInput(prompt: String, isVoice: Boolean = false) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank() && _selectedChatImage.value == null) return

        _inputText.value = ""
        val attachedImage = _selectedChatImage.value
        _selectedChatImage.value = null

        viewModelScope.launch {
            // Save user message to Room
            val userMsg = ChatMessageEntity(
                sender = "user",
                content = trimmed,
                isVoice = isVoice,
                imageUrl = attachedImage?.let { ImageProcessor.saveBitmapToCache(getApplication(), it)?.toString() }
            )
            chatDao.insertMessage(userMsg)

            // Step 1: Check if user input is an explicit Android Intent command
            val actionResult = actionController.executeCommand(trimmed)

            when (actionResult) {
                is ActionResult.Success -> {
                    val aiMsg = ChatMessageEntity(sender = "ai", content = actionResult.message, isVoice = false)
                    chatDao.insertMessage(aiMsg)
                    if (_autoSpeakTts.value) {
                        ttsManager.speak(actionResult.message)
                    }
                    return@launch
                }
                is ActionResult.TimeReport -> {
                    val aiMsg = ChatMessageEntity(sender = "ai", content = actionResult.timeInBengali, isVoice = false)
                    chatDao.insertMessage(aiMsg)
                    if (_autoSpeakTts.value) {
                        ttsManager.speak(actionResult.timeInBengali)
                    }
                    return@launch
                }
                is ActionResult.RequestSmsDraft -> {
                    _smsDraft.value = Pair(actionResult.recipient, actionResult.body)
                    val aiMsg = ChatMessageEntity(
                        sender = "ai",
                        content = "এসএমএস প্রেরণের জন্য একটি খসড়া প্রস্তুত করা হয়েছে। আপনি প্রাপকের নম্বর ও মেসেজ নিশ্চিত করে সেন্ড করুন।"
                    )
                    chatDao.insertMessage(aiMsg)
                    return@launch
                }
                is ActionResult.Failure -> {
                    // Continue to Gemini AI response
                }
            }

            // Step 2: Query Gemini AI with Google Search Grounding
            sendToGemini(trimmed, attachedImage)
        }
    }

    private suspend fun sendToGemini(userPrompt: String, attachedImage: Bitmap?) {
        _isLoading.value = true
        _loadingStatusText.value = "কাকা এআই উত্তর তৈরি করছে..."

        val effectiveApiKey = getEffectiveApiKey()
        if (effectiveApiKey.isBlank() || effectiveApiKey == "MY_GEMINI_API_KEY") {
            _isLoading.value = false
            val errorMsg = "⚠️ Gemini API কী (API Key) পাওয়া যায়নি!\n\nদয়া করে Settings ট্যাবে গিয়ে আপনার Google Gemini API Key প্রবেশ করান অথবা AI Studio Secrets প্যানেলে GEMINI_API_KEY কনফিগার করুন।"
            chatDao.insertMessage(ChatMessageEntity(sender = "ai", content = errorMsg))
            return
        }

        try {
            val partsList = mutableListOf<Part>()
            if (userPrompt.isNotBlank()) {
                partsList.add(Part(text = userPrompt))
            }
            if (attachedImage != null) {
                val b64 = ImageProcessor.bitmapToBase64(attachedImage)
                partsList.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = b64)))
            }

            val systemPrompt = "You are 'KAKA AI' (কাকা এআই), a highly capable, respectful, polite and intelligent personal AI assistant developed for Android. Your default and primary language is Bengali (বাংলা). If the user writes or speaks in Bengali, respond in fluent, natural and culturally appropriate Bengali. If the user uses English, you can reply in English. Explain topics clearly, be concise yet thorough, format with bullet points or paragraphs when helpful. When searching the internet, present current and accurate facts."

            val request = GeminiRequest(
                contents = listOf(
                    Content(role = "user", parts = partsList)
                ),
                systemInstruction = Content(
                    parts = listOf(Part(text = systemPrompt))
                ),
                generationConfig = GenerationConfig(
                    temperature = 0.7f,
                    topP = 0.95f,
                    topK = 40
                ),
                tools = listOf(Tool(googleSearch = emptyMap()))
            )

            val response = withContext(Dispatchers.IO) {
                RetrofitClient.geminiService.generateContent(effectiveApiKey, request)
            }

            _isLoading.value = false

            if (response.isSuccessful && response.body() != null) {
                val geminiResp = response.body()!!
                val candidate = geminiResp.candidates?.firstOrNull()
                val responseText = candidate?.content?.parts?.mapNotNull { it.text }?.joinToString("\n") ?: ""

                // Extract web grounding sources if available
                val webSources = candidate?.groundingMetadata?.groundingChunks?.mapNotNull { chunk ->
                    val uri = chunk.web?.uri
                    val title = chunk.web?.title ?: uri ?: ""
                    if (!uri.isNullOrBlank()) WebSource(title = title, uri = uri) else null
                }?.distinctBy { it.uri }

                val sourcesJson = if (!webSources.isNullOrEmpty()) {
                    val adapter = RetrofitClient.jsonParser.adapter<List<WebSource>>(
                        Types.newParameterizedType(List::class.java, WebSource::class.java)
                    )
                    adapter.toJson(webSources)
                } else null

                val finalOutput = if (responseText.isNotBlank()) {
                    responseText
                } else {
                    "কোনো উত্তর পাওয়া যায়নি। অনুগ্রহ করে আবার চেষ্টা করুন।"
                }

                val aiMsg = ChatMessageEntity(
                    sender = "ai",
                    content = finalOutput,
                    sourceLinksJson = sourcesJson
                )
                chatDao.insertMessage(aiMsg)

                if (_autoSpeakTts.value) {
                    ttsManager.speak(finalOutput)
                }
            } else {
                val errBody = response.errorBody()?.string() ?: ""
                val errText = "এআই রেসপন্সে সমস্যা হয়েছে (${response.code()}): ${response.message()}\n$errBody"
                chatDao.insertMessage(ChatMessageEntity(sender = "ai", content = errText))
            }
        } catch (e: Exception) {
            _isLoading.value = false
            val errorMsg = "নেটওয়ার্ক বা সংযোগ ত্রুটি: ${e.localizedMessage ?: "সার্ভার রেসপন্স করেনি। ইন্টারনেট সংযোগ পরীক্ষা করুন।"}"
            chatDao.insertMessage(ChatMessageEntity(sender = "ai", content = errorMsg))
        }
    }

    fun generateAiImage(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank()) return

        _isGeneratingImage.value = true
        _generatedAiBitmap.value = null

        viewModelScope.launch {
            val effectiveApiKey = getEffectiveApiKey()
            if (effectiveApiKey.isBlank() || effectiveApiKey == "MY_GEMINI_API_KEY") {
                _isGeneratingImage.value = false
                _uiEvents.emit("দয়া করে Settings-এ Gemini API Key প্রদান করুন।")
                return@launch
            }

            try {
                val request = GeminiRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = trimmed)))
                    ),
                    generationConfig = GenerationConfig(
                        responseModalities = listOf("TEXT", "IMAGE")
                    )
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.geminiService.generateImageContent(effectiveApiKey, request)
                }

                _isGeneratingImage.value = false

                if (response.isSuccessful && response.body() != null) {
                    val candidate = response.body()!!.candidates?.firstOrNull()
                    val imagePart = candidate?.content?.parts?.firstOrNull { it.inlineData != null }
                    val b64 = imagePart?.inlineData?.data

                    if (b64 != null) {
                        val bitmap = ImageProcessor.base64ToBitmap(b64)
                        _generatedAiBitmap.value = bitmap
                        _uiEvents.emit("ছবি সফলভাবে তৈরি হয়েছে!")
                    } else {
                        val text = candidate?.content?.parts?.firstOrNull()?.text
                        _uiEvents.emit(text ?: "ছবি তৈরি করা যায়নি। অন্য প্রম্পট দিয়ে চেষ্টা করুন।")
                    }
                } else {
                    _uiEvents.emit("ছবি জেনারেশন ব্যর্থ: ${response.message()}")
                }
            } catch (e: Exception) {
                _isGeneratingImage.value = false
                _uiEvents.emit("ছবি তৈরির সময় ত্রুটি: ${e.localizedMessage}")
            }
        }
    }

    // Media Editor actions
    fun loadMediaImage(uri: Uri) {
        viewModelScope.launch {
            val bmp = ImageProcessor.loadBitmapFromUri(getApplication(), uri)
            _mediaOriginalBitmap.value = bmp
            _mediaEditedBitmap.value = bmp
            _activeFilter.value = PhotoFilter.ORIGINAL
            _activeRotation.value = 0f
        }
    }

    fun applyFilterToMedia(filter: PhotoFilter) {
        val original = _mediaOriginalBitmap.value ?: return
        _activeFilter.value = filter
        viewModelScope.launch {
            val rotated = if (_activeRotation.value != 0f) {
                ImageProcessor.rotateBitmap(original, _activeRotation.value)
            } else original
            _mediaEditedBitmap.value = ImageProcessor.applyFilter(rotated, filter)
        }
    }

    fun rotateMedia90() {
        val original = _mediaOriginalBitmap.value ?: return
        val newRot = (_activeRotation.value + 90f) % 360f
        _activeRotation.value = newRot
        viewModelScope.launch {
            val rotated = ImageProcessor.rotateBitmap(original, newRot)
            _mediaEditedBitmap.value = ImageProcessor.applyFilter(rotated, _activeFilter.value)
        }
    }

    fun cropMediaSquare() {
        val current = _mediaEditedBitmap.value ?: return
        _mediaEditedBitmap.value = ImageProcessor.cropToSquare(current)
    }

    fun cropMedia4x3() {
        val current = _mediaEditedBitmap.value ?: return
        _mediaEditedBitmap.value = ImageProcessor.cropTo4x3(current)
    }

    fun setSelectedVideoUri(uri: Uri?) {
        _selectedVideoUri.value = uri
    }

    fun setAiImagePrompt(prompt: String) {
        _aiImagePrompt.value = prompt
    }

    // SMS confirmation
    fun dismissSmsDraft() {
        _smsDraft.value = null
    }

    fun confirmSendSms(recipient: String, body: String) {
        _smsDraft.value = null
        val res = actionController.sendSmsIntent(recipient, body)
        viewModelScope.launch {
            when (res) {
                is ActionResult.Success -> _uiEvents.emit(res.message)
                is ActionResult.Failure -> _uiEvents.emit(res.errorMessage)
                else -> {}
            }
        }
    }

    // Clear chat confirmation
    fun requestClearChat() {
        _confirmationState.value = "CLEAR_CHAT"
    }

    fun dismissConfirmation() {
        _confirmationState.value = null
    }

    fun confirmClearChat() {
        _confirmationState.value = null
        viewModelScope.launch {
            chatDao.clearAll()
            ttsManager.stop()
            _uiEvents.emit("সব চ্যাট হিস্ট্রি মুছে ফেলা হয়েছে।")
        }
    }

    // Settings actions
    fun setAutoSpeakTts(enabled: Boolean) {
        _autoSpeakTts.value = enabled
        prefs.edit().putBoolean("auto_speak_tts", enabled).apply()
    }

    fun setSpeechLocale(localeTag: String) {
        _speechLocale.value = localeTag
        prefs.edit().putString("speech_locale", localeTag).apply()
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key
        prefs.edit().putString("custom_api_key", key).apply()
    }

    private fun getEffectiveApiKey(): String {
        val custom = _customApiKey.value.trim()
        if (custom.isNotBlank()) return custom
        return BuildConfig.GEMINI_API_KEY
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.stopListening()
        ttsManager.shutdown()
    }
}
