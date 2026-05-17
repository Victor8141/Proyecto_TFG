package com.example.proyectotfg.datos

import com.example.proyectotfg.modelo.Planta

class MapeadorPlanta {
    companion object {
        // Convierte una planta de la API a un modelo simple de la aplicacion.
        fun crearPlanta(perenual: PlantaApi): Planta {

            val imgPrincipal = perenual.default_image?.original_url ?: ""
            val climaInfo = perenual.sunlight ?: "No especificado"

            return Planta(
                id = perenual.id,
                nomComun = perenual.common_name?.replaceFirstChar { it.uppercase() } ?: "Planta sin nombre",
                nomCient = perenual.scientific_name?.firstOrNull() ?: "N/A",
                descrip = perenual.description ?: "No hay descripción disponible para esta especie.",
                clima = "Exposición: $climaInfo",
                frecuenciaRiego = "Riego ${perenual.watering ?: "estándar"} (${perenual.watering_period ?: "según necesidad"})",
                urlImagen = imgPrincipal,
            )
        }

        // Convierte una planta de la API a entidad de Room para guardarla.
        fun crearEntidad(perenual: PlantaApi, origenApi: Boolean = true): EntidadPlanta {
            val planta = crearPlanta(perenual)

            return EntidadPlanta(
                perenualId = planta.id,
                nomComun = planta.nomComun,
                nomCient = planta.nomCient,
                descrip = planta.descrip,
                clima = planta.clima,
                intervaloRiego = perenual.aIntervaloRiego(),
                unidadRiego = "dias",
                urlImagenReferencia = planta.urlImagen,
                origenApi = origenApi
            )
        }

        // Traduce el texto de riego de la API a un numero aproximado de dias.
        private fun PlantaApi.aIntervaloRiego(): Int {
            return when (watering?.lowercase()) {
                "frequent" -> 1
                "average" -> 3
                "minimum" -> 7
                "none" -> 0
                else -> 0
            }
        }
    }
}
