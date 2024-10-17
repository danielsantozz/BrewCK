package com.example.brewck

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.brewck.controllers.EditarClienteController

class EditarCliente : AppCompatActivity() {
    private lateinit var edtClienteNome: EditText
    private lateinit var edtClienteCPF: EditText
    private lateinit var edtClienteEndereco: EditText
    private lateinit var btnVoltarCliente: Button
    private lateinit var btnEditarCliente: Button
    private lateinit var btnDeletarCliente: Button
    private lateinit var spinnerBarris: Spinner
    private lateinit var controller: EditarClienteController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_cliente)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val nome = intent.getStringExtra("nome")
        val cpf = intent.getStringExtra("cpf")
        val barril = intent.getStringExtra("barril")
        val endereco = intent.getStringExtra("endereco")

        edtClienteNome = findViewById(R.id.edtClienteNome)
        edtClienteCPF = findViewById(R.id.edtClienteCPF)
        spinnerBarris = findViewById(R.id.spinnerBarris)
        edtClienteEndereco = findViewById(R.id.edtClienteEndereço)

        edtClienteNome.setText(nome)
        edtClienteCPF.setText(cpf)
        edtClienteEndereco.setText(endereco)

        btnVoltarCliente = findViewById(R.id.btnVoltarEdtCliente)
        btnEditarCliente = findViewById(R.id.btnEditarCliente)
        btnDeletarCliente = findViewById(R.id.btnDeletarCliente)

        controller = EditarClienteController(this) // Passa o contexto para o controller

        btnVoltarCliente.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        controller.buscarBarrisPorEmail(spinnerBarris)

        btnEditarCliente.setOnClickListener {
            atualizarCliente()
        }

        btnDeletarCliente.setOnClickListener {
            excluirCliente()
        }
    }

    private fun atualizarCliente() {
        val id = intent.getStringExtra("id").toString()
        val newNome = edtClienteNome.text.toString()
        val newCPF = edtClienteCPF.text.toString()
        val newEndereco = edtClienteEndereco.text.toString()
        val barrilSelecionado = spinnerBarris.selectedItem.toString()

        if (controller.validarCampos(newNome, newCPF, newEndereco)) {
            controller.atualizarCliente(id, newNome, newCPF, barrilSelecionado, newEndereco) { sucesso ->
                if (sucesso) {
                    Toast.makeText(this, "Cliente atualizado com sucesso", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Erro ao atualizar cliente", Toast.LENGTH_SHORT).show()
                }
            }

            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun excluirCliente() {
        val id = intent.getStringExtra("id").toString()

        controller.deletarCliente(id) { sucesso ->
            if (sucesso) {
                Toast.makeText(this, "Cliente excluído com sucesso", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erro ao excluir cliente", Toast.LENGTH_SHORT).show()
            }
        }

        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
