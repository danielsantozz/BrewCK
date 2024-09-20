package com.example.brewck

import Cliente
import ClienteAdapter
import FirebaseRepository
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class Clientes : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnVoltar: Button
    private lateinit var btnCadastrarCliente: ImageView
    private lateinit var edtFiltrarNomeCliente: EditText
    private lateinit var spinnerFiltro: Spinner

    private lateinit var clienteAdapter: ClienteAdapter
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

        // Configurar spinner
        spinnerFiltro = findViewById(R.id.spinnerFiltro)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.avaliacoes,
            R.layout.spinner_item_text_normal
        )
        adapter.setDropDownViewResource(R.layout.spinner_item_text)
        spinnerFiltro.adapter = adapter

        // Outros componentes
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

        spinnerFiltro.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (::clienteAdapter.isInitialized) {
                    aplicarFiltro(edtFiltrarNomeCliente.text.toString(), spinnerFiltro.selectedItem.toString())
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        })

        recyclerView = findViewById(R.id.recyclerViewClientes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        buscarClientePorEmail()
    }

    companion object {
        private const val REQUEST_CODE = 1
    }

    private fun buscarClientePorEmail() {
        FirebaseRepository().pegarEmail { email ->
            Log.d("Clientes", "Buscando cliente por email: $email")
            if (email != null) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("clientes")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        Log.d("Clientes", "Clientes encontrados: ${querySnapshot.documents.size}")
                        listaClientes.clear()
                        for (document in querySnapshot.documents) {
                            val id = document.id
                            val nome = document.getString("nome") ?: ""
                            val cpf = document.getString("cpf") ?: ""
                            val barril = document.getString("barril") ?: ""
                            val endereco = document.getString("endereco") ?: ""
                            val avaliacao = document.getString("avaliacao") ?: ""
                            listaClientes.add(id to Cliente(id, nome, cpf, barril, endereco, avaliacao))
                        }

                        listaClientes.sortBy { it.second.nome }

                        // Log a lista de clientes
                        Log.d("Clientes", "Lista de clientes: $listaClientes")

                        // Inicialize o clienteAdapter aqui
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

                        // Aplique o filtro inicial
                        aplicarFiltro(edtFiltrarNomeCliente.text.toString(), spinnerFiltro.selectedItem?.toString())
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Clientes", "Erro ao carregar clientes: ${exception.message}")
                        Toast.makeText(this, "Erro ao carregar clientes: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Log.w("Clientes", "Usuário não autenticado.")
                Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun aplicarFiltro(nomeFiltro: String, avaliacaoFiltro: String?) {
        Log.d("Clientes", "Filtrando com nome: $nomeFiltro e avaliação: $avaliacaoFiltro")

        val filteredClientes = listaClientes.filter {
            val matchesNome = it.second.nome.contains(nomeFiltro, ignoreCase = true)
            val matchesAvaliacao = when (avaliacaoFiltro) {
                null, "", "Todos" -> true // Aceita qualquer avaliação se "Todos" ou vazio
                else -> it.second.avaliacao == avaliacaoFiltro
            }
            matchesNome && matchesAvaliacao
        }.map { it.second }

        clienteAdapter.updateClientes(filteredClientes)

        Log.d("Clientes", "Total de clientes após filtro: ${filteredClientes.size}")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            buscarClientePorEmail()
        }
    }

    override fun onResume() {
        super.onResume()
        buscarClientePorEmail()
    }
}
