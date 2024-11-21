package com.example.brewck

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.brewck.controllers.LoginController

class MainActivity : AppCompatActivity() {

    private lateinit var edtUsuario: EditText
    private lateinit var edtSenha: EditText
    private lateinit var btnEntrar: Button
    private lateinit var btnCadastro: TextView
    private lateinit var btnRecuperar: TextView
    private lateinit var progressBar: ProgressBar
    private val loginController = LoginController()  // Instância do controlador de login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Verifica a disponibilidade de rede
        if (!isNetworkAvailable()) {
            // Exibe um diálogo de erro caso a conexão com a internet não esteja disponível
            showNoInternetDialog()
        } else {
            // Caso o usuário já esteja logado, redireciona para o MenuPrincipal
            if (loginController.isUserLoggedIn()) {
                startActivity(Intent(this, MenuPrincipal::class.java))
                finish()  // Finaliza a activity atual
                return
            }

            // Inicializa os componentes da interface
            edtUsuario = findViewById(R.id.edtUsuario)
            edtSenha = findViewById(R.id.edtSenha)
            btnEntrar = findViewById(R.id.btnEntrar)
            btnCadastro = findViewById(R.id.btnCadastro)
            btnRecuperar = findViewById(R.id.btnRecuperar)
            progressBar = findViewById(R.id.progressBar)

            // Configura o ouvinte de clique para o botão "Entrar"
            btnEntrar.setOnClickListener { login() }
            // Configura o ouvinte de clique para o botão "Cadastro"
            btnCadastro.setOnClickListener {
                startActivity(Intent(this, Cadastro::class.java))  // Abre a activity de cadastro
            }
            // Configura o ouvinte de clique para o botão "Recuperar"
            btnRecuperar.setOnClickListener {
                startActivity(Intent(this, RecuperarConta::class.java))  // Abre a activity de recuperação de conta
            }
        }
    }

    // Método responsável por fazer o login
    private fun login() {
        showLoading(true)  // Exibe o progress bar enquanto o login está sendo feito

        // Obtém os valores inseridos no campo de usuário e senha
        val email = edtUsuario.text.toString()
        val senha = edtSenha.text.toString()

        // Chama o controlador de login para realizar o processo de autenticação
        loginController.fazerLogin(email, senha) { sucesso, mensagem ->
            showLoading(false)  // Oculta o progress bar após a tentativa de login
            if (sucesso) {
                // Exibe uma mensagem de sucesso e redireciona para o MenuPrincipal
                Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MenuPrincipal::class.java))
                finish()  // Finaliza a activity de login
            } else {
                // Exibe a mensagem de erro caso o login falhe
                Toast.makeText(this, "$mensagem", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Método para exibir ou ocultar o progress bar durante o processo de login
    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE  // Exibe ou oculta o progress bar
    }

    // Método para verificar se há conexão com a internet
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo  // Obtém as informações sobre a rede ativa
        return activeNetwork?.isConnected == true  // Retorna true se a rede está conectada
    }

    // Método para exibir um AlertDialog informando que não há conexão com a internet
    private fun showNoInternetDialog() {
        val builder = AlertDialog.Builder(this)
            .setTitle("Erro de Conexão")  // Título do diálogo
            .setMessage("Sem conexão com a internet.")  // Mensagem do diálogo
            .setCancelable(false)  // Impede que o usuário possa fechar o diálogo sem uma ação
            .setPositiveButton("OK") { _, _ ->
                // Fecha o aplicativo quando o usuário clicar em "OK"
                finishAffinity()  // Finaliza todas as atividades da pilha
            }

        val dialog = builder.create()
        dialog.show()  // Exibe o diálogo
    }
}
