package com.example.proyectotfg.ui

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import com.example.proyectotfg.R
import com.example.proyectotfg.datos.PlantaApi
import com.example.proyectotfg.viewmodel.BuscadorPlantasViewModel
import com.example.proyectotfg.viewmodel.EstadoBusquedaPlantas

@Composable
// Pantalla que permite buscar plantas en la API.
fun PantallaLista(
    modifier: Modifier = Modifier,
    navigateToDetail: (Int) -> Unit,
    viewModel: BuscadorPlantasViewModel
) {
    var textoBusqueda by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it },
            label = { Text("Nombre de la planta...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.buscarPlantas(textoBusqueda) }),
            trailingIcon = {
                IconButton(onClick = { viewModel.buscarPlantas(textoBusqueda) }) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            when (val estado = viewModel.estadoBusqueda) {
                is EstadoBusquedaPlantas.Inicial -> {
                    Text("Busca una planta para comenzar", Modifier.align(Alignment.Center))
                }

                is EstadoBusquedaPlantas.Cargando -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                is EstadoBusquedaPlantas.Vacio -> {
                    Text("No se han encontrado resultados para '$textoBusqueda'", Modifier.align(Alignment.Center))
                }

                is EstadoBusquedaPlantas.Error -> {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hubo un error", color = Color.Red)
                        Button(onClick = { viewModel.buscarPlantas(textoBusqueda) }) {
                            Text("Reintentar")
                        }
                    }
                }

                is EstadoBusquedaPlantas.Exito -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(estado.plantas) { planta ->
                            TarjetaPlantaApi(planta = planta, onClick = { navigateToDetail(planta.id) })
                        }
                    }
                }
            }

        }
    }
}
@Composable
// Dibuja una tarjeta con los datos basicos de una planta de la API.
fun TarjetaPlantaApi(planta: PlantaApi, onClick: () -> Unit) {
    val imageModel: Any = when {
        !planta.default_image?.original_url.isNullOrEmpty() -> planta.default_image!!.original_url!!
        else -> R.drawable.planta_generica
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            AsyncImage(
                model = imageModel,
                contentDescription = planta.common_name,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.planta_generica)
            )
            Text(
                text = planta.common_name ?: "Desconocida",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
