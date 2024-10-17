package com.example.brewck

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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

    private lateinit var imgConfig: ImageView
    private lateinit var imgLiquido: ImageView

    private lateinit var controller: MenuPrincipalController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_principal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        controller = MenuPrincipalController(this)

        txtUsuario = findViewById(R.id.txtUsuario)
        txtBarrisCheios = findViewById(R.id.txtBarrisCheios)
        txtBarrisSujos = findViewById(R.id.txtBarrisSujos)
        txtBarrisCliente = findViewById(R.id.txtBarrisCliente)
        txtBarrisLimpos = findViewById(R.id.txtBarrisLimpos)

        imgConfig = findViewById(R.id.imgConfig)
        imgLiquido = findViewById(R.id.imgLiquido)

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
            controller.deslogarUsuario()
        }
        imgConfig.setOnClickListener {
            val intent = Intent(this, Configuracoes::class.java)
            startActivity(intent)
        }
        imgLiquido.setOnClickListener {
            val intent = Intent(this, Liquidos::class.java)
            startActivity(intent)
        }

        controller.recuperarNomeDoUsuario { nome ->
            nome?.let {
                txtUsuario.text = "Olá, $it."
            } ?: run {
                Toast.makeText(this, "Nome não encontrado.", Toast.LENGTH_SHORT).show()
            }
        }

        controller.criarCanalDeNotificacao()
        controller.verificarNotificacao()

        controller.verificarPermissaoNotificacao { isGranted ->
            if (isGranted) {
                controller.agendarNotificacaoDiaria()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        controller.atualizarContagens { cheiosCount, sujosCount, limposCount, clienteCount ->
            txtBarrisCheios.text = "Barris Cheios: $cheiosCount"
            txtBarrisSujos.text = "Barris Sujos: $sujosCount"
            txtBarrisLimpos.text = "Barris Limpos: $limposCount"
            txtBarrisCliente.text = "Barris no Cliente: $clienteCount"

            controller.emitirAlerta { isAlertNeeded ->
                if (isAlertNeeded) {
                    mostrarAlerta()
                }
            }
        }
    }

    private fun mostrarAlerta() {
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
