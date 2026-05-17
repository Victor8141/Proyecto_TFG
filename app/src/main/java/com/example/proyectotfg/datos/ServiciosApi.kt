package com.example.proyectotfg.datos

import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface ServicioPerenual {
    // Busca plantas por nombre en la API de Perenual.
    @GET("api/species-list")
    suspend fun buscarPlantas(
        @Query("q") consulta: String,
        @Query("key") claveApi: String,
        @Query("page") pagina: Int = 1
    ): RespuestaPerenual

    // Obtiene el detalle completo de una planta concreta.
    @GET("api/species/details/{id}")
    suspend fun obtenerDetallePlanta(
        @Path("id") id: Int,
        @Query("key") claveApi: String
    ): ResponseBody

    companion object {
        private const val BASE_URL = "https://perenual.com/"

        // Crea el cliente Retrofit para acceder a Perenual.
        fun crear(): ServicioPerenual {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ServicioPerenual::class.java)
        }
    }
}
interface ServicioWikipedia {
    // Pide un resumen de Wikipedia para mostrar una descripcion sencilla.
    @Headers("User-Agent: MiAppDePlantasTFG/1.0 (contacto: tu-email@ejemplo.com)")
    @GET("api.php")
    suspend fun obtenerResumenPlanta(
        @Query("action") action: String = "query",
        @Query("format") format: String = "json",
        @Query("prop") prop: String = "extracts",
        @Query("exintro") exintro: Int = 1,
        @Query("explaintext") explaintext: Int = 1,
        @Query("titles") titles: String,
        @Query("redirects") redirects: Int = 1,
        @Query("origin") origin: String = "*"
    ): ResponseBody

    companion object {
        // Crea el cliente Retrofit para consultar Wikipedia.
        fun crear(): ServicioWikipedia {
            return Retrofit.Builder()
                .baseUrl("https://es.wikipedia.org/w/").build()
                .create(ServicioWikipedia::class.java)
        }
    }
}
