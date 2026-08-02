package com.example.infinityfitness.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class WhatsAppMessagingService(
    private val token: String,
    private val phoneNumberId: String
) {
    private val client = OkHttpClient()
    private val mapper = jacksonObjectMapper()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    @Throws(IOException::class)
    fun sendTextMessage(to: String, message: String) {
        val url = "https://graph.facebook.com/v22.0/$phoneNumberId/messages"

        val payload = mapOf(
            "messaging_product" to "whatsapp",
            "to" to to,
            "type" to "text",
            "text" to mapOf("body" to message)
        )

        val jsonBody = mapper.writeValueAsString(payload)
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                // Log the error body for debugging
                val errorBody = response.body?.string()
                val errorMsg = "Failed to send message to $to: ${response.code} $errorBody"
                android.util.Log.e("WhatsAppMessaging", errorMsg)
                throw IOException(errorMsg)
            } else {
                 android.util.Log.d("WhatsAppMessaging", "Message sent successfully to $to: ${response.body?.string()}")
            }
        }
    }

    @Throws(IOException::class)
    fun sendDocumentMessage(to: String, mediaId: String, filename: String, caption: String) {
        val url = "https://graph.facebook.com/v22.0/$phoneNumberId/messages"

        val payload = mapOf(
            "messaging_product" to "whatsapp",
            "to" to to,
            "type" to "document",
            "document" to mapOf(
                "id" to mediaId,
                "filename" to filename,
                "caption" to caption
            )
        )

        val jsonBody = mapper.writeValueAsString(payload)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                val errorMsg = "Failed to send document to $to : ${response.code} $errorBody"
                android.util.Log.e("WhatsAppMessaging", errorMsg)
                throw IOException(errorMsg)
             } else {
                 android.util.Log.d("WhatsAppMessaging", "Document sent successfully to $to : ${response.body?.string()}")
            }
        }
    }
}
