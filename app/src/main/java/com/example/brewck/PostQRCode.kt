package com.example.brewck

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.brewck.controllers.PostQRCodeController

class PostQRCode : AppCompatActivity() {

    lateinit var txtNome: TextView
    lateinit var txtCapacidade: TextView
    lateinit var txtStatus: TextView
    lateinit var imgBarril: ImageView
    lateinit var btnVoltar: Button
    private lateinit var btnEncher: Button
    private lateinit var btnVender: Button
    private lateinit var btnPegar: Button
    private lateinit var btnLimpar: Button

    // Declaração do controlador
    private lateinit var controller: PostQRCodeController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_post_qrcode)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa as variáveis de interface com os elementos do layout
        txtNome = findViewById(R.id.txtQRNomeBarril)
        txtCapacidade = findViewById(R.id.txtCapacidade)
        txtStatus = findViewById(R.id.txtStatus)
        imgBarril = findViewById(R.id.imgBarril)
        btnVoltar = findViewById(R.id.btnVoltar)
        btnEncher = findViewById(R.id.btnEncher)
        btnVender = findViewById(R.id.btnVender)
        btnPegar = findViewById(R.id.btnPegar)
        btnLimpar = findViewById(R.id.btnLimpar)

        // Inicializa o controlador para gerenciar as operações
        controller = PostQRCodeController(this)

        // Configura o comportamento do botão Voltar para finalizar a activity
        btnVoltar.setOnClickListener {
            finish()
        }

        // Recupera os extras passados pela Intent
        val extras = intent.extras
        val id = extras?.getString("id") // ID do barril
        val nome = extras?.getString("nome") // Nome do barril
        val status = extras?.getString("status") // Status do barril

        // Configura o comportamento do botão Encher
        btnEncher.setOnClickListener {
            if (id != null) {
                // Chama o método para buscar os líquidos disponíveis
                controller.buscarLiquidos { liquidos ->
                    if (liquidos.isNotEmpty()) {
                        if (nome != null && status != null) {
                            // Se houver líquidos disponíveis, exibe o diálogo para selecionar um
                            mostrarDialogLiquidos(id, nome, liquidos, status)
                        }
                    } else {
                        // Caso não haja líquidos disponíveis, exibe um toast
                        Toast.makeText(this, "Nenhum líquido encontrado.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Configura o comportamento do botão Vender
        btnVender.setOnClickListener {
            if (id != null) {
                // Busca os clientes cadastrados para vender o barril
                controller.buscarClientes { clientes ->
                    if (clientes.isNotEmpty()) {
                        controller.buscarBarrilPorId(id) { barril ->
                            barril?.let {
                                // Exibe o diálogo para selecionar um cliente
                                mostrarDialogClientes(it.id, it.nome, clientes)
                            }
                        }
                    } else {
                        // Caso não haja clientes disponíveis, exibe um toast
                        Toast.makeText(this, "Nenhum cliente encontrado.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Configura o comportamento do botão Pegar para marcar o barril como "Sujo"
        btnPegar.setOnClickListener {
            if (id != null && nome != null && status != null) {
                // Atualiza o status do barril para "Sujo"
                controller.atualizarStatusBarril(id, nome, status, "Sujo") { success ->
                    if (success) {
                        // Se a atualização for bem-sucedida, atualiza a interface
                        controller.buscarBarrilPorId(id) { controller.atualizarUI(it) }
                    }
                }
            }
        }

        // Configura o comportamento do botão Limpar para marcar o barril como "Limpo"
        btnLimpar.setOnClickListener {
            if (id != null && nome != null && status != null) {
                // Atualiza o status do barril para "Limpo"
                controller.atualizarStatusBarril(id, nome, status, "Limpo") { success ->
                    if (success) {
                        // Se a atualização for bem-sucedida, atualiza a interface
                        controller.buscarBarrilPorId(id) { controller.atualizarUI(it) }
                    }
                }
            }
        }

        // Verifica se o ID foi passado e busca as informações do barril
        id?.let {
            controller.buscarBarrilPorId(it) { barril ->
                controller.atualizarUI(barril) // Atualiza a interface com as informações do barril
            }
        } ?: run {
            // Caso o ID não tenha sido fornecido, exibe um toast de erro
            Toast.makeText(this, "ID do barril não fornecido", Toast.LENGTH_SHORT).show()
        }
    }

    // Método para exibir o diálogo de seleção de líquido para encher o barril
    private fun mostrarDialogLiquidos(barrilId: String, nomeBarril: String, liquidos: List<String>, status: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_encher_barril, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerLiquidos)

        // Configura o Spinner para exibir os líquidos disponíveis
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, liquidos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Cria e exibe o diálogo
        AlertDialog.Builder(this)
            .setTitle("Escolha um líquido")
            .setView(dialogView)
            .setPositiveButton("Encher") { _, _ ->
                // Atualiza o líquido do barril quando o botão "Encher" for clicado
                val liquidoSelecionado = spinner.selectedItem.toString()
                controller.atualizarLiquidoDoBarril(barrilId, nomeBarril, liquidoSelecionado, status)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Método para exibir o diálogo de seleção de cliente para vender o barril
    private fun mostrarDialogClientes(barrilId: String, nomeBarril: String, clientes: List<String>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_encher_barril, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerLiquidos)

        // Configura o Spinner para exibir os clientes disponíveis
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, clientes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Cria e exibe o diálogo
        AlertDialog.Builder(this)
            .setTitle("Escolha um cliente")
            .setView(dialogView)
            .setPositiveButton("Vender") { _, _ ->
                // Atualiza o cliente do barril quando o botão "Vender" for clicado
                val clienteSelecionado = spinner.selectedItem.toString()

                controller.atualizarClienteNoBarril(barrilId, clienteSelecionado)
                controller.atualizarBarrilNoCliente(clienteSelecionado, nomeBarril)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
