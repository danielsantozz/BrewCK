package com.example.brewck

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EditarPerfil : AppCompatActivity() {
    private lateinit var txtTituloEditar: TextView
    private lateinit var txtNovidade: TextView
    private lateinit var edtNovidade: EditText
    private lateinit var btnVoltar: Button
    private lateinit var btnAplicar: Button
    private lateinit var perfilController: EditarPerfilController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_perfil)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val titulo = intent.getStringExtra("Titulo")
        val subtitulo = intent.getStringExtra("Subtitulo")

        txtTituloEditar = findViewById(R.id.txtTituloEditar)
        txtNovidade = findViewById(R.id.txtNovidade)
        edtNovidade = findViewById(R.id.edtNovidade)
        btnVoltar = findViewById(R.id.btnVoltarEditarPerfil)
        btnAplicar = findViewById(R.id.btnAplicar)

        txtTituloEditar.text = "Editar $titulo"
        txtNovidade.text = subtitulo

        perfilController = EditarPerfilController(this)

        btnVoltar.setOnClickListener { finish() }
        btnAplicar.setOnClickListener {
            perfilController.atualizarPerfil(titulo.toString(), edtNovidade.text.toString()) { sucesso ->
                if (sucesso) {
                    val intent = Intent(this, MenuPrincipal::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}
