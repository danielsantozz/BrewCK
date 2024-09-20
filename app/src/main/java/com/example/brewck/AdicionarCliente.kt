package com.example.brewck

import FirebaseRepository
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class AdicionarCliente : AppCompatActivity() {
    private lateinit var edtNomeCliente : EditText
    private lateinit var edtCPFCliente : EditText
    private lateinit var btnAdicionarCliente : Button
    private lateinit var edtEnderecoCliente : EditText
    private lateinit var btnVoltarAddCliente : Button
    private val repository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_cliente)

        edtNomeCliente = findViewById(R.id.edtNomeCliente)
        edtCPFCliente = findViewById(R.id.edtCPFCliente)
        btnVoltarAddCliente = findViewById(R.id.btnVoltarAddCliente)
        btnVoltarAddCliente.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        edtEnderecoCliente = findViewById(R.id.edtEnderecoCliente)

        btnAdicionarCliente = findViewById(R.id.btnAdicionarCliente)
        btnAdicionarCliente.setOnClickListener {
            if (validarCampos()) {
                val nome = edtNomeCliente.text.toString().trim()
                val cpf = edtCPFCliente.text.toString().trim()
                val endereco = edtEnderecoCliente.text.toString().trim()

                repository.pegarEmail { email ->
                    if (email != null) {
                        repository.adicionarCliente(nome, email, cpf, "Nenhum", endereco, "") { sucesso ->
                            if (sucesso) {
                                Toast.makeText(this, "Cliente adicionado com sucesso!", Toast.LENGTH_SHORT).show()
                                val resultIntent = Intent()
                                setResult(Activity.RESULT_OK, resultIntent)
                                finish()
                            } else {
                                Toast.makeText(this, "Falha ao adicionar o cliente.", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "Capacidade deve ter no máximo 4 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}