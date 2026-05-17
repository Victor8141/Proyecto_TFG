package com.example.proyectotfg

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.proyectotfg.ui.NavegacionApp
import com.example.proyectotfg.ui.theme.ProyectoTFGTheme

class ActividadPrincipal : ComponentActivity() {
    // Inicia la aplicacion y carga la interfaz principal.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pedirPermisosAplicacion()
        enableEdgeToEdge()
        setContent {
            ProyectoTFGTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ContenidoPrincipal(
                        nombre = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }


    // Pide los permisos necesarios para camara, notificaciones e imagenes.
    private fun pedirPermisosAplicacion() {
        val permisos = buildList {
            add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
                add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permisos.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permisos.toTypedArray(), 1001)
        }
    }
}

// Contenedor principal de la app.
@Composable
fun ContenidoPrincipal(nombre: String, modifier: Modifier = Modifier) {
    NavegacionApp()
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaPrincipal() {
    ProyectoTFGTheme {
        ContenidoPrincipal("Android")
    }
}
