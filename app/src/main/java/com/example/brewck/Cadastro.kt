package com.example.brewck

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
import com.example.brewck.controllers.CadastroController

class Cadastro : AppCompatActivity() {

    private lateinit var btnEntrada: TextView
    private lateinit var edtCriaUsuario: EditText
    private lateinit var edtCriaSenha: EditText
    private lateinit var edtCriaEmail: EditText
    private lateinit var btnCadastrar: Button
    private val cadastroController = CadastroController()  // Controlador que gerencia o processo de cadastro.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cadastro)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa os componentes da interface do usuário (botões, campos de texto).
        btnEntrada = findViewById(R.id.btnEntrada)
        btnEntrada.setOnClickListener {
            // Quando o botão de entrada é clicado, navega para a tela principal (MainActivity).
            val intentMENU = Intent(this, MainActivity::class.java)
            startActivity(intentMENU)
        }

        // Inicializa os campos de texto e o botão de cadastro.
        edtCriaUsuario = findViewById(R.id.edtCriaUsuario)
        edtCriaSenha = findViewById(R.id.edtCriaSenha)
        edtCriaEmail = findViewById(R.id.edtCriaEmail)
        btnCadastrar = findViewById(R.id.btnCadastrar)

        // Define o comportamento do botão de cadastro.
        btnCadastrar.setOnClickListener { cadastrar() }
    }

    // Método que é chamado quando o usuário clica no botão de cadastro.
    private fun cadastrar() {
        // Obtém os dados inseridos pelo usuário nos campos de texto.
        val newUsuario = edtCriaUsuario.text.toString()
        val newSenha = edtCriaSenha.text.toString()
        val newEmail = edtCriaEmail.text.toString()

        // Chama o método 'criarUsuario' no controlador para tentar cadastrar o usuário.
        cadastroController.criarUsuario(newEmail, newSenha, newUsuario) { sucesso, mensagem ->
            if (sucesso) {
                // Caso o cadastro seja bem-sucedido, exibe uma mensagem de sucesso e navega para a tela principal.
                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                val intent1 = Intent(this, MainActivity::class.java)
                startActivity(intent1)
            } else {
                // Caso ocorra um erro, exibe uma mensagem com o erro retornado pelo controlador.
                Toast.makeText(this, "$mensagem", Toast.LENGTH_LONG).show()
            }
        }
    }
}
