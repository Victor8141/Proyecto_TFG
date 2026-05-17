package com.example.proyectotfg.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyectotfg.datos.BaseDatosApp
import com.example.proyectotfg.datos.EntidadPlanta
import com.example.proyectotfg.datos.MapeadorPlanta
import com.example.proyectotfg.datos.PlantaConContenido
import com.example.proyectotfg.datos.PlantaApi
import com.example.proyectotfg.datos.RepositorioPlantas
import com.example.proyectotfg.notificaciones.ProgramadorNotificacionesRiego
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HuertoViewModel(
    private val repositorio: RepositorioPlantas,
    private val programadorRiego: ProgramadorNotificacionesRiego
) : ViewModel() {
    init {
        viewModelScope.launch {
            repositorio.sembrarPlantillasPrueba()
        }
    }

    val plantas: StateFlow<List<EntidadPlanta>> = repositorio.plantas.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val plantasConContenido: StateFlow<List<PlantaConContenido>> = repositorio.plantasConContenido.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val plantasApiGuardadas: StateFlow<List<EntidadPlanta>> = repositorio.plantasApiGuardadas.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    // Observa una planta concreta junto con sus fotos y notas.
    fun observarPlanta(id: Long) = repositorio.observarPlanta(id)

    // Crea una planta del huerto y programa su riego si hace falta.
    fun guardarPlanta(
        nomComun: String,
        clima: String,
        intervaloRiego: Int,
        unidadRiego: String,
        nomCient: String = "N/A",
        descrip: String = "Sin descripcion",
        urlImagenReferencia: String = "",
        perenualId: Int? = null,
        origenApi: Boolean = false,
        onSaved: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val planta = EntidadPlanta(
                perenualId = perenualId,
                nomComun = nomComun.ifBlank { "Planta sin nombre" },
                nomCient = nomCient.ifBlank { "N/A" },
                descrip = descrip.ifBlank { "Sin descripcion" },
                clima = clima.ifBlank { "No especificado" },
                intervaloRiego = intervaloRiego.coerceAtLeast(0),
                unidadRiego = unidadRiego.ifBlank { "dias" },
                urlImagenReferencia = urlImagenReferencia,
                origenApi = origenApi
            )
            val id = repositorio.guardarPlanta(planta)
            programadorRiego.programar(planta.copy(id = id))
            onSaved(id)
        }
    }

    // Guarda una planta obtenida desde la API como plantilla local.
    fun guardarDesdeApi(planta: PlantaApi, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repositorio.guardarPlanta(MapeadorPlanta.crearEntidad(planta, origenApi = true))
            onSaved(id)
        }
    }

    // Actualiza la frecuencia de riego y vuelve a programar el aviso.
    fun actualizarRiego(planta: EntidadPlanta, intervalo: Int, unidad: String) {
        viewModelScope.launch {
            val actualizada = planta.copy(
                intervaloRiego = intervalo.coerceAtLeast(0),
                unidadRiego = unidad.ifBlank { "dias" }
            )
            repositorio.actualizarPlanta(actualizada)
            programadorRiego.programar(actualizada)
        }
    }

    // Anade una nota a una planta si el texto no esta vacio.
    fun anadirNota(plantaId: Long, texto: String) {
        if (texto.isBlank()) return
        viewModelScope.launch {
            repositorio.anadirNota(plantaId, texto.trim())
        }
    }

    // Guarda la ruta de una foto tomada por el usuario.
    fun anadirFoto(plantaId: Long, uri: String) {
        if (uri.isBlank()) return
        viewModelScope.launch {
            repositorio.anadirFoto(plantaId, uri)
        }
    }

    // Borra una planta y cancela su notificacion de riego.
    fun borrarPlanta(plantaId: Long) {
        viewModelScope.launch {
            programadorRiego.cancelar(plantaId)
            repositorio.borrarPlanta(plantaId)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val baseDatos = BaseDatosApp.obtenerInstancia(application)
                HuertoViewModel(
                    RepositorioPlantas(baseDatos.daoPlantas()),
                    ProgramadorNotificacionesRiego(application.applicationContext)
                )
            }
        }
    }
}
