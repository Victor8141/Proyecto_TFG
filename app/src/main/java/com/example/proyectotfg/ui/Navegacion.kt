package com.example.proyectotfg.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyectotfg.viewmodel.BuscadorPlantasViewModel
import com.example.proyectotfg.viewmodel.HuertoViewModel
import java.net.URLDecoder

private sealed class RutaInferior(
    val ruta: String,
    val etiqueta: String,
    val icono: ImageVector
) {
    data object Huerto : RutaInferior("huerto", "Huerto", Icons.Default.Home)
    data object Buscador : RutaInferior("buscador", "Buscar", Icons.Default.Search)
    data object ApiGuardadas : RutaInferior("api_guardadas", "API", Icons.Default.Star)
}

// Controla la navegacion principal y la barra inferior de la aplicacion.
@Composable
fun NavegacionApp(modifier: Modifier = Modifier) {
    val controladorNavegacion = rememberNavController()
    val huertoViewModel: HuertoViewModel = viewModel(factory = HuertoViewModel.Factory)
    val rutasInferiores = listOf(RutaInferior.Huerto, RutaInferior.Buscador, RutaInferior.ApiGuardadas)
    val entradaActual by controladorNavegacion.currentBackStackEntryAsState()
    val destinoActual = entradaActual?.destination
    val mostrarBarraInferior = rutasInferiores.any { it.ruta == destinoActual?.route }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (mostrarBarraInferior) {
                NavigationBar {
                    rutasInferiores.forEach { item ->
                        NavigationBarItem(
                            selected = destinoActual?.hierarchy?.any { it.route == item.ruta } == true,
                            onClick = {
                                controladorNavegacion.navigate(item.ruta) {
                                    popUpTo(controladorNavegacion.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icono, contentDescription = item.etiqueta) },
                            label = { Text(item.etiqueta) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = controladorNavegacion,
            startDestination = RutaInferior.Huerto.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(RutaInferior.Huerto.ruta) {
                PantallaHuerto(
                    huertoViewModel = huertoViewModel,
                    navegarACrear = { controladorNavegacion.navigate("crear_planta") },
                    navegarADetalle = { id -> controladorNavegacion.navigate("detalle_local/$id") }
                )
            }

            composable(RutaInferior.Buscador.ruta) {
                val buscadorViewModel: BuscadorPlantasViewModel = viewModel(factory = BuscadorPlantasViewModel.Factory)

                PantallaLista(
                    modifier = Modifier,
                    navigateToDetail = { id -> controladorNavegacion.navigate("detalle_api/$id") },
                    viewModel = buscadorViewModel
                )
            }

            composable(RutaInferior.ApiGuardadas.ruta) {
                PantallaApiGuardadas(
                    huertoViewModel = huertoViewModel,
                    navigateToCrear = { controladorNavegacion.navigate("crear_planta") }
                )
            }

            composable("crear_planta") {
                PantallaCrearPlanta(
                    huertoViewModel = huertoViewModel,
                    navigateBack = { controladorNavegacion.popBackStack() },
                    navigateToDetalle = { id ->
                        controladorNavegacion.navigate("detalle_local/$id") {
                            popUpTo(RutaInferior.Huerto.ruta)
                        }
                    }
                )
            }

            composable("detalle_local/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                PantallaDetalleLocal(
                    plantaId = id,
                    huertoViewModel = huertoViewModel,
                    navigateBack = { controladorNavegacion.popBackStack() }
                )
            }

            composable("detalle_api/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                val buscadorViewModel: BuscadorPlantasViewModel = viewModel(factory = BuscadorPlantasViewModel.Factory)
                PantallaDetalleApi(
                    id = id,
                    viewModel = buscadorViewModel,
                    localViewModel = huertoViewModel,
                    navigateBack = { controladorNavegacion.popBackStack() },
                    navigateToDetail = { url -> controladorNavegacion.navigate("web/$url") },
                    navigateToPlantaLocal = { localId -> controladorNavegacion.navigate("detalle_local/$localId") }
                )
            }

            composable("web/{url}") { backStackEntry ->
                val url = backStackEntry.arguments?.getString("url") ?: ""
                val decodedUrl = URLDecoder.decode(url, "UTF-8")
                PantallaWeb(url = decodedUrl, navigateBack = { controladorNavegacion.popBackStack() })
            }
        }
    }
}
