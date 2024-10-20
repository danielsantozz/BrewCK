package com.example.brewck

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.brewck.controllers.EditarBarrilController

class EditarBarril : AppCompatActivity() {
    private lateinit var edtBarrilNome: EditText
    private lateinit var edtBarrilCapacidade: EditText
    private lateinit var edtBarrilPropriedade: Spinner
    private lateinit var edtBarrilStatus: Spinner
    private lateinit var edtBarrilLiquido: EditText
    private lateinit var btnVoltar: Button
    private lateinit var btnEditar: Button
    private lateinit var btnDeletar: Button
    private lateinit var controller: EditarBarrilController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_barril)

        controller = EditarBarrilController(this)

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
            controller.mostrarMensagem("Por favor, preencha todos os campos.")
            return false
        }

        if (nome.length > 100) {
            controller.mostrarMensagem("Nome deve ter no máximo 100 caracteres.")
            return false
        }

        if (capacidadeText.length > 4) {
            controller.mostrarMensagem("Capacidade deve ter no máximo 4 caracteres.")
            return false
        }

        return true
    }

    private fun atualizarBarril() {
        if (!validarCampos()) {
            return
        }

        val id = intent.getStringExtra("id").toString()
        val newNome = edtBarrilNome.text.toString()
        val newCapacidade = edtBarrilCapacidade.text.toString().toInt()
        val newPropriedade = edtBarrilPropriedade.selectedItem.toString()
        val newStatus = edtBarrilStatus.selectedItem.toString()
        val newLiquido = edtBarrilLiquido.text.toString()

        controller.atualizarBarril(id, newNome, newCapacidade, newPropriedade, newStatus, newLiquido) { sucesso ->
            if (sucesso) {
                controller.mostrarMensagem("Barril atualizado com sucesso")
            } else {
                controller.mostrarMensagem("Erro ao atualizar barril")
            }
        }

        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun excluirBarril() {
        val id = intent.getStringExtra("id").toString()

        controller.deletarBarril(id) { sucesso ->
            if (sucesso) {
                controller.mostrarMensagem("Barril excluído com sucesso")
            } else {
                controller.mostrarMensagem("Erro ao excluir barril")
            }
        }

        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
