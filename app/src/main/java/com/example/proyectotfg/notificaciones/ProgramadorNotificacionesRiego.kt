package com.example.proyectotfg.notificaciones

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.proyectotfg.ActividadPrincipal
import com.example.proyectotfg.R
import com.example.proyectotfg.datos.EntidadPlanta

class ProgramadorNotificacionesRiego(private val context: Context) {
    private val gestorAlarmas = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Programa la siguiente alarma de riego para una planta del huerto.
    fun programar(planta: EntidadPlanta) {
        if (planta.origenApi || planta.intervaloRiego <= 0) {
            cancelar(planta.id)
            return
        }

        crearCanal()
        val triggerAt = System.currentTimeMillis() + planta.intervaloRiego.toMillis(planta.unidadRiego)
        val pendingIntent = pendingIntent(planta)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !gestorAlarmas.canScheduleExactAlarms()) {
            gestorAlarmas.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            gestorAlarmas.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    // Cancela la alarma asociada a una planta.
    fun cancelar(plantaId: Long) {
        val intent = Intent(context, ReceptorRecordatorioRiego::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            plantaId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        gestorAlarmas.cancel(pendingIntent)
    }

    // Muestra la notificacion cuando toca regar una planta.
    fun mostrarNotificacion(plantaId: Long, nombrePlanta: String) {
        crearCanal()
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val openAppIntent = Intent(context, ActividadPrincipal::class.java)
        val contentIntent = PendingIntent.getActivity(
            context,
            plantaId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Riego pendiente")
            .setContentText("Toca regar $nombrePlanta.")
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(plantaId.toInt(), notification)
    }

    // Prepara el PendingIntent que recibira el BroadcastReceiver.
    private fun pendingIntent(planta: EntidadPlanta): PendingIntent {
        val intent = Intent(context, ReceptorRecordatorioRiego::class.java).apply {
            putExtra(ReceptorRecordatorioRiego.EXTRA_PLANTA_ID, planta.id)
            putExtra(ReceptorRecordatorioRiego.EXTRA_PLANTA_NOMBRE, planta.nomComun)
            putExtra(ReceptorRecordatorioRiego.EXTRA_INTERVALO, planta.intervaloRiego)
            putExtra(ReceptorRecordatorioRiego.EXTRA_UNIDAD, planta.unidadRiego)
        }

        return PendingIntent.getBroadcast(
            context,
            planta.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Crea el canal necesario para notificaciones en Android 8 o superior.
    private fun crearCanal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Recordatorios de riego",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisos para regar las plantas del huerto"
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    // Convierte la frecuencia elegida por el usuario a milisegundos.
    private fun Int.toMillis(unidad: String): Long {
        val safeValue = coerceAtLeast(1).toLong()
        return when (unidad) {
            "segundos" -> safeValue * 1_000L
            "minutos" -> safeValue * 60_000L
            "horas" -> safeValue * 3_600_000L
            else -> safeValue * 86_400_000L
        }
    }

    companion object {
        const val CHANNEL_ID = "riego_recordatorios"
    }
}
