package com.example.brewck

import Barril
import BarrilAdapter
import FirebaseRepository
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

class Barris : AppCompatActivity() {
    private lateinit var btnVoltar: Button
    private lateinit var btnCadastrarBarril: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var barrilAdapter: BarrilAdapter
    private lateinit var edtFiltroNome: EditText
    private var filtroStatus: String = "Todos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_barris)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        edtFiltroNome = findViewById(R.id.edtFiltroNome)

        btnCadastrarBarril = findViewById(R.id.btnCadastrarBarril)
        btnCadastrarBarril.setOnClickListener {
            val intent = Intent(this, AdicionarBarril::class.java)
            startActivity(intent)
        }

        btnVoltar = findViewById(R.id.btnVoltarBarril)
        btnVoltar.setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerViewBarris)
        recyclerView.layoutManager = LinearLayoutManager(this)

        edtFiltroNome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                aplicarFiltro(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        val opcoesFiltro = arrayOf("Todos", "Cheio", "No Cliente", "Sujo", "Limpo", "Favoritos")
        val spinner: Spinner = findViewById(R.id.spinnerFiltro)
        val adapter = ArrayAdapter(this, R.layout.spinner_item_text_normal, opcoesFiltro)
        adapter.setDropDownViewResource(R.layout.spinner_item_text)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                filtroStatus = opcoesFiltro[position]
                aplicarFiltro(edtFiltroNome.text.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        buscarBarrilPorEmail()
    }

    private fun buscarBarrilPorEmail() {
        FirebaseRepository().pegarEmail { email ->
            if (email != null) {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("barris")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val barris = mutableListOf<Pair<String, Barril>>()
                        for (document in querySnapshot.documents) {
                            val id = document.id
                            val nome = document.getString("nome") ?: ""
                            val capacidade = document.getLong("capacidade")?.toInt() ?: 0
                            val propriedade = document.getString("propriedade") ?: ""
                            val status = document.getString("status") ?: ""
                            val liquido = document.getString("liquido") ?: ""
                            val isFavorite = document.getBoolean("isFavorite") ?: false
                            barris.add(id to Barril(nome, capacidade, propriedade, status, liquido, isFavorite))
                            Log.d("Teste", "Capacidade: $capacidade Propriedade: $propriedade")
                        }

                        if (barris.isEmpty()) {
                            Toast.makeText(this, "Nenhum barril encontrado.", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }

                        barris.sortBy { it.second.nome }

                        barrilAdapter = BarrilAdapter(barris.map { it.second }) { barril ->
                            val intent = Intent(this, DetalhesBarril::class.java)
                            intent.putExtra("id", barris.find { it.second == barril }?.first)
                            intent.putExtra("nome", barril.nome)
                            intent.putExtra("capacidade", barril.capacidade)
                            intent.putExtra("propriedade", barril.propriedade)
                            intent.putExtra("status", barril.status)
                            intent.putExtra("liquido", barril.liquido)
                            intent.putExtra("favorito", barril.isFavorite)
                            startActivity(intent)
                        }

                        recyclerView.adapter = barrilAdapter
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(
                            this,
                            "Erro ao carregar barris: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun aplicarFiltro(nomeFiltro: String) {
        if (!::barrilAdapter.isInitialized) return

        val barrisFiltradosPorNome = barrilAdapter.getOriginalBarris().filter {
            it.nome.contains(nomeFiltro, ignoreCase = true)
        }

        val barrisFiltradosPorStatus = when (filtroStatus) {
            "Cheio" -> barrisFiltradosPorNome.filter { it.status == "Cheio" }
            "No Cliente" -> barrisFiltradosPorNome.filter { it.status == "No Cliente" }
            "Sujo" -> barrisFiltradosPorNome.filter { it.status == "Sujo" }
            "Limpo" -> barrisFiltradosPorNome.filter { it.status == "Limpo" }
            "Favoritos" -> barrisFiltradosPorNome.filter { it.isFavorite }
            else -> barrisFiltradosPorNome
        }

        barrilAdapter.updateBarris(barrisFiltradosPorStatus)
    }

    override fun onResume() {
        super.onResume()
        buscarBarrilPorEmail()
    }
}
