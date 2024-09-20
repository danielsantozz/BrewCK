package com.example.brewck.components

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.brewck.MenuPrincipal
import com.example.brewck.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sharedPreferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val contagemSujos = sharedPreferences.getInt("contagem_sujos", 0)

        if (contagemSujos > 0) {
            val channelId = "barris_notification_channel"

            
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                val notificationIntent = Intent(context, MenuPrincipal::class.java)
                notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.brewck_logo)
                    .setContentTitle("Lembrete de Limpeza")
                    .setContentText("Você tem $contagemSujos barris sujos. Não se esqueça de limpá-los!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(1, notification)
            } else {
                
                
            }
        }
    }
}
