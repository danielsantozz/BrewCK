package com.example.brewck

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.brewck.controllers.AdicionarBarrilController

class AdicionarBarril : AppCompatActivity() {
    private lateinit var edtNomeBarril: EditText
    private lateinit var edtCapacidadeBarril: EditText
    private lateinit var btnAdicionarBarril: Button
    private lateinit var spinner: Spinner
    private lateinit var btnVoltarAddBarril: Button
    private val controller = AdicionarBarrilController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_barril)

        edtNomeBarril = findViewById(R.id.edtNomeBarril)
        edtCapacidadeBarril = findViewById(R.id.edtCapacidadeBarril)
        btnVoltarAddBarril = findViewById(R.id.btnVoltarAddBarril)
        btnAdicionarBarril = findViewById(R.id.btnAdicionarBarril)
        spinner = findViewById(R.id.spinner)

        val items = listOf("Próprio", "Terceiro")
        val adapter = ArrayAdapter(this, R.layout.spinner_item_text_black, items)
        adapter.setDropDownViewResource(R.layout.spinner_item_text)
        spinner.adapter = adapter

        btnVoltarAddBarril.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        btnAdicionarBarril.setOnClickListener {
            val nome = edtNomeBarril.text.toString().trim()
            val capacidade = edtCapacidadeBarril.text.toString().toIntOrNull() ?: 0
            val propriedade = spinner.selectedItem.toString()

            controller.adicionarBarril(
                nome = nome,
                capacidade = capacidade,
                propriedade = propriedade,
                status = "Limpo",
                liquido = "Nenhum",
                isFavorite = false,
                cliente = ""
            ) { sucesso, mensagem ->
                if (sucesso) {
                    Toast.makeText(this, "Barril adicionado com sucesso!", Toast.LENGTH_SHORT).show()
                    val resultIntent = Intent()
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
