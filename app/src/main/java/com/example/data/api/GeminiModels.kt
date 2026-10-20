package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null,
    val tools: List<Tool>? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val role: String? = null,
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    val mimeType: String,
    val data: String // base64
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = 0.7f,
    val topP: Float? = 0.95f,
    val topK: Int? = 40,
    val responseModalities: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class Tool(
    val googleSearch: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val promptFeedback: PromptFeedback? = null,
    val error: GeminiError? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null,
    val groundingMetadata: GroundingMetadata? = null
)

@JsonClass(generateAdapter = true)
data class GroundingMetadata(
    val webSearchQueries: List<String>? = null,
    val searchEntryPoint: SearchEntryPoint? = null,
    val groundingChunks: List<GroundingChunk>? = null
)

@JsonClass(generateAdapter = true)
data class SearchEntryPoint(
    val renderedContent: String? = null
)

@JsonClass(generateAdapter = true)
data class GroundingChunk(
    val web: WebChunk? = null
)

@JsonClass(generateAdapter = true)
data class WebChunk(
    val uri: String? = null,
    val title: String? = null
)

@JsonClass(generateAdapter = true)
data class PromptFeedback(
    val blockReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

// Simplified model for web search source item
data class WebSource(
    val title: String,
    val uri: String
)
