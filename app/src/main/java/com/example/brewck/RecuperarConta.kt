package com.example.brewck

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.brewck.controllers.RecuperarContaController

class RecuperarConta : AppCompatActivity() {
    private lateinit var btnVoltar: Button
    private lateinit var edtEmail: EditText
    private lateinit var btnRecuperar: TextView
    private lateinit var controller: RecuperarContaController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recuperar_conta)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        edtEmail = findViewById(R.id.edtEmailRecuperacao)
        btnVoltar = findViewById(R.id.btnVoltarRecuperacao)
        btnRecuperar = findViewById(R.id.btnRecuperarSenha)

        controller = RecuperarContaController(this)

        btnVoltar.setOnClickListener { controller.voltarParaMain() }
        btnRecuperar.setOnClickListener { controller.recuperarConta(edtEmail) }
    }
}
