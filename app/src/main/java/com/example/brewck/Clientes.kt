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
    private val clientesController = ClientesController()
    private var listaClientes = mutableListOf<Pair<String, Cliente>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_clientes)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        spinnerFiltro = findViewById(R.id.spinnerFiltro)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.avaliacoes,
            R.layout.spinner_item_text_normal
        )
        adapter.setDropDownViewResource(R.layout.spinner_item_text)
        spinnerFiltro.adapter = adapter

        btnVoltar = findViewById(R.id.btnVoltarCliente)
        btnVoltar.setOnClickListener { finish() }

        btnCadastrarCliente = findViewById(R.id.btnCadastrarCliente)
        btnCadastrarCliente.setOnClickListener {
            val intent = Intent(this, AdicionarCliente::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        }

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

        spinnerFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (::clienteAdapter.isInitialized) {
                    aplicarFiltro(edtFiltrarNomeCliente.text.toString(), spinnerFiltro.selectedItem.toString())
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        recyclerView = findViewById(R.id.recyclerViewClientes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        progressBar = findViewById(R.id.progressBar)

        buscarClientes()
    }

    companion object {
        private const val REQUEST_CODE = 1
    }

    private fun buscarClientes() {
        progressBar.visibility = VISIBLE

        clientesController.buscarClientesPorEmail { sucesso, clientes, mensagem ->
            progressBar.visibility = ProgressBar.GONE

            if (sucesso && clientes != null) {
                listaClientes = clientes.toMutableList()

                clienteAdapter = ClienteAdapter(listaClientes.map { it.second }) { perfil ->
                    val intent = Intent(this, DetalhesCliente::class.java)
                    intent.putExtra("id", listaClientes.find { it.second == perfil }?.first)
                    intent.putExtra("nome", perfil.nome)
                    intent.putExtra("cpf", perfil.cpf)
                    intent.putExtra("barril", perfil.barril)
                    intent.putExtra("endereco", perfil.endereco)
                    intent.putExtra("avaliacao", perfil.avaliacao)
                    startActivityForResult(intent, REQUEST_CODE)
                }
                recyclerView.adapter = clienteAdapter

                aplicarFiltro(edtFiltrarNomeCliente.text.toString(), spinnerFiltro.selectedItem?.toString())
            } else {
                Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun aplicarFiltro(nomeFiltro: String, avaliacaoFiltro: String?) {
        if (::clienteAdapter.isInitialized) {
            val filteredClientes = clientesController.filtrarClientes(listaClientes, nomeFiltro, avaliacaoFiltro)
            clienteAdapter.updateClientes(filteredClientes)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            buscarClientes()
        }
    }

    override fun onResume() {
        super.onResume()
        buscarClientes()
    }
}
