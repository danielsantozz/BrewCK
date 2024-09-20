package com.example.brewck

import FirebaseRepository
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var edtUsuario: EditText
    private lateinit var edtSenha: EditText
    private lateinit var btnEntrar: Button
    private lateinit var btnCadastro: TextView
    private lateinit var btnRecuperar: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            startActivity(Intent(this, MenuPrincipal::class.java))
            finish()
            return
        }

        edtUsuario = findViewById(R.id.edtUsuario)
        edtSenha = findViewById(R.id.edtSenha)
        btnEntrar = findViewById(R.id.btnEntrar)
        btnCadastro = findViewById(R.id.btnCadastro)
        btnRecuperar = findViewById(R.id.btnRecuperar)
        progressBar = findViewById(R.id.progressBar)

        btnEntrar.setOnClickListener { login() }
        btnCadastro.setOnClickListener {
            startActivity(Intent(this, Cadastro::class.java))
        }
        btnRecuperar.setOnClickListener {
            startActivity(Intent(this, RecuperarConta::class.java))
        }
    }

    private fun login() {
        showLoading(true)
        val repository = FirebaseRepository()

        val email = edtUsuario.text.toString()
        val senha = edtSenha.text.toString()

        repository.fazerLogin(email, senha) { sucesso, mensagem ->
            showLoading(false)
            if (sucesso) {
                Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MenuPrincipal::class.java))
                finish()
            } else {
                Toast.makeText(this, "$mensagem", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
