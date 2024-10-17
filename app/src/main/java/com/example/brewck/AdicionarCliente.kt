package com.example.brewck

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.brewck.controllers.AdicionarClienteController

class AdicionarCliente : AppCompatActivity() {
    private lateinit var edtNomeCliente: EditText
    private lateinit var edtCPFCliente: EditText
    private lateinit var btnAdicionarCliente: Button
    private lateinit var edtEnderecoCliente: EditText
    private lateinit var btnVoltarAddCliente: Button
    private val clienteController = AdicionarClienteController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_cliente)

        edtNomeCliente = findViewById(R.id.edtNomeCliente)
        edtCPFCliente = findViewById(R.id.edtCPFCliente)
        edtEnderecoCliente = findViewById(R.id.edtEnderecoCliente)
        btnVoltarAddCliente = findViewById(R.id.btnVoltarAddCliente)

        btnVoltarAddCliente.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        btnAdicionarCliente = findViewById(R.id.btnAdicionarCliente)
        btnAdicionarCliente.setOnClickListener {
            if (validarCampos()) {
                val nome = edtNomeCliente.text.toString().trim()
                val cpf = edtCPFCliente.text.toString().trim()
                val endereco = edtEnderecoCliente.text.toString().trim()

                // Chama o controller para adicionar o cliente
                clienteController.adicionarCliente(nome, cpf, "Nenhum", endereco, "") { sucesso ->
                    if (sucesso) {
                        Toast.makeText(this, "Cliente adicionado com sucesso!", Toast.LENGTH_SHORT).show()
                        val resultIntent = Intent()
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    } else {
                        Toast.makeText(this, "Falha ao adicionar o cliente.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun validarCampos(): Boolean {
        val nome = edtNomeCliente.text.toString().trim()
        val cpf = edtCPFCliente.text.toString().trim()
        val endereco = edtEnderecoCliente.text.toString().trim()

        if (nome.isEmpty() || cpf.isEmpty() || endereco.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (nome.length > 100) {
            Toast.makeText(this, "Nome deve ter no máximo 100 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (cpf.length != 11) {
            Toast.makeText(this, "CPF deve ter 11 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (endereco.length > 100) {
            Toast.makeText(this, "Endereço deve ter no máximo 100 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
