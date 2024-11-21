package com.example.brewck

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.brewck.controllers.DetalhesClienteController

class DetalhesCliente : AppCompatActivity() {
    private lateinit var txtNomeCliente: TextView
    private lateinit var txtCPFCliente: TextView
    private lateinit var txtBarrilCliente: TextView
    private lateinit var txtEnderecoCliente: TextView
    private lateinit var imgCliente: ImageView
    private lateinit var btnEditar: Button
    private lateinit var btnVoltar: Button
    private lateinit var btnAvaliarBom: ImageButton
    private lateinit var btnAvaliarRuim: ImageButton

    // Controlador para gerenciar a lógica do cliente
    private lateinit var controller: DetalhesClienteController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalhes_cliente)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa o controlador
        controller = DetalhesClienteController(this)

        // Obtém os dados passados pela Intent
        val id = intent.getStringExtra("id")
        val nome = intent.getStringExtra("nome")
        val cpf = intent.getStringExtra("cpf")
        val barrilArray = intent.getStringArrayExtra("barril") ?: arrayOf()  // Pode ser um array vazio
        val endereco = intent.getStringExtra("endereco")
        val avaliacao = intent.getStringExtra("avaliacao")

        // Inicializa os componentes da interface
        txtNomeCliente = findViewById(R.id.txtDetNomeCliente)
        txtCPFCliente = findViewById(R.id.txtDetCPF)
        txtBarrilCliente = findViewById(R.id.txtDetBarril)
        txtEnderecoCliente = findViewById(R.id.txtDetEndereco)
        btnEditar = findViewById(R.id.btnIntentEditar)
        imgCliente = findViewById(R.id.imgCliente)
        btnAvaliarBom = findViewById(R.id.btnAvaliarBom)
        btnAvaliarRuim = findViewById(R.id.btnAvaliarRuim)
        btnVoltar = findViewById(R.id.btnVoltar)

        // Listener para o botão de voltar (fecha a activity atual)
        btnVoltar.setOnClickListener {
            finish()
        }

        // Exibe os dados do cliente na interface
        txtNomeCliente.text = nome
        txtCPFCliente.text = cpf

        // Verifica se o array de barris não está vazio e processa a lista de barris
        if (barrilArray.isNotEmpty()) {
            // Concatena os barris até o limite de 100 caracteres
            val barrisConcatenados = barrilArray.joinToString(", ")

            // Se o texto exceder 30 caracteres, corta e adiciona reticências
            val textoExibido = if (barrisConcatenados.length > 30) {
                "${barrisConcatenados.substring(0, 30)}..."
            } else {
                barrisConcatenados
            }

            // Exibe a lista de barris no TextView
            txtBarrilCliente.text = textoExibido

            // Listener para exibir o diálogo de barris se houver mais de um barril
            txtBarrilCliente.setOnClickListener {
                if (barrilArray.size > 1) {
                    mostrarDialogBarris(barrilArray)  // Mostra o diálogo com a lista completa de barris
                } else {
                    Log.d("Somente um barril", "Somente um barril: ${barrilArray[0]}")
                }
            }
        } else {
            txtBarrilCliente.text = "Nenhum barril atribuído"  // Exibe caso o array de barris esteja vazio
        }

        // Exibe o endereço do cliente
        txtEnderecoCliente.text = endereco

        // Atualiza a imagem de avaliação do cliente com base no valor passado
        when (avaliacao) {
            "Bom" -> imgCliente.setImageResource(R.drawable.usergreen)  // Imagem para avaliação boa
            "Ruim" -> imgCliente.setImageResource(R.drawable.userred)  // Imagem para avaliação ruim
            else -> imgCliente.setImageResource(R.drawable.user)  // Imagem padrão
        }

        // Configura o listener do botão de editar para navegar até a tela de edição
        btnEditar.setOnClickListener {
            val intent = Intent(this, EditarCliente::class.java)
            intent.putExtra("id", id)
            intent.putExtra("nome", nome)
            intent.putExtra("cpf", cpf)
            intent.putExtra("endereco", endereco)
            intent.putExtra("avaliacao", avaliacao)
            startActivity(intent)
        }

        // Listener para o botão de avaliação "Bom"
        btnAvaliarBom.setOnClickListener {
            if (id != null) {
                controller.avaliarCliente("Bom", id, imgCliente)  // Avalia o cliente como bom
            }
        }

        // Listener para o botão de avaliação "Ruim"
        btnAvaliarRuim.setOnClickListener {
            if (id != null) {
                controller.avaliarCliente("Ruim", id, imgCliente)  // Avalia o cliente como ruim
            }
        }

        // Listener para resetar a avaliação do cliente ao clicar na imagem
        imgCliente.setOnClickListener {
            if (id != null) {
                controller.resetarAvaliacao(id, imgCliente)  // Reseta a avaliação do cliente
            }
        }
    }

    // Método que exibe um diálogo com a lista de barris atribuídos ao cliente
    private fun mostrarDialogBarris(barrilArray: Array<String>) {
        // Concatena todos os barris em uma string, separada por linha
        val listaBarris = barrilArray.joinToString("\n")

        // Constrói o diálogo com a lista de barris
        AlertDialog.Builder(this)
            .setTitle("Barris atribuídos")  // Título do diálogo
            .setMessage(listaBarris)  // Mensagem com os barris
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }  // Botão de fechar
            .create()
            .show()  // Exibe o diálogo
    }

}
