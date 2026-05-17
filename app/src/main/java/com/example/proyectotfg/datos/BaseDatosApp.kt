package com.example.proyectotfg.datos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [EntidadPlanta::class, EntidadFotoPlanta::class, EntidadNotaPlanta::class],
    version = 2,
    exportSchema = false
)
abstract class BaseDatosApp : RoomDatabase() {
    abstract fun daoPlantas(): DaoPlantas

    companion object {
        @Volatile
        private var INSTANCIA: BaseDatosApp? = null

        // Devuelve una unica instancia de la base de datos para toda la app.
        fun obtenerInstancia(context: Context): BaseDatosApp {
            return INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    BaseDatosApp::class.java,
                    "plantas.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCIA = it }
            }
        }
    }
}
