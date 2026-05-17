package com.example.proyectotfg.notificaciones

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.proyectotfg.datos.BaseDatosApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReceptorRecordatorioRiego : BroadcastReceiver() {
    // Se ejecuta cuando salta la alarma de riego.
    override fun onReceive(context: Context, intent: Intent) {
        val plantaId = intent.getLongExtra(EXTRA_PLANTA_ID, -1L)
        val nombre = intent.getStringExtra(EXTRA_PLANTA_NOMBRE).orEmpty()
        if (plantaId <= 0) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val programador = ProgramadorNotificacionesRiego(context.applicationContext)
                val baseDatos = BaseDatosApp.obtenerInstancia(context.applicationContext)
                val planta = baseDatos.daoPlantas().obtenerPlanta(plantaId)

                if (planta != null && !planta.origenApi && planta.intervaloRiego > 0) {
                    programador.mostrarNotificacion(planta.id, planta.nomComun.ifBlank { nombre })
                    programador.programar(planta)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_PLANTA_ID = "extra_planta_id"
        const val EXTRA_PLANTA_NOMBRE = "extra_planta_nombre"
        const val EXTRA_INTERVALO = "extra_intervalo"
        const val EXTRA_UNIDAD = "extra_unidad"
    }
}
