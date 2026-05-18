package com.example.proyectotfg.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.proyectotfg.R
import com.example.proyectotfg.datos.EntidadNotaPlanta
import com.example.proyectotfg.datos.EntidadPlanta
import com.example.proyectotfg.datos.PlantaConContenido
import com.example.proyectotfg.viewmodel.HuertoViewModel
import java.io.File

@Composable
// Muestra la cuadricula principal con las plantas del huerto.
fun PantallaHuerto(
    huertoViewModel: HuertoViewModel,
    navegarACrear: () -> Unit,
    navegarADetalle: (Long) -> Unit
) {
    val context = LocalContext.current
    val plantas by huertoViewModel.plantasConContenido.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clickable { navegarACrear() },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Add, contentDescription = "Crear planta", modifier = Modifier.size(44.dp))
                        Text("Nueva planta", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(plantas) { item ->
            TarjetaPlantaHuerto(
                item = item,
                onClick = { navegarADetalle(item.planta.id) },
                onDelete = {
                    huertoViewModel.borrarPlanta(item.planta.id)
                    Toast.makeText(context, "Planta eliminada del huerto", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
// Muestra una planta del huerto en forma de tarjeta.
private fun TarjetaPlantaHuerto(item: PlantaConContenido, onClick: () -> Unit, onDelete: () -> Unit) {
    val ultimaFoto = item.fotos
        .filter { isUsableImagePath(it.uri) }
        .maxByOrNull { it.fecha }
        ?.uri
    val imageModel = ultimaFoto?.let { localImageModel(it) }
        ?: item.planta.urlImagenReferencia.takeIf { it.isNotBlank() }
        ?: R.drawable.planta_generica

    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f).clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Box {
            AsyncImage(
                model = imageModel,
                contentDescription = item.planta.nomComun,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.planta_generica)
            )
            Card(
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
            ) {
                Text(
                    text = item.planta.nomComun,
                    modifier = Modifier.padding(8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar planta")
            }
        }
    }
}

@Composable
// Muestra todos los datos de una planta guardada en el huerto.
fun PantallaDetalleLocal(
    plantaId: Long,
    huertoViewModel: HuertoViewModel,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current
    val plantaState by huertoViewModel.observarPlanta(plantaId).collectAsState(initial = null)
    var nuevaNota by remember { mutableStateOf("") }
    var notaAmpliada by remember { mutableStateOf<EntidadNotaPlanta?>(null) }
    var riegoEditable by remember { mutableStateOf("0") }
    var unidadRiegoEditable by remember { mutableStateOf("dias") }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            val path = guardarBitmapEnGaleriaLocal(context, it)
            huertoViewModel.anadirFoto(plantaId, path)
            Toast.makeText(context, "Foto añadida a la galería", Toast.LENGTH_SHORT).show()
        } ?: Toast.makeText(context, "Cámara no disponible", Toast.LENGTH_SHORT).show()
    }

    fun abrirCamaraSiDisponible() {
        val permisoConcedido = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (permisoConcedido) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Cámara no disponible", Toast.LENGTH_SHORT).show()
        }
    }

    val contenido = plantaState
    if (contenido == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val planta = contenido.planta
    val fotosOrdenadas = contenido.fotos.sortedByDescending { it.fecha }
    val notasOrdenadas = contenido.notas.sortedByDescending { it.fecha }
    val imagenCabecera = fotosOrdenadas.firstOrNull { isUsableImagePath(it.uri) }?.uri?.let { localImageModel(it) }
        ?: planta.urlImagenReferencia.takeIf { it.isNotBlank() }
        ?: R.drawable.planta_generica

    LaunchedEffect(planta.id, planta.intervaloRiego, planta.unidadRiego) {
        riegoEditable = planta.intervaloRiego.toString()
        unidadRiegoEditable = planta.unidadRiego
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box {
            AsyncImage(
                model = imagenCabecera,
                contentDescription = planta.nomComun,
                modifier = Modifier.fillMaxWidth().height(300.dp),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.planta_generica)
            )
            IconButton(onClick = navigateBack, modifier = Modifier.padding(12.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(planta.nomComun, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(planta.nomCient, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(planta.descrip, style = MaterialTheme.typography.bodyMedium)

            HorizontalDivider()

            RiegoEditor(
                intervalo = riegoEditable,
                unidad = unidadRiegoEditable,
                onIntervaloChange = { riegoEditable = it.filter(Char::isDigit) },
                onUnidadChange = { unidadRiegoEditable = it },
                onGuardar = {
                    huertoViewModel.actualizarRiego(
                        planta,
                        riegoEditable.toIntOrNull() ?: 0,
                        unidadRiegoEditable
                    )
                    Toast.makeText(context, "Frecuencia de riego actualizada", Toast.LENGTH_SHORT).show()
                }
            )

            InfoChip(label = "Clima y luz ideal", value = planta.clima, modifier = Modifier.fillMaxWidth())

            Text("Notas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = nuevaNota,
                    onValueChange = { nuevaNota = it },
                    label = { Text("Añadir nota") },
                    modifier = Modifier.weight(1f),
                    minLines = 1
                )
                IconButton(onClick = {
                    huertoViewModel.anadirNota(plantaId, nuevaNota)
                    if (nuevaNota.isNotBlank()) {
                        Toast.makeText(context, "Nota añadida", Toast.LENGTH_SHORT).show()
                        nuevaNota = ""
                    }
                }) {
                    Icon(Icons.Default.NoteAdd, contentDescription = "Añadir nota")
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(notasOrdenadas) { nota ->
                    Card(
                        modifier = Modifier.size(width = 180.dp, height = 110.dp).clickable { notaAmpliada = nota },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = nota.texto,
                            modifier = Modifier.padding(12.dp),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Galería", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                FilledTonalButton(onClick = { abrirCamaraSiDisponible() }) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Foto")
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(fotosOrdenadas) { foto ->
                    AsyncImage(
                        model = localImageModel(foto.uri),
                        contentDescription = "Foto de ${planta.nomComun}",
                        modifier = Modifier.size(150.dp).aspectRatio(1f),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.planta_generica)
                    )
                }
            }
        }
    }

    notaAmpliada?.let { nota ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { notaAmpliada = null },
            confirmButton = { TextButton(onClick = { notaAmpliada = null }) { Text("Cerrar") } },
            title = { Text("Nota") },
            text = { Text(nota.texto) }
        )
    }
}

@Composable
// Pantalla para crear una planta manualmente o desde una plantilla.
fun PantallaCrearPlanta(
    huertoViewModel: HuertoViewModel,
    navigateBack: () -> Unit,
    navigateToDetalle: (Long) -> Unit
) {
    val plantillasApi by huertoViewModel.plantasApiGuardadas.collectAsState()
    var plantillaSeleccionada by remember { mutableStateOf<EntidadPlanta?>(null) }
    var nomComun by remember { mutableStateOf("") }
    var clima by remember { mutableStateOf("") }
    var riego by remember { mutableStateOf("0") }
    var unidadRiego by remember { mutableStateOf("dias") }
    var nomCientPlantilla by remember { mutableStateOf("N/A") }
    var descripPlantilla by remember { mutableStateOf("Sin descripcion") }
    var imagenPlantilla by remember { mutableStateOf("") }
    var perenualIdPlantilla by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(plantillaSeleccionada?.id) {
        plantillaSeleccionada?.let {
            nomComun = it.nomComun
            clima = it.clima
            riego = it.intervaloRiego.toString()
            unidadRiego = it.unidadRiego
            nomCientPlantilla = it.nomCient
            descripPlantilla = it.descrip
            imagenPlantilla = it.urlImagenReferencia
            perenualIdPlantilla = it.perenualId
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = navigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Text("Crear planta", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        if (plantillasApi.isNotEmpty()) {
            Text("Rellenar desde plantas guardadas de la API", fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(plantillasApi) { plantilla ->
                    Card(
                        modifier = Modifier.size(width = 160.dp, height = 90.dp).clickable { plantillaSeleccionada = plantilla },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(Modifier.padding(10.dp)) {
                            Text(plantilla.nomComun, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                            Text(plantilla.nomCient, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        OutlinedTextField(nomComun, { nomComun = it }, label = { Text("Nombre común") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(clima, { clima = it }, label = { Text("Clima / exposición ideal") }, modifier = Modifier.fillMaxWidth())
        RiegoEditor(
            intervalo = riego,
            unidad = unidadRiego,
            onIntervaloChange = { riego = it.filter(Char::isDigit) },
            onUnidadChange = { unidadRiego = it },
            onGuardar = null
        )

        Button(
            onClick = {
                huertoViewModel.guardarPlanta(
                    nomComun = nomComun,
                    clima = clima,
                    intervaloRiego = riego.toIntOrNull() ?: 0,
                    unidadRiego = unidadRiego,
                    nomCient = nomCientPlantilla,
                    descrip = descripPlantilla,
                    urlImagenReferencia = imagenPlantilla,
                    perenualId = perenualIdPlantilla,
                    origenApi = false,
                    onSaved = navigateToDetalle
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Guardar planta")
        }
    }
}

@Composable
// Editor reutilizable para escoger cada cuanto se riega la planta.
private fun RiegoEditor(
    intervalo: String,
    unidad: String,
    onIntervaloChange: (String) -> Unit,
    onUnidadChange: (String) -> Unit,
    onGuardar: (() -> Unit)?
) {
    val unidades = listOf("segundos", "minutos", "horas", "dias")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = intervalo,
            onValueChange = onIntervaloChange,
            label = { Text("Frecuencia de riego") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailingIcon = {
                if (onGuardar != null) {
                    IconButton(onClick = onGuardar) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar riego")
                    }
                }
            }
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(unidades) { item ->
                FilterChip(
                    selected = unidad == item,
                    onClick = { onUnidadChange(item) },
                    label = { Text(item) }
                )
            }
        }
    }
}

@Composable
// Muestra las plantillas guardadas desde la API o las plantillas de prueba.
fun PantallaApiGuardadas(huertoViewModel: HuertoViewModel, navigateToCrear: () -> Unit) {
    val plantas by huertoViewModel.plantasApiGuardadas.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Plantas guardadas de la API", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Estas fichas sirven como plantilla para crear una planta editable en tu huerto.")

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(plantas) { planta ->
                Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().aspectRatio(0.85f)) {
                    AsyncImage(
                        model = planta.urlImagenReferencia.ifBlank { R.drawable.planta_generica },
                        contentDescription = planta.nomComun,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.planta_generica)
                    )
                    Column(Modifier.padding(8.dp)) {
                        Text(planta.nomComun, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(planta.nomCient, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        if (plantas.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = navigateToCrear) {
                    Text("Crear planta manual")
                }
            }
        }
    }
}

// Devuelve una imagen valida o la generica si el archivo ya no existe.
private fun localImageModel(path: String): Any {
    if (path.startsWith("http")) return path

    val file = File(path)
    return if (file.exists()) file else R.drawable.planta_generica
}

// Comprueba que una ruta local exista antes de usarla.
private fun isUsableImagePath(path: String): Boolean {
    return path.startsWith("http") || File(path).exists()
}

// Guarda una foto en la carpeta privada de la aplicacion.
private fun guardarBitmapEnGaleriaLocal(context: Context, bitmap: Bitmap): String {
    val directory = File(context.filesDir, "plant_photos").apply { mkdirs() }
    val file = File(directory, "planta_${System.currentTimeMillis()}.jpg")
    file.outputStream().use { output ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)
    }
    return file.absolutePath
}
