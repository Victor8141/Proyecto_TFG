package com.example.proyectotfg.datos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoPlantas {
    @Query("SELECT * FROM plantas ORDER BY fechaAlta DESC")
    fun observarPlantas(): Flow<List<EntidadPlanta>>

    @Transaction
    @Query("SELECT * FROM plantas WHERE origenApi = 0 ORDER BY fechaAlta DESC")
    fun observarHuertoConContenido(): Flow<List<PlantaConContenido>>

    @Query("SELECT * FROM plantas WHERE origenApi = 1 ORDER BY fechaAlta DESC")
    fun observarPlantasApiGuardadas(): Flow<List<EntidadPlanta>>

    @Transaction
    @Query("SELECT * FROM plantas WHERE id = :id")
    fun observarPlanta(id: Long): Flow<PlantaConContenido?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPlanta(planta: EntidadPlanta): Long

    @Update
    suspend fun actualizarPlanta(planta: EntidadPlanta)

    @Insert
    suspend fun insertarFoto(foto: EntidadFotoPlanta): Long

    @Insert
    suspend fun insertarNota(nota: EntidadNotaPlanta): Long

    @Query("DELETE FROM plantas WHERE id = :id")
    suspend fun borrarPlanta(id: Long)

    @Query("SELECT COUNT(*) FROM plantas WHERE perenualId IN (-1001, -1002)")
    suspend fun contarPlantillasPrueba(): Int

    @Query("SELECT * FROM plantas WHERE id = :id LIMIT 1")
    suspend fun obtenerPlanta(id: Long): EntidadPlanta?
}
