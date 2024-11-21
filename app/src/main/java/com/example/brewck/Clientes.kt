package com.example.brewck

import ClienteAdapter
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import android.widget.ProgressBar.VISIBLE
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.brewck.controllers.ClientesController
import com.example.brewck.models.Cliente

class Clientes : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnVoltar: Button
    private lateinit var btnCadastrarCliente: ImageView
    private lateinit var edtFiltrarNomeCliente: EditText
    private lateinit var spinnerFiltro: Spinner
    private lateinit var clienteAdapter: ClienteAdapter
    private lateinit var progressBar: ProgressBar
    private val clientesController = ClientesController()  // Controlador que gerencia as operações dos clientes.
    private var listaClientes = mutableListOf<Pair<String, Cliente>>()  // Lista de clientes com ID e objeto Cliente.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_clientes)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa o Spinner (campo de seleção) para filtrar os clientes por avaliação.
        spinnerFiltro = findViewById(R.id.spinnerFiltro)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.avaliacoes,
            R.layout.spinner_item_text_normal
        )
        adapter.setDropDownViewResource(R.layout.spinner_item_text)
        spinnerFiltro.adapter = adapter

        // Inicializa o botão de "Voltar", que encerra a Activity atual.
        btnVoltar = findViewById(R.id.btnVoltarCliente)
        btnVoltar.setOnClickListener { finish() }

        // Inicializa o botão de "Cadastrar Cliente", que inicia a Activity para adicionar um novo cliente.
        btnCadastrarCliente = findViewById(R.id.btnCadastrarCliente)
        btnCadastrarCliente.setOnClickListener {
            val intent = Intent(this, AdicionarCliente::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        }

        // Configura um filtro de texto para buscar clientes pelo nome enquanto o usuário digita.
        edtFiltrarNomeCliente = findViewById(R.id.edtFiltroNomeCliente)
        edtFiltrarNomeCliente.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::clienteAdapter.isInitialized) {
                    val avaliacaoSelecionada = spinnerFiltro.selectedItem?.toString()
                    aplicarFiltro(s.toString(), avaliacaoSelecionada)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Configura o Spinner para filtrar os clientes pela avaliação selecionada.
        spinnerFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (::clienteAdapter.isInitialized) {
                    aplicarFiltro(edtFiltrarNomeCliente.text.toString(), spinnerFiltro.selectedItem.toString())
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Configura o RecyclerView para listar os clientes de forma eficiente.
        recyclerView = findViewById(R.id.recyclerViewClientes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializa a ProgressBar para mostrar o carregamento dos dados.
        progressBar = findViewById(R.id.progressBar)

        // Carrega os clientes na tela ao iniciar a Activity.
        buscarClientes()
    }

    companion object {
        private const val REQUEST_CODE = 1  // Código de solicitação para a Activity de cadastro de cliente.
    }

    // Método que busca os clientes do servidor/BD.
    private fun buscarClientes() {
        progressBar.visibility = VISIBLE  // Exibe a barra de progresso enquanto os dados estão sendo carregados.

        // Chama o método do controlador para buscar os clientes, fornecendo uma resposta com sucesso, lista de clientes ou mensagem de erro.
        clientesController.buscarClientesPorEmail { sucesso, clientes, mensagem ->
            progressBar.visibility = ProgressBar.GONE  // Esconde a barra de progresso ao concluir a busca.

            if (sucesso && clientes != null) {
                listaClientes = clientes.toMutableList()

                // Inicializa o adapter para exibir os clientes no RecyclerView.
                clienteAdapter = ClienteAdapter(listaClientes.map { it.second }) { perfil ->
                    // Ao clicar em um cliente, abre os detalhes do cliente.
                    val intent = Intent(this, DetalhesCliente::class.java)
                    intent.putExtra("id", listaClientes.find { it.second == perfil }?.first)
                    intent.putExtra("nome", perfil.nome)
                    intent.putExtra("cpf", perfil.cpf)
                    intent.putExtra("barril", perfil.barril.toTypedArray())
                    intent.putExtra("endereco", perfil.endereco)
                    intent.putExtra("avaliacao", perfil.avaliacao)
                    startActivityForResult(intent, REQUEST_CODE)
                }
                recyclerView.adapter = clienteAdapter

                // Aplica o filtro com base no nome e avaliação do cliente.
                aplicarFiltro(edtFiltrarNomeCliente.text.toString(), spinnerFiltro.selectedItem?.toString())
            } else {
                // Exibe uma mensagem de erro caso a busca falhe.
                Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Método que aplica o filtro de nome e avaliação aos clientes.
    private fun aplicarFiltro(nomeFiltro: String, avaliacaoFiltro: String?) {
        if (::clienteAdapter.isInitialized) {
            val filteredClientes = clientesController.filtrarClientes(listaClientes, nomeFiltro, avaliacaoFiltro)
            clienteAdapter.updateClientes(filteredClientes)  // Atualiza o adapter com os clientes filtrados.
        }
    }

    // Método chamado quando a Activity que foi iniciada com 'startActivityForResult' retorna um resultado.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Atualiza a lista de clientes após a adição ou edição de um cliente.
            buscarClientes()
        }
    }

    // Método chamado quando a Activity retorna para a tela principal (ex.: após adicionar/editar cliente).
    override fun onResume() {
        super.onResume()
        buscarClientes()  // Recarrega os clientes ao retomar a Activity.
    }
}
