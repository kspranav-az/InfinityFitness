package com.example.infinityfitness.services

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

import android.util.Log
import java.util.Properties

class WhatsAppReportService(private val context: Context) {

    private val pdfService = PdfService(context)
    private var mediaService: WhatsAppMediaService
    private var messagingService: WhatsAppMessagingService

    init {
        // Load properties from local.properties asset or raw resource in a real app, 
        // OR better yet, pass them in via constructor/DI.
        // For this specific request, we'll read them from a properties file logic or BuildConfig if available.
        // Since we added them to local.properties which is build-time only, we need them exposed to the app.
        // BUT, local.properties values are NOT accessible at runtime unless passed to BuildConfig.
        // I will assume for now we use the placeholders or pass them in.
        // Ideally, we should have added them to buildConfigField in build.gradle. kts. 
        // Let's assume they are passed/hardcoded for this step or we read from a resource.
        
        // TEMPORARY: Retrieving from likely BuildConfig or constants. 
        // Since we can't easily auto-generate BuildConfig without a gradle sync and I can't sync gradle,
        // I will use a helper to get them or expect them to be passed in.

        // For the purpose of this task, I will initialize them with the values found (or placeholders requiring replacement).
        // IN PRODUCTION: These should be in BuildConfig.
        // Initializing with secrets from BuildConfig
        val token = com.example.infinityfitness.BuildConfig.WHATSAPP_TOKEN
        val phoneId = com.example.infinityfitness.BuildConfig.PHONE_NUMBER_ID
        
        mediaService = WhatsAppMediaService(token, phoneId)
        messagingService = WhatsAppMessagingService(token, phoneId)
    }
    
    // Allow re-init if we want to pass keys dynamically (e.g. from UI for testing)
    fun init(token: String, phoneId: String) {
        mediaService = WhatsAppMediaService(token, phoneId)
        messagingService = WhatsAppMessagingService(token, phoneId)
    }

    // Flag to control file retention. 
    // If true ("on"), files are deleted after sending (No saving). 
    // If false, files are kept in ExternalFilesDir for debugging.
    private val deleteAfterSend = false 

    // Method to send an ALREADY generated PDF file (e.g. from a Preview)
    suspend fun sendPdf(
        phoneNumber: String,
        pdfFile: File,
        billFileName: String,
        caption: String,
        onProgress: (String) -> Unit,
        onError: (String) -> Unit,
        onSuccess: () -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                 // 1. Upload PDF
                 onProgress("Uploading PDF...")
                 val mediaId = mediaService.uploadMedia(pdfFile)
                 Log.d("WhatsApp", "Media Uploaded. ID: $mediaId")

                 // 2. Send Message
                 onProgress("Sending Message to $phoneNumber...")
                 Log.d("WhatsApp", "Sending document to: $phoneNumber")
                 messagingService.sendDocumentMessage(phoneNumber, mediaId, billFileName, caption)
                 Log.d("WhatsApp", "Message Sent Successfully to $phoneNumber")
                 
                 // Cleanup if flag is on
                 if (deleteAfterSend) {
                     if (pdfFile.exists()) {
                          pdfFile.delete()
                          Log.d("WhatsApp", "Temp PDF deleted per cleanup flag.")
                     }
                 } else {
                     Log.d("WhatsApp", "PDF saved at: ${pdfFile.absolutePath}")
                 }

                 withContext(Dispatchers.Main) {
                     onSuccess()
                 }
            } catch (e: Exception) {
                Log.e("WhatsApp", "Error in Upload/Send: ${e.message}")
                withContext(Dispatchers.Main) {
                    onError("Network Error: ${e.message}")
                }
            }
        }
    }

    suspend fun sendBill(
        phoneNumber: String,
        billHtml: String,
        billFileName: String,
        caption: String,
        onProgress: (String) -> Unit,
        onError: (String) -> Unit,
        onSuccess: () -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                // 1. Generate PDF
                // Using getExternalFilesDir(null) to match original HTML saving location
                val pdfFile = File(context.getExternalFilesDir(null), billFileName)
                
                onProgress("Generating PDF at ${pdfFile.absolutePath}...")
                
                // We need to wait for the callback-based PdfService. 
                // Since PdfService uses WebView (Main Thread), we need to switch context for that part.
            
                withContext(Dispatchers.Main) {
                     pdfService.htmlToPdf(billHtml, pdfFile, 
                        onComplete = { file -> 
                            // Go back to IO for networking
                            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    // 2. Upload PDF
                                    onProgress("Uploading PDF...")
                                    val mediaId = mediaService.uploadMedia(file)
                                    Log.d("WhatsApp", "Media Uploaded. ID: $mediaId")

                                    // 3. Send Message
                                    onProgress("Sending Message to $phoneNumber...")
                                    Log.d("WhatsApp", "Sending document to: $phoneNumber")
                                    messagingService.sendDocumentMessage(phoneNumber, mediaId, billFileName, caption)
                                    Log.d("WhatsApp", "Message Sent Successfully to $phoneNumber")
                                    
                                    // Cleanup if flag is on
                                    if (deleteAfterSend) {
                                        if (file.exists()) {
                                             file.delete()
                                             Log.d("WhatsApp", "Temp PDF deleted per cleanup flag.")
                                        }
                                    } else {
                                        Log.d("WhatsApp", "PDF saved at: ${file.absolutePath}")
                                    }

                                    withContext(Dispatchers.Main) {
                                        onSuccess()
                                    }
                                } catch (e: Exception) {
                                    Log.e("WhatsApp", "Error in Upload/Send: ${e.message}")
                                    withContext(Dispatchers.Main) {
                                        onError("Network Error: ${e.message}")
                                    }
                                }
                            }
                        },
                        onError = { e ->
                            onError("PDF Generation Error: ${e.message}")
                        }
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Error: ${e.message}")
                }
            }
        }
    }

    suspend fun sendText(phoneNumber: String, message: String) {
        withContext(Dispatchers.IO) {
            Log.d("WhatsApp", "Sending text to: $phoneNumber")
            messagingService.sendTextMessage(phoneNumber, message)
        }
    }
}
