package com.example.brewck

import BarrilAdapter
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.brewck.controllers.BarrilController

class Barris : AppCompatActivity() {

    private lateinit var btnVoltar: Button
    private lateinit var btnCadastrarBarril: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var barrilAdapter: BarrilAdapter
    private lateinit var edtFiltroNome: EditText
    private lateinit var progressBar: ProgressBar
    private var filtroStatus: String = "Todos"  // Variável para armazenar o filtro de status
    private val controller = BarrilController()  // Controlador responsável pela lógica dos barris.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_barris)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialização dos componentes da interface (botões, campos de texto, RecyclerView, etc.).
        edtFiltroNome = findViewById(R.id.edtFiltroNome)
        progressBar = findViewById(R.id.progressBar)

        // Configuração do botão de cadastro de barril, que abre a Activity 'AdicionarBarril'.
        btnCadastrarBarril = findViewById(R.id.btnCadastrarBarril)
        btnCadastrarBarril.setOnClickListener {
            val intent = Intent(this, AdicionarBarril::class.java)
            startActivity(intent)
        }

        // Configuração do botão de voltar, que finaliza a Activity atual.
        btnVoltar = findViewById(R.id.btnVoltarBarril)
        btnVoltar.setOnClickListener { finish() }

        // Configuração do RecyclerView, que exibe a lista de barris.
        recyclerView = findViewById(R.id.recyclerViewBarris)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Adiciona um TextWatcher para filtrar barris conforme o usuário digita no campo de filtro de nome.
        edtFiltroNome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                aplicarFiltro(s.toString())  // Aplica o filtro de nome e status.
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Configuração do Spinner para filtrar barris por status.
        val opcoesFiltro = arrayOf("Todos", "Cheio", "No Cliente", "Sujo", "Limpo", "Favoritos")
        val spinner: Spinner = findViewById(R.id.spinnerFiltro)
        val adapter = ArrayAdapter(this, R.layout.spinner_item_text_normal, opcoesFiltro)
        adapter.setDropDownViewResource(R.layout.spinner_item_text)
        spinner.adapter = adapter

        // Configura o listener para alterar o filtro de status quando o item do Spinner for selecionado.
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filtroStatus = opcoesFiltro[position]  // Atualiza o filtro de status.
                aplicarFiltro(edtFiltroNome.text.toString())  // Aplica o filtro com base no nome e status.
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Carrega os barris ao iniciar a Activity.
        carregarBarris()
    }

    // Método para carregar os barris da fonte de dados e exibi-los no RecyclerView.
    private fun carregarBarris() {
        progressBar.visibility = ProgressBar.VISIBLE  // Exibe a barra de progresso enquanto os dados são carregados.

        // Chama o controlador para buscar os barris associados ao email do usuário.
        controller.buscarBarrilPorEmail { barris ->
            progressBar.visibility = ProgressBar.GONE  // Oculta a barra de progresso após o carregamento.

            // Verifica se a lista de barris está vazia e exibe uma mensagem.
            if (barris.isEmpty()) {
                Toast.makeText(this, "Nenhum barril encontrado.", Toast.LENGTH_SHORT).show()
                return@buscarBarrilPorEmail
            }

            // Cria o adaptador do RecyclerView com a lista de barris e configura o comportamento do clique no item.
            barrilAdapter = BarrilAdapter(barris) { barril ->
                // Cria a Intent para abrir a Activity de detalhes do barril.
                val intent = Intent(this, DetalhesBarril::class.java)
                intent.putExtra("id", barril.id)
                intent.putExtra("nome", barril.nome)
                intent.putExtra("capacidade", barril.capacidade)
                intent.putExtra("propriedade", barril.propriedade)
                intent.putExtra("status", barril.status)
                intent.putExtra("liquido", barril.liquido)
                intent.putExtra("favorito", barril.isFavorite)
                intent.putExtra("cliente", barril.cliente)
                startActivity(intent)  // Inicia a Activity de detalhes do barril.
            }
            recyclerView.adapter = barrilAdapter  // Define o adaptador no RecyclerView.
        }
    }

    // Método para aplicar o filtro de nome e status nos barris.
    private fun aplicarFiltro(nomeFiltro: String) {
        if (::barrilAdapter.isInitialized) {
            controller.filtrarBarris(nomeFiltro, filtroStatus) { barrisFiltrados ->
                barrilAdapter.updateBarris(barrisFiltrados)  // Atualiza a lista de barris no adaptador.
            }
        }
    }

    // Método chamado quando a Activity é retomada (ex: após adicionar ou editar um barril).
    override fun onResume() {
        super.onResume()
        carregarBarris()  // Recarrega os barris quando a Activity retorna à tela.
    }
}
