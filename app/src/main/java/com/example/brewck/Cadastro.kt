package com.example.brewck

import FirebaseRepository
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class Cadastro : AppCompatActivity() {
    private lateinit var btnEntrada : TextView
    private lateinit var edtCriaUsuario : EditText
    private lateinit var edtCriaSenha : EditText
    private lateinit var edtCriaEmail : EditText
    private lateinit var btnCadastrar : Button

    private val repository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cadastro)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        btnEntrada = findViewById(R.id.btnEntrada)
        btnEntrada.setOnClickListener {
            val intentMENU = Intent(this, MainActivity::class.java)
            startActivity(intentMENU)
        }

        edtCriaUsuario = findViewById(R.id.edtCriaUsuario)
        edtCriaSenha = findViewById(R.id.edtCriaSenha)
        edtCriaEmail = findViewById(R.id.edtCriaEmail)
        btnCadastrar = findViewById(R.id.btnCadastrar)
        btnCadastrar.setOnClickListener { cadastrar() }


    }

    fun cadastrar() {
        val newUsuario = edtCriaUsuario.text.toString()
        val newSenha = edtCriaSenha.text.toString()
        val newEmail = edtCriaEmail.text.toString()


        repository.criarUsuario(newEmail, newSenha, newUsuario) { sucesso, mensagem ->
            if (sucesso) {
                println("Usuário criado com sucesso!")
                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                val intent1 = Intent(this, MainActivity::class.java)
                startActivity(intent1)
            } else {
                println("Falha ao criar usuário: $mensagem")
                Toast.makeText(this, "$mensagem", Toast.LENGTH_LONG).show()
            }
        }

    }
}