package com.example.proyectotfg.datos

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "plantas")
data class EntidadPlanta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val perenualId: Int? = null,
    val nomComun: String,
    val nomCient: String,
    val descrip: String,
    val clima: String,
    val intervaloRiego: Int = 0,
    val unidadRiego: String = "dias",
    val urlImagenReferencia: String,
    val fechaAlta: Long = System.currentTimeMillis(),
    val origenApi: Boolean = false
)

@Entity(
    tableName = "fotos_planta",
    foreignKeys = [
        ForeignKey(
            entity = EntidadPlanta::class,
            parentColumns = ["id"],
            childColumns = ["plantaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("plantaId")]
)
data class EntidadFotoPlanta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantaId: Long,
    val uri: String,
    @ColumnInfo(defaultValue = "0") val fecha: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notas_planta",
    foreignKeys = [
        ForeignKey(
            entity = EntidadPlanta::class,
            parentColumns = ["id"],
            childColumns = ["plantaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("plantaId")]
)
data class EntidadNotaPlanta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantaId: Long,
    val texto: String,
    val fecha: Long = System.currentTimeMillis()
)

data class PlantaConContenido(
    @androidx.room.Embedded val planta: EntidadPlanta,
    @androidx.room.Relation(
        parentColumn = "id",
        entityColumn = "plantaId"
    )
    val fotos: List<EntidadFotoPlanta>,
    @androidx.room.Relation(
        parentColumn = "id",
        entityColumn = "plantaId"
    )
    val notas: List<EntidadNotaPlanta>
)
