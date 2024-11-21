package com.example.brewck.controllers

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.brewck.MainActivity
import com.example.brewck.components.NotificationReceiver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MenuPrincipalController(private val context: Context) {
    // Instâncias do Firestore e FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var contagemSujos: Int = 0

    // Constantes para identificação de preferências e permissões
    companion object {
        private const val REQUEST_CODE_NOTIFICATION_PERMISSION = 101
        private const val PREFS_NAME = "PREFS"
        private const val PREFS_CONTAGEM_SUJOS = "contagem_sujos"
    }

    // Função para recuperar o nome do usuário logado
    fun recuperarNomeDoUsuario(callback: (String?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userDocRef = firestore.collection("users").document(userId)
            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val nome = documentSnapshot.getString("nome")
                        callback(nome) // Passa o nome do usuário ou null
                    } else {
                        callback(null) // Se o documento não existir, retorna null
                    }
                }
        } else {
            callback(null) // Se o usuário não estiver logado, retorna null
        }
    }

    // Função para atualizar as contagens de barris por status
    fun atualizarContagens(onUpdate: (Int, Int, Int, Int) -> Unit) {
        contarBarrisPorStatus("Cheio") { cheiosCount ->
            contarBarrisPorStatus("Sujo") { sujosCount ->
                contarBarrisPorStatus("Limpo") { limposCount ->
                    contarBarrisPorStatus("No Cliente") { clienteCount ->
                        contagemSujos = sujosCount // Atualiza a contagem de barris sujos
                        atualizarPreferenciasContagemSujos() // Atualiza as preferências
                        onUpdate(cheiosCount, sujosCount, limposCount, clienteCount) // Passa os resultados para o callback
                    }
                }
            }
        }
    }

    // Função para contar barris de acordo com o status
    private fun contarBarrisPorStatus(status: String, onResult: (Int) -> Unit) {
        val email = auth.currentUser?.email
        if (email != null) {
            firestore.collection("barris")
                .whereEqualTo("email", email) // Filtra pelo email do usuário
                .whereEqualTo("status", status) // Filtra pelo status do barril
                .get()
                .addOnSuccessListener { result ->
                    onResult(result.size()) // Passa a quantidade de barris encontrados
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Erro ao contar barris: ${exception.message}", Toast.LENGTH_SHORT).show()
                    onResult(0) // Em caso de erro, passa 0
                }
        } else {
            Toast.makeText(context, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            onResult(0) // Se não houver um usuário autenticado, passa 0
        }
    }

    // Função para atualizar as preferências com a contagem de barris sujos
    private fun atualizarPreferenciasContagemSujos() {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt(PREFS_CONTAGEM_SUJOS, contagemSujos) // Armazena a contagem de barris sujos
            apply() // Salva as mudanças
        }
    }

    // Função para emitir um alerta se a porcentagem de barris sujos for maior que 50%
    fun emitirAlerta(onAlert: (Boolean) -> Unit) {
        contarBarrisPorStatus("Sujo") { sujosCount ->
            contarTotalBarris { totalCount ->
                if (totalCount > 0) {
                    val percentualSujos = (sujosCount.toDouble() / totalCount) * 100
                    onAlert(percentualSujos >= 50) // Alerta se a porcentagem for maior ou igual a 50%
                } else {
                    onAlert(false) // Caso não haja barris, não emite alerta
                }
            }
        }
    }

    // Função para contar o total de barris do usuário
    private fun contarTotalBarris(onResult: (Int) -> Unit) {
        val email = auth.currentUser?.email
        if (email != null) {
            firestore.collection("barris")
                .whereEqualTo("email", email) // Filtra pelo email do usuário
                .get()
                .addOnSuccessListener { result ->
                    onResult(result.size()) // Passa a quantidade total de barris
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Erro ao contar total de barris: ${exception.message}", Toast.LENGTH_SHORT).show()
                    onResult(0) // Em caso de erro, passa 0
                }
        } else {
            Toast.makeText(context, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            onResult(0) // Se não houver um usuário autenticado, passa 0
        }
    }

    // Função para criar um canal de notificações no Android 8.0 ou superior
    fun criarCanalDeNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "barris_notification_channel"
            val channelName = "Lembrete de Limpeza de Barris"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Notificações para lembrar o usuário de limpar barris sujos"
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel) // Cria o canal de notificações
        }
    }

    // Função para agendar uma notificação diária
    fun agendarNotificacaoDiaria() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 12 * 60 * 60 * 1000, // Começa 12 horas depois
            12 * 60 * 60 * 1000, // Repetição a cada 12 horas
            pendingIntent
        )
    }

    // Função para verificar se a notificação está agendada
    fun verificarNotificacao() {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val contagemSujos = sharedPreferences.getInt(PREFS_CONTAGEM_SUJOS, 0)

        if (contagemSujos > 0) {
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            if (pendingIntent == null) {
                agendarNotificacaoDiaria() // Agenda a notificação se não estiver agendada
            }
        }
    }

    // Função para deslogar o usuário e retornar à tela principal
    fun deslogarUsuario() {
        auth.signOut() // Desloga o usuário
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent) // Redireciona para a tela principal
    }

    // Função para verificar a permissão de envio de notificações
    fun verificarPermissaoNotificacao(onResult: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_NOTIFICATION_PERMISSION)
                onResult(false) // Se a permissão não foi concedida, solicita e retorna false
            } else {
                onResult(true) // Caso a permissão já tenha sido concedida, retorna true
            }
        } else {
            onResult(true) // Em versões abaixo de Android 13, sempre retorna true
        }
    }
}
