package com.runwayiq.ai

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

class GroqApiException(message: String) : Exception(message)

class GroqClient(apiKey: String) {

    private val apiKey = apiKey.trim()

    private val client = HttpClient(CIO) {
        engine { requestTimeout = 120_000 }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val endpoint = "https://api.groq.com/openai/v1/chat/completions"

    @Serializable
    data class Message(val role: String, val content: String)

    @Serializable
    private data class ChatMessage(val role: String, val content: String)

    @Serializable
    private data class GroqRequest(
        val model: String,
        val messages: List<ChatMessage>,
        val stream: Boolean,
    )

    fun streamResponse(
        systemPrompt: String,
        messages: List<Message>,
    ): Flow<String> = flow {
        val chatMessages = buildList {
            if (systemPrompt.isNotBlank()) {
                add(ChatMessage(role = "system", content = systemPrompt))
            }
            messages
                .filter { it.content.isNotBlank() }
                .forEach { msg ->
                    add(
                        ChatMessage(
                            role = when (msg.role) {
                                "assistant" -> "assistant"
                                else -> "user"
                            },
                            content = msg.content,
                        ),
                    )
                }
        }

        if (chatMessages.isEmpty() || (chatMessages.size == 1 && chatMessages[0].role == "system")) {
            throw GroqApiException("No messages to send.")
        }

        val request = GroqRequest(
            model = "llama-3.3-70b-versatile",
            messages = chatMessages,
            stream = true,
        )
        val requestBody = json.encodeToString(GroqRequest.serializer(), request)

        log("Groq request POST $endpoint")
        log("Groq request Authorization: Bearer ${maskApiKey(apiKey)}")
        log("Groq request body: $requestBody")

        val response = client.post(endpoint) {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            setBody(requestBody)
        }

        val rawBody = response.bodyAsText()
        log("Groq response status: ${response.status}")
        log("Groq response body:\n$rawBody")

        if (!response.status.isSuccess()) {
            throw GroqApiException(parseError(rawBody))
        }

        var emitted = false
        for (line in rawBody.lineSequence()) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue

            val payload = when {
                trimmedLine.startsWith("data: ") -> trimmedLine.removePrefix("data: ").trim()
                trimmedLine.startsWith("{") -> trimmedLine
                else -> {
                    log("Groq skipped non-data line: $trimmedLine")
                    continue
                }
            }
            if (payload.isEmpty() || payload == "[DONE]") continue

            val errorMessage = parseStreamError(payload)
            if (errorMessage != null) {
                throw GroqApiException(errorMessage)
            }

            extractStreamDelta(payload)?.let { text ->
                if (text.isNotBlank()) {
                    emitted = true
                    emit(text)
                }
            }
        }

        if (!emitted) {
            extractCompleteMessage(rawBody)?.let { text ->
                if (text.isNotBlank()) {
                    emitted = true
                    emit(text)
                }
            }
        }

        if (!emitted) {
            throw GroqApiException("Groq returned an empty response. Check your API key and try again.")
        }
    }

    private fun parseError(body: String): String {
        return try {
            json.parseToJsonElement(body).jsonObject["error"]?.jsonObject
                ?.get("message")?.jsonPrimitive?.contentOrNull
                ?: body.take(300)
        } catch (_: Exception) {
            body.take(300).ifBlank { "Request failed." }
        }
    }

    private fun parseStreamError(payload: String): String? {
        return try {
            val obj = json.parseToJsonElement(payload).jsonObject
            obj["error"]?.jsonObject?.get("message")?.jsonPrimitive?.contentOrNull
        } catch (_: Exception) {
            null
        }
    }

    private fun extractStreamDelta(payload: String): String? {
        return try {
            val obj = json.parseToJsonElement(payload).jsonObject
            obj["choices"]?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("delta")?.jsonObject
                ?.get("content")?.jsonPrimitive?.contentOrNull
        } catch (_: Exception) {
            null
        }
    }

    private fun extractCompleteMessage(body: String): String? {
        return try {
            val obj = json.parseToJsonElement(body.trim()).jsonObject
            obj["choices"]?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("message")?.jsonObject
                ?.get("content")?.jsonPrimitive?.contentOrNull
        } catch (_: Exception) {
            null
        }
    }

    private fun maskApiKey(key: String): String {
        if (key.length <= 8) return "***"
        return "${key.take(4)}...${key.takeLast(4)}"
    }

    private fun log(message: String) {
        println("[GroqClient] $message")
    }

    suspend fun complete(systemPrompt: String, messages: List<Message>): String {
        val sb = StringBuilder()
        streamResponse(systemPrompt, messages).collect { sb.append(it) }
        return sb.toString()
    }

    fun close() = client.close()
}
