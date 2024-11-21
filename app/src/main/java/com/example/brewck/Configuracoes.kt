package com.example.brewck

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
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

    private lateinit var txtNome: TextView
    private lateinit var txtEmail: TextView

    // Inicializa o controlador para manipular as ações de configurações do usuário.
    private val controller = ConfiguracoesController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_configuracoes)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa os botões e campos de texto da interface.
        btnVoltar = findViewById(R.id.btnVoltarCfg)
        btnAlterarNome = findViewById(R.id.btnAlterarNome)
        btnAlterarSenha = findViewById(R.id.btnAlterarSenha)
        btnExcluirConta = findViewById(R.id.btnExcluirConta)
        txtNome = findViewById(R.id.txtNomeUser)
        txtEmail = findViewById(R.id.txtEmailUser)

        // Configura a ação do botão "Voltar", que encerra a Activity atual.
        btnVoltar.setOnClickListener { finish() }

        // Configura a ação do botão "Alterar Nome", que abre a Activity 'EditarPerfil' para alterar o nome do usuário.
        btnAlterarNome.setOnClickListener {
            val intent = Intent(this, EditarPerfil::class.java)
            intent.putExtra("Titulo", "Nome")
            intent.putExtra("Subtitulo", "Digite seu novo nome")
            startActivity(intent)
        }

        // Configura a ação do botão "Alterar Senha", que abre a Activity 'EditarPerfil' para alterar a senha do usuário.
        btnAlterarSenha.setOnClickListener {
            val intent = Intent(this, EditarPerfil::class.java)
            intent.putExtra("Titulo", "Senha")
            intent.putExtra("Subtitulo", "Digite sua nova senha")
            startActivity(intent)
        }

        // Configura a ação do botão "Excluir Conta", que chama a função de exclusão de conta.
        btnExcluirConta.setOnClickListener {
            excluirConta()
        }

        // Chama o método para preencher os dados do usuário (nome e email) ao iniciar a Activity.
        preencherDados()
    }

    // Função que deleta a conta do usuário.
    private fun excluirConta() {
        // Chama o controlador para excluir a conta e fornece um callback para a resposta.
        controller.deletarConta { sucesso, mensagem ->
            if (sucesso) {
                // Se a exclusão for bem-sucedida, navega para a Activity 'MainActivity' (geralmente a tela inicial).
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()  // Finaliza a Activity atual (Configuracoes).
            } else {
                // Caso haja erro, exibe a mensagem retornada.
                Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Função que preenche os dados do usuário nos campos de texto (nome e email).
    private fun preencherDados() {
        // Chama o controlador para obter o nome do usuário e atualiza o campo de texto.
        val nome = controller.getNome { nome ->
            txtNome.text = nome
        }
        // Chama o controlador para obter o email do usuário e atualiza o campo de texto.
        val email = controller.getEmail { email ->
            txtEmail.text = email
        }
    }
}
