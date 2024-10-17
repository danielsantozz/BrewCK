package com.example.brewck

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.brewck.controllers.ConfiguracoesController

class Configuracoes : AppCompatActivity() {

    private lateinit var btnVoltar: Button
    private lateinit var btnAlterarNome: Button
    private lateinit var btnAlterarSenha: Button
    private lateinit var btnExcluirConta: Button

    private val configuracoesController = ConfiguracoesController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_configuracoes)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnVoltar = findViewById(R.id.btnVoltarCfg)
        btnAlterarNome = findViewById(R.id.btnAlterarNome)
        btnAlterarSenha = findViewById(R.id.btnAlterarSenha)
        btnExcluirConta = findViewById(R.id.btnExcluirConta)

        btnVoltar.setOnClickListener { finish() }

        btnAlterarNome.setOnClickListener {
            val intent = Intent(this, EditarPerfil::class.java)
            intent.putExtra("Titulo", "Nome")
            intent.putExtra("Subtitulo", "Digite seu novo nome")
            startActivity(intent)
        }

        btnAlterarSenha.setOnClickListener {
            val intent = Intent(this, EditarPerfil::class.java)
            intent.putExtra("Titulo", "Senha")
            intent.putExtra("Subtitulo", "Digite sua nova senha")
            startActivity(intent)
        }

        btnExcluirConta.setOnClickListener {
            excluirConta()
        }
    }

    private fun excluirConta() {
        configuracoesController.deletarConta { sucesso, mensagem ->
            if (sucesso) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
            }
        }
    }
}
