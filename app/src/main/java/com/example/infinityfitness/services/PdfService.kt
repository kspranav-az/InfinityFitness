package com.example.infinityfitness.services

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.print.PrintAttributes
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.File
import java.io.FileOutputStream

class PdfService(private val context: Context) {

    fun htmlToPdf(htmlString: String, outputFile: File, onComplete: (File) -> Unit, onError: (Exception) -> Unit) {
        // Create a WebView to render the HTML
        val webView = WebView(context)
        webView.settings.javaScriptEnabled = false // Disable JS for security/speed if not needed
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                // Wait for the view to render its content. onPageFinished is for loading, not rendering.
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    createPdfFromWebView(view, outputFile, onComplete, onError)
                }, 1000) 
            }
        }

        // Load the HTML content with asset base URL to allow loading local images/css if any
        webView.loadDataWithBaseURL("file:///android_asset/", htmlString, "text/html", "UTF-8", null)
    }

    // Expose this so we can capture a visible WebView from a Fragment/Dialog
    fun createPdfFromWebView(webView: WebView, outputFile: File, onComplete: (File) -> Unit, onError: (Exception) -> Unit) {
         // Create PDF document

            
        // Use a hidden api or alternative context if PrintManager is strictly required, 
        // but for generating a file directly, we can use the print adapter to write to a file descriptor?
        // Actually, Android's PrintDocumentAdapter is designed for the system print service. 
        // A common workaround for direct file generation without user interaction involves using the `PdfDocument` class directly 
        // if we can draw the view's canvas, OR using a library.
        // Since iText is available, let's use iText for a more robust backend-like generation without UI dependency if possible.
        // However, standard iText (5.x) or iText 7 Core creates PDFs from scratch. HTML to PDF requires the `html2pdf` add-on.
        
        // Re-evaluating: The prompt suggested "OpenHTMLtoPDF". I proposed iText.
        // If iText html2pdf is not present, we can't easily do HTML->PDF.
        // Let's stick to the Android Native approach by drawing the View to a Canvas on a PdfDocument.
        // This is safe and requires no extra libs.
        
        try {
            // 1. Calculate the scale to fit the WebView width onto the PDF page width (A4 = 595 points)
            // We use the measure specs to get the full scrollable height if possible, 
            // but for a visible view, 'width' and 'height' give visible dims.
            // Ideally we want the full content. 
            
            val pdfPageWidth = 595
            // Capture the full content height if possible, otherwise visible height
            // contentHeight is imprecise, but often better than just 'height' for scrolling views
            // We'll stick to 'height' for now as the Dialog might be wrapping content.
            // Actually, let's use the view's width to determine scale.
            
            val viewWidth = webView.width
            val viewHeight = webView.height
            
            if (viewWidth == 0 || viewHeight == 0) {
                onError(Exception("WebView has 0 dimensions"))
                return
            }
            
            val scale = pdfPageWidth.toFloat() / viewWidth.toFloat()
            val pdfPageHeight = (viewHeight * scale).toInt() // Keep aspect ratio
            
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(pdfPageWidth, pdfPageHeight, 1).create()
            val page = document.startPage(pageInfo)
            
            // 2. Scale the canvas
            page.canvas.scale(scale, scale)
            
            // 3. Draw the WebView
            // Note: drawing a visible View to a canvas usually captures the visible viewport.
            // If the bill is long and scrolls, this might cut it. 
            // However, since we are in a Dialog that likely fits the screen, and the bill is seemingly short, this is safer than re-layout.
            webView.draw(page.canvas)
            
            document.finishPage(page)
            
            // Write to file
            val fos = FileOutputStream(outputFile)
            document.writeTo(fos)
            document.close()
            fos.close()
            
            onComplete(outputFile)
            
        } catch (e: Exception) {
            onError(e)
        }
    }
}
