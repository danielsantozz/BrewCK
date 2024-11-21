package com.example.brewck.components

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.brewck.MenuPrincipal
import com.example.brewck.R

// A classe NotificationReceiver é responsável por receber os broadcasts de notificação e exibir uma notificação
class NotificationReceiver : BroadcastReceiver() {

    // Método chamado quando o Broadcast é recebido
    override fun onReceive(context: Context, intent: Intent) {

        // Recupera as preferências compartilhadas (shared preferences) para acessar dados persistidos
        val sharedPreferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)

        // Obtém a contagem de barris sujos, armazenada nas preferências compartilhadas
        val contagemSujos = sharedPreferences.getInt("contagem_sujos", 0)

        // Verifica se a contagem de barris sujos é maior que 0
        if (contagemSujos > 0) {
            val channelId = "barris_notification_channel" // ID do canal de notificação

            // Cria uma instância do NotificationManagerCompat para gerenciar as notificações
            val notificationManager = NotificationManagerCompat.from(context)

            // Verifica se as notificações estão habilitadas no dispositivo
            if (notificationManager.areNotificationsEnabled()) {

                // Cria um Intent para abrir a activity MenuPrincipal quando a notificação for clicada
                val notificationIntent = Intent(context, MenuPrincipal::class.java)
                notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                // Cria um PendingIntent para permitir que a notificação inicie a activity MenuPrincipal
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Cria a notificação com os parâmetros definidos, como ícone, título, texto e ação de clique
                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.brewck_logo) // Ícone da notificação
                    .setContentTitle("Lembrete de Limpeza") // Título da notificação
                    .setContentText("Você tem $contagemSujos barris sujos. Não se esqueça de limpá-los!") // Texto da notificação
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Prioridade da notificação
                    .setContentIntent(pendingIntent) // Intent a ser executada quando a notificação for clicada
                    .setAutoCancel(true) // Faz com que a notificação seja cancelada ao ser clicada
                    .build()

                // Exibe a notificação usando o NotificationManager
                notificationManager.notify(1, notification)
            }
        }
    }
}
