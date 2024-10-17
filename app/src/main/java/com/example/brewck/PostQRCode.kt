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
    lateinit var btnEncher: Button
    lateinit var btnVender: Button
    lateinit var btnPegar: Button
    lateinit var btnLimpar: Button

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

        txtNome = findViewById(R.id.txtQRNomeBarril)
        txtCapacidade = findViewById(R.id.txtCapacidade)
        txtStatus = findViewById(R.id.txtStatus)
        imgBarril = findViewById(R.id.imgBarril)
        btnVoltar = findViewById(R.id.btnVoltar)
        btnEncher = findViewById(R.id.btnEncher)
        btnVender = findViewById(R.id.btnVender)
        btnPegar = findViewById(R.id.btnPegar)
        btnLimpar = findViewById(R.id.btnLimpar)

        controller = PostQRCodeController(this)

        btnVoltar.setOnClickListener {
            finish()
        }

        val extras = intent.extras
        val id = extras?.getString("id")

        btnEncher.setOnClickListener {
            if (id != null) {
                controller.buscarLiquidos { liquidos ->
                    if (liquidos.isNotEmpty()) {
                        mostrarDialogLiquidos(id, liquidos)
                    } else {
                        Toast.makeText(this, "Nenhum líquido encontrado.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnVender.setOnClickListener {
            if (id != null) {
                controller.buscarClientes { clientes ->
                    if (clientes.isNotEmpty()) {
                        controller.buscarBarrilPorId(id) { barril ->
                            barril?.let {
                                mostrarDialogClientes(it.id, it.nome, clientes)
                            }
                        }
                    } else {
                        Toast.makeText(this, "Nenhum cliente encontrado.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnPegar.setOnClickListener {
            if (id != null) {
                controller.atualizarStatus(id, "Sujo") { success ->
                    if (success) controller.buscarBarrilPorId(id) { controller.atualizarUI(it) }
                }
            }
        }
        btnLimpar.setOnClickListener {
            if (id != null) {
                controller.atualizarStatus(id, "Limpo") { success ->
                    if (success) controller.buscarBarrilPorId(id) { controller.atualizarUI(it) }
                }
            }
        }

        id?.let {
            controller.buscarBarrilPorId(it) { barril ->
                controller.atualizarUI(barril)
            }
        } ?: run {
            Toast.makeText(this, "ID do barril não fornecido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogLiquidos(barrilId: String, liquidos: List<String>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_encher_barril, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerLiquidos)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, liquidos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        AlertDialog.Builder(this)
            .setTitle("Escolha um líquido")
            .setView(dialogView)
            .setPositiveButton("Encher") { _, _ ->
                val liquidoSelecionado = spinner.selectedItem.toString()
                controller.atualizarLiquidoDoBarril(barrilId, liquidoSelecionado)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogClientes(barrilId: String, nomeBarril: String, clientes: List<String>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_encher_barril, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerLiquidos)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, clientes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        AlertDialog.Builder(this)
            .setTitle("Escolha um cliente")
            .setView(dialogView)
            .setPositiveButton("Vender") { _, _ ->
                val clienteSelecionado = spinner.selectedItem.toString()

                controller.atualizarClienteNoBarril(barrilId, clienteSelecionado)
                controller.atualizarBarrilNoCliente(clienteSelecionado, nomeBarril, barrilId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }



}
