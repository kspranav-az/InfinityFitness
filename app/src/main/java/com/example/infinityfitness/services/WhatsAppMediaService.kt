package com.example.infinityfitness.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class WhatsAppMediaService(
    private val token: String,
    private val phoneNumberId: String
) {
    private val client = OkHttpClient()
    private val mapper = jacksonObjectMapper()

    @Throws(IOException::class)
    fun uploadMedia(file: File): String {
        val url = "https://graph.facebook.com/v22.0/$phoneNumberId/media"

        val fileBody = file.asRequestBody("application/pdf".toMediaType())
        
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("messaging_product", "whatsapp")
            .addFormDataPart("file", file.name, fileBody)
            .build()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }

            val responseBody = response.body?.string() ?: throw IOException("Empty response")
            val json: Map<String, Any> = mapper.readValue(responseBody)
            
            return json["id"] as? String ?: throw IOException("Media ID not found in response")
        }
    }
}
