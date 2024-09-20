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

class EditarBarril : AppCompatActivity() {
    private lateinit var edtBarrilNome : EditText
    private lateinit var edtBarrilCapacidade : EditText
    private lateinit var edtBarrilPropriedade : Spinner
    private lateinit var edtBarrilStatus : Spinner
    private lateinit var edtBarrilLiquido : EditText
    private lateinit var btnVoltar : Button
    private lateinit var btnEditar : Button
    private lateinit var btnDeletar : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_barril)

        val nome = intent.getStringExtra("nome")
        val capacidade = intent.getIntExtra("capacidade", 0)
        val propriedade = intent.getStringExtra("propriedade")
        val status = intent.getStringExtra("status")
        val liquido = intent.getStringExtra("liquido")

        edtBarrilNome = findViewById(R.id.edtBarrilNome)
        edtBarrilCapacidade = findViewById(R.id.edtBarrilCapacidade)
        edtBarrilPropriedade = findViewById(R.id.edtBarrilPropriedade)
        edtBarrilStatus = findViewById(R.id.edtBarrilStatus)
        edtBarrilLiquido = findViewById(R.id.edtBarrilLiquido)

        btnVoltar = findViewById(R.id.btnVoltarEdtBarril)
        btnVoltar.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        btnEditar = findViewById(R.id.btnEditar)
        btnEditar.setOnClickListener { atualizarBarril() }

        btnDeletar = findViewById(R.id.btnDeletar)
        btnDeletar.setOnClickListener { excluirBarril() }

        val itemStatus = arrayOf("Limpo", "Cheio", "No Cliente", "Sujo")
        val adapterStatus = ArrayAdapter(this, R.layout.spinner_item_text_black, itemStatus)
        adapterStatus.setDropDownViewResource(R.layout.spinner_item_text)
        edtBarrilStatus.adapter = adapterStatus

        val itemPropriedade = arrayOf("Próprio", "Terceiro")
        val adapterPropriedade = ArrayAdapter(this, R.layout.spinner_item_text_black, itemPropriedade)
        adapterPropriedade.setDropDownViewResource(R.layout.spinner_item_text)
        edtBarrilPropriedade.adapter = adapterPropriedade

        edtBarrilNome.setText(nome)
        edtBarrilCapacidade.setText(capacidade.toString())
        edtBarrilPropriedade.setSelection(adapterPropriedade.getPosition(propriedade.toString()))
        edtBarrilStatus.setSelection(adapterStatus.getPosition(status.toString()))
        edtBarrilLiquido.setText(liquido)
    }

    private fun validarCampos(): Boolean {
        val nome = edtBarrilNome.text.toString().trim()
        val capacidadeText = edtBarrilCapacidade.text.toString().trim()

        if (nome.isEmpty() || capacidadeText.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verifica se o nome possui no máximo 100 caracteres
        if (nome.length > 100) {
            Toast.makeText(this, "Nome deve ter no máximo 100 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verifica se a capacidade possui no máximo 4 caracteres
        if (capacidadeText.length > 4) {
            Toast.makeText(this, "Capacidade deve ter no máximo 4 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    fun atualizarBarril() {
        if (!validarCampos()) {
            return
        }

        val firebaseRepository = FirebaseRepository()

        val id = intent.getStringExtra("id").toString()
        val newNome = edtBarrilNome.text.toString()
        val newCapacidade = edtBarrilCapacidade.text.toString().toInt()
        val newPropriedade = edtBarrilPropriedade.selectedItem.toString()
        val newStatus = edtBarrilStatus.selectedItem.toString()
        val newLiquido = edtBarrilLiquido.text.toString()

        firebaseRepository.atualizarBarril(id, newNome, newCapacidade, newPropriedade, newStatus, newLiquido) { sucesso ->
            if (sucesso) {
                Toast.makeText(this, "Barril atualizado com sucesso", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erro ao atualizar barril", Toast.LENGTH_SHORT).show()
            }
        }

        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    fun excluirBarril() {
        val firebaseRepository = FirebaseRepository()
        val id = intent.getStringExtra("id").toString()

        firebaseRepository.deletarBarril(id) { sucesso ->
            if (sucesso) {
                Toast.makeText(this, "Barril excluído com sucesso", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erro ao excluir barril", Toast.LENGTH_SHORT).show()
            }
        }

        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
