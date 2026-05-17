package com.example.proyectotfg.datos

class RepositorioPlantas(private val dao: DaoPlantas) {
    val plantas = dao.observarPlantas()
    val plantasConContenido = dao.observarHuertoConContenido()
    val plantasApiGuardadas = dao.observarPlantasApiGuardadas()

    fun observarPlanta(id: Long) = dao.observarPlanta(id)

    // Guarda una planta en Room y devuelve su identificador.
    suspend fun guardarPlanta(planta: EntidadPlanta): Long = dao.insertarPlanta(planta)

    // Actualiza los datos de una planta existente.
    suspend fun actualizarPlanta(planta: EntidadPlanta) = dao.actualizarPlanta(planta)

    // Guarda una nueva foto asociada a la planta indicada.
    suspend fun anadirFoto(plantaId: Long, uri: String) {
        dao.insertarFoto(EntidadFotoPlanta(plantaId = plantaId, uri = uri))
    }

    // Guarda una nota escrita por el usuario.
    suspend fun anadirNota(plantaId: Long, texto: String) {
        dao.insertarNota(EntidadNotaPlanta(plantaId = plantaId, texto = texto))
    }

    // Elimina una planta del huerto.
    suspend fun borrarPlanta(plantaId: Long) = dao.borrarPlanta(plantaId)

    // Obtiene una planta concreta para las notificaciones.
    suspend fun obtenerPlanta(plantaId: Long): EntidadPlanta? = dao.obtenerPlanta(plantaId)

    // Inserta plantillas de prueba si todavia no existen.
    suspend fun sembrarPlantillasPrueba() {
        if (dao.contarPlantillasPrueba() > 0) return

        dao.insertarPlanta(
            EntidadPlanta(
                perenualId = -1001,
                nomComun = "Tomate",
                nomCient = "Solanum lycopersicum",
                descrip = "Planta hortícola de ciclo cálido, sensible al frío y con alta necesidad de luz.",
                clima = "Sol directo y temperatura templada-cálida",
                intervaloRiego = 2,
                unidadRiego = "dias",
                urlImagenReferencia = "https://images.pexels.com/photos/1327838/pexels-photo-1327838.jpeg",
                origenApi = true
            )
        )

        dao.insertarPlanta(
            EntidadPlanta(
                perenualId = -1002,
                nomComun = "Girasol",
                nomCient = "Helianthus annuus",
                descrip = "Planta anual de crecimiento rápido que necesita muchas horas de luz.",
                clima = "Sol directo y ambiente exterior luminoso",
                intervaloRiego = 3,
                unidadRiego = "dias",
                urlImagenReferencia = "https://images.pexels.com/photos/33044/sunflower-sun-summer-yellow.jpg",
                origenApi = true
            )
        )
    }
}
