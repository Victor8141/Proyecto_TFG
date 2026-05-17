package com.example.proyectotfg.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.proyectotfg.datos.PlantaApi
import com.example.proyectotfg.datos.ServicioPerenual
import com.example.proyectotfg.datos.ServicioWikipedia
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed interface EstadoBusquedaPlantas {
    object Inicial : EstadoBusquedaPlantas
    object Cargando : EstadoBusquedaPlantas
    data class Exito(val plantas: List<PlantaApi>) : EstadoBusquedaPlantas
    object Vacio : EstadoBusquedaPlantas
    data class Error(val mensaje: String) : EstadoBusquedaPlantas
}

class BuscadorPlantasViewModel(private val servicio: ServicioPerenual) : ViewModel() {

    private val servicioWikipedia = ServicioWikipedia.crear()

    var estadoBusqueda by mutableStateOf<EstadoBusquedaPlantas>(EstadoBusquedaPlantas.Inicial)
    var plantaDetalle by mutableStateOf<PlantaApi?>(null)

    var descripcionWiki by mutableStateOf("Buscando informacion en Wikipedia...")
        private set

    private val claveApi = "sk-NK1t69fb5a0c65d2b16595"

    // Busca plantas en Perenual y actualiza el estado de la pantalla.
    fun buscarPlantas(consulta: String) {
        if (consulta.isBlank()) return

        viewModelScope.launch {
            estadoBusqueda = EstadoBusquedaPlantas.Cargando
            try {
                val respuesta = servicio.buscarPlantas(consulta = consulta, claveApi = claveApi)

                estadoBusqueda = if (respuesta.data.isEmpty()) {
                    EstadoBusquedaPlantas.Vacio
                } else {
                    EstadoBusquedaPlantas.Exito(respuesta.data)
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", e.message.toString())
                estadoBusqueda = EstadoBusquedaPlantas.Error("Error de conexion: ${e.message}")
            }
        }
    }

    // Obtiene el detalle de una planta y busca una descripcion en Wikipedia.
    fun obtenerDetalle(id: Int) {
        viewModelScope.launch {
            plantaDetalle = null
            descripcionWiki = "Buscando informacion en Wikipedia..."

            try {
                val respuesta = servicio.obtenerDetallePlanta(id, claveApi)
                val textoJson = respuesta.string()
                val planta = Gson().fromJson(textoJson, PlantaApi::class.java)
                plantaDetalle = planta
                val nombreCientifico = planta.scientific_name?.firstOrNull()

                if (!nombreCientifico.isNullOrEmpty()) {
                    consultarWikipedia(
                        nombreCientifico
                            .replace("'", "")
                            .replace("\"", "")
                            .trim()
                    )
                } else {
                    descripcionWiki = "No hay nombre cientifico disponible."
                }
            } catch (e: Exception) {
                descripcionWiki = "Error al obtener detalles."
            }
        }
    }

    // Consulta Wikipedia y reintenta con el genero si la especie exacta no existe.
    private suspend fun consultarWikipedia(nombreCientifico: String) {
        try {
            val respuesta = servicioWikipedia.obtenerResumenPlanta(titles = nombreCientifico)
            val jsonObject = JSONObject(respuesta.string())
            val paginas = jsonObject.getJSONObject("query").getJSONObject("pages")
            val primeraClave = paginas.keys().next()

            if (primeraClave == "-1") {
                val palabras = nombreCientifico.split(" ")
                if (palabras.size > 1) {
                    consultarWikipedia(palabras[0])
                } else {
                    descripcionWiki = "No se encontro informacion para '$nombreCientifico'."
                }
            } else {
                val pagina = paginas.getJSONObject(primeraClave)
                descripcionWiki = pagina.optString("extract", "Sin resumen disponible.")
            }
        } catch (e: Exception) {
            descripcionWiki = "Error al conectar con Wikipedia."
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return BuscadorPlantasViewModel(ServicioPerenual.crear()) as T
            }
        }
    }
}
