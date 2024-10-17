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
    private var filtroStatus: String = "Todos"
    private val controller = BarrilController()

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
        progressBar = findViewById(R.id.progressBar)

        btnCadastrarBarril = findViewById(R.id.btnCadastrarBarril)
        btnCadastrarBarril.setOnClickListener {
            val intent = Intent(this, AdicionarBarril::class.java)
            startActivity(intent)
        }

        btnVoltar = findViewById(R.id.btnVoltarBarril)
        btnVoltar.setOnClickListener { finish() }

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
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filtroStatus = opcoesFiltro[position]
                aplicarFiltro(edtFiltroNome.text.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        carregarBarris()
    }

    private fun carregarBarris() {
        progressBar.visibility = ProgressBar.VISIBLE

        controller.buscarBarrilPorEmail { barris ->
            progressBar.visibility = ProgressBar.GONE

            if (barris.isEmpty()) {
                Toast.makeText(this, "Nenhum barril encontrado.", Toast.LENGTH_SHORT).show()
                return@buscarBarrilPorEmail
            }

            barrilAdapter = BarrilAdapter(barris) { barril ->
                val intent = Intent(this, DetalhesBarril::class.java)
                intent.putExtra("id", barril.id)
                intent.putExtra("nome", barril.nome)
                intent.putExtra("capacidade", barril.capacidade)
                intent.putExtra("propriedade", barril.propriedade)
                intent.putExtra("status", barril.status)
                intent.putExtra("liquido", barril.liquido)
                intent.putExtra("favorito", barril.isFavorite)
                intent.putExtra("cliente", barril.cliente)
                startActivity(intent)
            }
            recyclerView.adapter = barrilAdapter
        }
    }

    private fun aplicarFiltro(nomeFiltro: String) {
        if (::barrilAdapter.isInitialized) {
            controller.filtrarBarris(nomeFiltro, filtroStatus) { barrisFiltrados ->
                barrilAdapter.updateBarris(barrisFiltrados)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        carregarBarris()
    }
}
