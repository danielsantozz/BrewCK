package com.example.brewck

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
import com.example.brewck.components.NotificationReceiver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MenuPrincipalController(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var contagemSujos: Int = 0

    companion object {
        private const val REQUEST_CODE_NOTIFICATION_PERMISSION = 101
        private const val PREFS_NAME = "PREFS"
        private const val PREFS_CONTAGEM_SUJOS = "contagem_sujos"
    }

    fun recuperarNomeDoUsuario(callback: (String?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userDocRef = firestore.collection("users").document(userId)
            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val nome = documentSnapshot.getString("nome")
                        callback(nome)
                    } else {
                        callback(null)
                    }
                }
        } else {
            callback(null)
        }
    }

    fun atualizarContagens(onUpdate: (Int, Int, Int, Int) -> Unit) {
        contarBarrisPorStatus("Cheio") { cheiosCount ->
            contarBarrisPorStatus("Sujo") { sujosCount ->
                contarBarrisPorStatus("Limpo") { limposCount ->
                    contarBarrisPorStatus("No Cliente") { clienteCount ->
                        contagemSujos = sujosCount
                        atualizarPreferenciasContagemSujos()
                        onUpdate(cheiosCount, sujosCount, limposCount, clienteCount)
                    }
                }
            }
        }
    }

    private fun contarBarrisPorStatus(status: String, onResult: (Int) -> Unit) {
        val email = auth.currentUser?.email
        if (email != null) {
            firestore.collection("barris")
                .whereEqualTo("email", email)
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener { result ->
                    onResult(result.size())
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Erro ao contar barris: ${exception.message}", Toast.LENGTH_SHORT).show()
                    onResult(0)
                }
        } else {
            Toast.makeText(context, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            onResult(0)
        }
    }

    private fun atualizarPreferenciasContagemSujos() {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt(PREFS_CONTAGEM_SUJOS, contagemSujos)
            apply()
        }
    }

    fun emitirAlerta(onAlert: (Boolean) -> Unit) {
        contarBarrisPorStatus("Sujo") { sujosCount ->
            contarTotalBarris { totalCount ->
                if (totalCount > 0) {
                    val percentualSujos = (sujosCount.toDouble() / totalCount) * 100
                    onAlert(percentualSujos >= 50)
                } else {
                    onAlert(false)
                }
            }
        }
    }

    private fun contarTotalBarris(onResult: (Int) -> Unit) {
        val email = auth.currentUser?.email
        if (email != null) {
            firestore.collection("barris")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { result ->
                    onResult(result.size())
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Erro ao contar total de barris: ${exception.message}", Toast.LENGTH_SHORT).show()
                    onResult(0)
                }
        } else {
            Toast.makeText(context, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            onResult(0)
        }
    }

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
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun agendarNotificacaoDiaria() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 12 * 60 * 60 * 1000,
            12 * 60 * 60 * 1000,
            pendingIntent
        )
    }

    fun verificarNotificacao() {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val contagemSujos = sharedPreferences.getInt(PREFS_CONTAGEM_SUJOS, 0)

        if (contagemSujos > 0) {
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            if (pendingIntent == null) {
                agendarNotificacaoDiaria()
            }
        }
    }

    fun deslogarUsuario() {
        auth.signOut()
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    fun verificarPermissaoNotificacao(onResult: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_NOTIFICATION_PERMISSION)
                onResult(false)
            } else {
                onResult(true)
            }
        } else {
            onResult(true)
        }
    }
}
