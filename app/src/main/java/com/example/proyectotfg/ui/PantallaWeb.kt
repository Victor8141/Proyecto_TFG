package com.example.proyectotfg.ui


import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
// Muestra una pagina web dentro de la app.
fun PantallaWeb(url: String, navigateBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    webViewClient = WebViewClient()

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true

                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    loadUrl(url)
                }
            },
            update = { webView -> webView.loadUrl(url) },
            modifier = Modifier.fillMaxSize()
        )
        IconButton(
            onClick = { navigateBack() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }
    }
}
