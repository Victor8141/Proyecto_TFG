package com.example.proyectotfg.datos

import com.google.gson.annotations.SerializedName

data class RespuestaPerenual(
    val data: List<PlantaApi>
)

data class PlantaApi(
    val id: Int,
    @SerializedName("common_name") val common_name: String?,
    @SerializedName("scientific_name") val scientific_name: List<String>?,
    @SerializedName("default_image") val default_image: ImagenPerenual?,
    @SerializedName("description") val description: String?,
    @SerializedName("watering") val watering: String?,
    @SerializedName("watering_period") val watering_period: String?,
    @SerializedName("sunlight") val sunlight: Any?,
)

data class ImagenPerenual(
    @SerializedName("original_url") val original_url: String?
)
