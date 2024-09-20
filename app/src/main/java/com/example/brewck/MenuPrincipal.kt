package com.example.brewck

import FirebaseRepository
import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.brewck.components.NotificationReceiver

class MenuPrincipal : AppCompatActivity() {
    private lateinit var btnBarris: ConstraintLayout
    private lateinit var btnClientes: ConstraintLayout
    private lateinit var btnQR: ConstraintLayout
    private lateinit var btnSair: ConstraintLayout

    private lateinit var txtBarrisLimpos: TextView
    private lateinit var txtBarrisSujos: TextView
    private lateinit var txtBarrisCheios: TextView
    private lateinit var txtBarrisCliente: TextView

    private lateinit var txtUsuario: TextView

    private lateinit var firestore: FirebaseFirestore
    private val repository = FirebaseRepository()

    private var contagemSujos: Int = 0

    companion object {
        private const val REQUEST_CODE_NOTIFICATION_PERMISSION = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_principal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()

        txtUsuario = findViewById(R.id.txtUsuario)
        repository.recuperarNomeDoUsuario { nome ->
            nome?.let {
                txtUsuario.text = "Olá, $it."
            } ?: run {
                Toast.makeText(this, "Nome não encontrado.", Toast.LENGTH_SHORT).show()
            }
        }

        txtBarrisCheios = findViewById(R.id.txtBarrisCheios)
        txtBarrisSujos = findViewById(R.id.txtBarrisSujos)
        txtBarrisCliente = findViewById(R.id.txtBarrisCliente)
        txtBarrisLimpos = findViewById(R.id.txtBarrisLimpos)

        btnBarris = findViewById(R.id.btnBarris)
        btnClientes = findViewById(R.id.btnClientes)
        btnQR = findViewById(R.id.btnQR)
        btnSair = findViewById(R.id.btnSair)

        btnBarris.setOnClickListener {
            val intent = Intent(this, Barris::class.java)
            startActivity(intent)
        }
        btnClientes.setOnClickListener {
            val intent = Intent(this, Clientes::class.java)
            startActivity(intent)
        }
        btnQR.setOnClickListener {
            val intent = Intent(this, QRCode::class.java)
            startActivity(intent)
        }
        btnSair.setOnClickListener {
            deslogarUsuario()
        }
        txtUsuario.setOnClickListener {
            val intent = Intent(this, Configuracoes::class.java)
            startActivity(intent)
        }

        criarCanalDeNotificacao()
        verificarNotificacao()

        // Emitir alerta toda vez que onCreate for chamado
        emitirAlerta()

        // Verificar permissão para notificações e solicitar se necessário
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_NOTIFICATION_PERMISSION)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        atualizarContagens()
        // Verificar a notificação sem emitir alerta novamente
        verificarNotificacao()
    }

    private fun atualizarContagens() {
        contarBarrisPorStatus("Cheio") { cheiosCount ->
            txtBarrisCheios.text = "Barris Cheios: $cheiosCount"
        }

        contarBarrisPorStatus("Sujo") { sujosCount ->
            txtBarrisSujos.text = "Barris Sujos: $sujosCount"
            contagemSujos = sujosCount

            val sharedPreferences = getSharedPreferences("PREFS", MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putInt("contagem_sujos", contagemSujos)
                apply()
            }

            contarTotalBarris { totalCount ->
                if (totalCount > 0) {
                    val percentualSujos = (sujosCount.toDouble() / totalCount) * 100
                    if (percentualSujos >= 50) {
                        // Emitir alerta apenas no onCreate
                        // A verificação do alerta emitido é feita em emitirAlerta
                    }
                }
            }
        }

        contarBarrisPorStatus("Limpo") { limposCount ->
            txtBarrisLimpos.text = "Barris Limpos: $limposCount"
        }

        contarBarrisPorStatus("No Cliente") { clienteCount ->
            txtBarrisCliente.text = "Barris no Cliente: $clienteCount"
        }
    }

    private fun emitirAlerta() {
        contarBarrisPorStatus("Sujo") { sujosCount ->
            contarTotalBarris { totalCount ->
                if (totalCount > 0) {
                    val percentualSujos = (sujosCount.toDouble() / totalCount) * 100
                    if (percentualSujos >= 50) {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Alerta")
                        builder.setMessage("50% ou mais dos barris estão sujos!")
                        builder.setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }

                        val alertDialog = builder.create()
                        alertDialog.show()
                    }
                }
            }
        }
    }

    private fun contarBarrisPorStatus(status: String, onResult: (Int) -> Unit) {
        val email = FirebaseAuth.getInstance().currentUser?.email
        if (email != null) {
            firestore.collection("barris")
                .whereEqualTo("email", email)
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener { result ->
                    val count = result.size()
                    onResult(count)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Erro ao contar barris: ${exception.message}", Toast.LENGTH_SHORT).show()
                    onResult(0)
                }
        } else {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            onResult(0)
        }
    }

    private fun deslogarUsuario() {
        val auth = FirebaseAuth.getInstance()
        auth.signOut()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun contarTotalBarris(onResult: (Int) -> Unit) {
        val email = FirebaseAuth.getInstance().currentUser?.email
        if (email != null) {
            firestore.collection("barris")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { result ->
                    val count = result.size()
                    onResult(count)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Erro ao contar total de barris: ${exception.message}", Toast.LENGTH_SHORT).show()
                    onResult(0)
                }
        } else {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            onResult(0)
        }
    }

    private fun criarCanalDeNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "barris_notification_channel"
            val channelName = "Lembrete de Limpeza de Barris"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Notificações para lembrar o usuário de limpar barris sujos"
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun agendarNotificacaoDiaria() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Definindo o alarme para disparar a cada 12 horas
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 12 * 60 * 60 * 1000, // Inicia após 12 horas
            12 * 60 * 60 * 1000, // Repete a cada 12 horas
            pendingIntent
        )
    }

    private fun verificarNotificacao() {
        val sharedPreferences = getSharedPreferences("PREFS", MODE_PRIVATE)
        val contagemSujos = sharedPreferences.getInt("contagem_sujos", 0)

        if (contagemSujos > 0) {
            val intent = Intent(this, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            if (pendingIntent == null) {
                // Agendar a notificação se não estiver já agendada
                agendarNotificacaoDiaria()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão concedida
            } else {
                // Permissão negada
                Toast.makeText(this, "Permissão de notificações negada.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
