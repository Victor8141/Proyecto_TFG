package com.example.proyectotfg.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import com.example.proyectotfg.R
import com.example.proyectotfg.viewmodel.BuscadorPlantasViewModel
import com.example.proyectotfg.viewmodel.HuertoViewModel

@Composable
// Muestra el detalle de una planta obtenida desde la API.
fun PantallaDetalleApi(
    modifier: Modifier = Modifier,
    id: String,
    viewModel: BuscadorPlantasViewModel,
    localViewModel: HuertoViewModel,
    navigateBack: () -> Unit,
    navigateToDetail: (String) -> Unit,
    navigateToPlantaLocal: (Long) -> Unit
) {
    val context = LocalContext.current // Necesario para mostrar el Toast

    LaunchedEffect(id) {
        id.toIntOrNull()?.let { viewModel.obtenerDetalle(it) }
    }

    val planta = viewModel.plantaDetalle
    val descripcionWiki = viewModel.descripcionWiki

    fun obtenerNombreLimpio(nombre: String?): String {
        if (nombre.isNullOrEmpty()) return ""
        return nombre.replace("'", "").replace("\"", "").trim()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // IMAGEN
            val imageModel: Any = if (!planta?.default_image?.original_url.isNullOrEmpty()) {
                planta!!.default_image!!.original_url!!
            } else {
                R.drawable.planta_generica
            }

            AsyncImage(
                model = imageModel,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(350.dp),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.planta_generica)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = planta?.common_name ?: "Cargando...",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                val nombreCientificoOriginal = planta?.scientific_name?.firstOrNull() ?: ""

                Text(
                    text = nombreCientificoOriginal,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )

                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                Text(text = "Sobre esta planta", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text(text = "Descripción (vía Wikipedia)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = descripcionWiki, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    enabled = planta != null,
                    onClick = {
                        planta?.let {
                            localViewModel.guardarDesdeApi(it) { localId ->
                                Toast.makeText(context, "Planta guardada en tu huerto", Toast.LENGTH_SHORT).show()
                                navigateToPlantaLocal(localId)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar en mi huerto")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val nombreCientifico = planta?.scientific_name?.firstOrNull() ?: ""
                        val nombreLimpio = obtenerNombreLimpio(nombreCientifico)
                        val palabras = nombreLimpio.split(" ")
                        if (descripcionWiki.contains("No existe") || descripcionWiki.contains("No se encontró")) {

                            if (palabras.isNotEmpty()) {
                                val urlGenero = "https://es.wikipedia.org/wiki/${palabras[0]}"
                                val encodedUrl = URLEncoder.encode(urlGenero, StandardCharsets.UTF_8.toString())

                                Toast.makeText(context, "Redirigiendo a información general de ${palabras[0]}", Toast.LENGTH_SHORT).show()
                                navigateToDetail(encodedUrl)
                            }
                        } else {
                            val formatNombre = nombreLimpio.replace(" ", "_")
                            val urlCompleta = "https://es.wikipedia.org/wiki/$formatNombre"
                            val encodedUrl = URLEncoder.encode(urlCompleta, StandardCharsets.UTF_8.toString())

                            navigateToDetail(encodedUrl)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ver información en Wikipedia")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Botón Volver
        IconButton(
            onClick = { navigateBack() },
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart).background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
        }
    }
}

@Composable
fun InfoChip(modifier: Modifier = Modifier, label: String, value: String) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
