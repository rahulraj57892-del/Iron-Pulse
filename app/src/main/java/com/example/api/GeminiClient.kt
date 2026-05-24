package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini API Moshi Models ---

data class Part(val text: String?)
data class Content(val parts: List<Part>)
data class GenerationConfig(val temperature: Float? = 0.7f)

data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = GenerationConfig()
)

data class Candidate(val content: Content?)
data class GenerateContentResponse(val candidates: List<Candidate>?)

// --- Retrofit Interface ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// --- API Client ---

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun getCoachResponse(systemInstruction: String, userPrompt: String): String {
        val fullPrompt = "$systemInstruction\n\nUser Question: $userPrompt"
        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = fullPrompt)))
            )
        )
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
                return "Connecting your trainer offline. Please configure your GEMINI_API_KEY in the AI Studio Secrets panel. Here's your localized AI tip: Keep pumping heavy rep sets, tracking pure hydration goals, and consuming high-protein breakfast, Siddharth!"
            }
            val response = apiService.generateContent(key, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "I am here, ready to push you to your absolute limits! Let's dominate this session."
        } catch (e: Exception) {
            "Iron Pulse AI: Let's power through! (Offline Tip: Dynamic warm-ups before progressive overload are critical for muscle growth. Keep hydrated & hit 1.5g protein/kg!)"
        }
    }
}
