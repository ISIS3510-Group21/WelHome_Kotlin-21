package com.team21.myapplication.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {
    private val mediaJson = "application/json".toMediaType()
    private val endpoint = "https://api.cohere.ai/v1/chat"
    private val model = "command-a-03-2025"
    private val apiKey = "j08Gkzq9cgfCsbw7HUk9w9LcvTv0IOeQnDcINmAb"

    suspend fun generateListingDescription(prompt: String, maxTokens: Int = 180): Result<String> {
        // Validación temprana de API key
        if (apiKey.isNullOrBlank()) {
            Log.e("AiRepository", "COHERE_API_KEY is empty — check local.properties/gradle sync.")
            return Result.failure(IllegalStateException("Missing Cohere API key"))
        }

        val body = JSONObject().apply {
            put("model", model)   // puedes cambiar a "command-r" si lo tienes habilitado
            put("message", prompt)
            put("max_tokens", maxTokens)
            put("temperature", 0.5)
            put("k", 0)
            put("p", 0.0)
        }.toString().toRequestBody(mediaJson)

        val req = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Cohere-Version", "2022-12-06") // recomendable
            .post(body)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(req).execute().use { resp ->
                    val raw = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) {
                        Log.e("AiRepository", "AI HTTP ${resp.code}: $raw")
                        val message = when (resp.code) {
                            401, 403 -> "Invalid or unauthorized API key."
                            429 -> "Rate limit reached. Please wait a bit."
                            else -> "AI service error (${resp.code})."
                        }
                        return@withContext Result.failure(RuntimeException(message))
                    }
                    val json = JSONObject(raw)
                    val text = json.optString("text").trim()
                    if (text.isBlank()) {
                        Log.w("AiRepository", "Empty text from AI.")
                        return@withContext Result.failure(RuntimeException("Empty response"))
                    }
                    Result.success(text)
                }
            } catch (e: Exception) {
                Log.e("AiRepository", "AI call failed", e)
                Result.failure(e)
            }
        }
    }
}
