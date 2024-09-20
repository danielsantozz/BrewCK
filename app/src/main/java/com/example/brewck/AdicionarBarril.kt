package com.example.brewck

import FirebaseRepository
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast

class AdicionarBarril : AppCompatActivity() {
    private lateinit var edtNomeBarril : EditText
    private lateinit var edtCapacidadeBarril : EditText
    private lateinit var btnAdicionarBarril : Button
    private lateinit var spinner : Spinner
    private lateinit var btnVoltarAddBarril : Button
    private val repository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_barril)

        edtNomeBarril = findViewById(R.id.edtNomeBarril)
        edtCapacidadeBarril = findViewById(R.id.edtCapacidadeBarril)
        btnVoltarAddBarril = findViewById(R.id.btnVoltarAddBarril)
        btnVoltarAddBarril.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        val items = listOf("Próprio", "Terceiro")
        spinner = findViewById(R.id.spinner)
        val adapter = ArrayAdapter(this, R.layout.spinner_item_text_black, items)
        adapter.setDropDownViewResource(R.layout.spinner_item_text)
        spinner.adapter = adapter

        btnAdicionarBarril = findViewById(R.id.btnAdicionarBarril)
        btnAdicionarBarril.setOnClickListener {
            if (validarCampos()) {
                val nome = edtNomeBarril.text.toString().trim()
                val capacidade = edtCapacidadeBarril.text.toString().toInt()
                val propriedade = spinner.selectedItem.toString()

                repository.pegarEmail { email ->
                    if (email != null) {
                        repository.adicionarBarril(nome, email, capacidade, propriedade, "Limpo", "Nenhum", false) { sucesso ->
                            if (sucesso) {
                                Toast.makeText(this, "Barril adicionado com sucesso!", Toast.LENGTH_SHORT).show()
                                val resultIntent = Intent()
                                setResult(Activity.RESULT_OK, resultIntent)
                                finish()
                            } else {
                                Toast.makeText(this, "Falha ao adicionar o barril.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun validarCampos(): Boolean {
        val nome = edtNomeBarril.text.toString().trim()
        val capacidadeText = edtCapacidadeBarril.text.toString().trim()

        if (nome.isEmpty() || capacidadeText.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (nome.length > 100) {
            Toast.makeText(this, "Nome deve ter no máximo 100 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (capacidadeText.length > 4) {
            Toast.makeText(this, "Capacidade deve ter no máximo 4 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
