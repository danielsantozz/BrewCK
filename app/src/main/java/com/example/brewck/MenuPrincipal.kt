package com.example.brewck

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.brewck.controllers.MenuPrincipalController

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

    private lateinit var progressBar: ProgressBar
    private lateinit var blurBackground: View

    private lateinit var controller: MenuPrincipalController
    private var activeTasks = 0 // Contador de tarefas ativas

    // Funções para iniciar e completar tarefas com controle de carregamento
    private fun startTask() {
        if (activeTasks == 0) showLoading() // Exibe o carregamento se for a primeira tarefa
        activeTasks++
    }

    private fun completeTask() {
        activeTasks-- // Decrementa o número de tarefas ativas
        if (activeTasks <= 0) hideLoading() // Esconde o carregamento quando todas as tarefas são concluídas
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

        controller = MenuPrincipalController(this) // Inicializa o controlador da tela

        // Vinculação dos componentes de interface com os elementos do layout
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

        progressBar = findViewById(R.id.progressBar)
        blurBackground = findViewById(R.id.blurBackground)

        // Configuração de listeners para os botões de navegação
        btnBarris.setOnClickListener {
            startActivity(Intent(this, Barris::class.java)) // Navega para a tela de barris
        }
        btnClientes.setOnClickListener {
            startActivity(Intent(this, Clientes::class.java)) // Navega para a tela de clientes
        }
        btnQR.setOnClickListener {
            startActivity(Intent(this, QRCode::class.java)) // Navega para a tela de QR Code
        }
        btnSair.setOnClickListener {
            controller.deslogarUsuario() // Desloga o usuário
        }
        imgConfig.setOnClickListener {
            startActivity(Intent(this, Configuracoes::class.java)) // Navega para configurações
        }
        imgLiquido.setOnClickListener {
            startActivity(Intent(this, Liquidos::class.java)) // Navega para a tela de líquidos
        }

        // Criação e verificação de notificações
        controller.criarCanalDeNotificacao()
        controller.verificarNotificacao()

        // Verificação de permissões para notificações
        controller.verificarPermissaoNotificacao { isGranted ->
            if (isGranted) {
                controller.agendarNotificacaoDiaria() // Agenda a notificação diária se a permissão for concedida
            }
        }

        // Carrega os dados iniciais ao abrir a tela
        carregarDadosIniciais()
    }

    // Função para carregar dados iniciais como o nome do usuário e contagens dos barris
    private fun carregarDadosIniciais() {
        startTask() // Inicia uma nova tarefa de carregamento
        controller.recuperarNomeDoUsuario { nome ->
            nome?.let {
                val primeiroNome = it.split(" ").firstOrNull() ?: it
                val nomeFormatado = if (primeiroNome.length > 10) {
                    "${primeiroNome.take(10)}..." // Limita o nome do usuário a 10 caracteres
                } else {
                    primeiroNome
                }
                txtUsuario.text = "Olá, $nomeFormatado." // Exibe o nome formatado na tela
            } ?: run {
                Toast.makeText(this, "Nome não encontrado.", Toast.LENGTH_SHORT).show() // Exibe mensagem de erro se o nome não for encontrado
            }
            completeTask() // Conclui a tarefa de carregamento
        }

        // Atualiza as contagens dos barris (cheios, sujos, limpos e no cliente)
        startTask()
        controller.atualizarContagens { cheiosCount, sujosCount, limposCount, clienteCount ->
            txtBarrisCheios.text = "Barris cheios: $cheiosCount"
            txtBarrisSujos.text = "Barris sujos: $sujosCount"
            txtBarrisLimpos.text = "Barris limpos: $limposCount"
            txtBarrisCliente.text = "Barris no cliente: $clienteCount"

            // Emite um alerta caso necessário (ex: 50% dos barris sujos)
            controller.emitirAlerta { isAlertNeeded ->
                if (isAlertNeeded) mostrarAlerta() // Exibe alerta se necessário
            }
            completeTask() // Conclui a tarefa de carregamento
        }
    }

    // Atualiza os dados sempre que a tela volta ao primeiro plano
    override fun onResume() {
        super.onResume()
        startTask()
        controller.atualizarContagens { cheiosCount, sujosCount, limposCount, clienteCount ->
            txtBarrisCheios.text = "Barris cheios: $cheiosCount"
            txtBarrisSujos.text = "Barris sujos: $sujosCount"
            txtBarrisLimpos.text = "Barris limpos: $limposCount"
            txtBarrisCliente.text = "Barris no cliente: $clienteCount"
            completeTask()
        }
    }

    // Exibe um alerta quando 50% ou mais dos barris estão sujos
    private fun mostrarAlerta() {
        AlertDialog.Builder(this)
            .setTitle("Alerta")
            .setMessage("50% ou mais dos barris estão sujos!")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() } // Fechar alerta
            .show()
    }

    // Função para exibir o carregamento na tela
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        blurBackground.visibility = View.VISIBLE

        // Desativa a interação com os elementos enquanto o carregamento está ativo
        setAllViewsClickable(false)
    }

    // Função para esconder o carregamento
    private fun hideLoading() {
        progressBar.visibility = View.GONE
        blurBackground.visibility = View.GONE

        // Reativa a interação com os elementos
        setAllViewsClickable(true)
    }

    // Função para habilitar ou desabilitar a interatividade com todos os botões
    private fun setAllViewsClickable(isClickable: Boolean) {
        btnBarris.isClickable = isClickable
        btnClientes.isClickable = isClickable
        btnQR.isClickable = isClickable
        btnSair.isClickable = isClickable
        imgConfig.isClickable = isClickable
        imgLiquido.isClickable = isClickable
    }
}
