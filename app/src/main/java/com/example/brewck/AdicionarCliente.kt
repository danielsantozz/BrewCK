package com.example.brewck

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.brewck.controllers.AdicionarClienteController

class AdicionarCliente : AppCompatActivity() {

    private lateinit var edtNomeCliente: EditText
    private lateinit var edtCPFCliente: EditText
    private lateinit var btnAdicionarCliente: Button
    private lateinit var edtEnderecoCliente: EditText
    private lateinit var btnVoltarAddCliente: Button
    private val clienteController = AdicionarClienteController()  // Controlador para gerenciar a adição de cliente.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_cliente)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialização das variáveis com os componentes da interface.
        edtNomeCliente = findViewById(R.id.edtNomeCliente)
        edtCPFCliente = findViewById(R.id.edtCPFCliente)
        edtEnderecoCliente = findViewById(R.id.edtEnderecoCliente)
        btnVoltarAddCliente = findViewById(R.id.btnVoltarAddCliente)

        // Configuração do botão 'Voltar', que fecha a Activity e retorna à Activity anterior.
        btnVoltarAddCliente.setOnClickListener {
            val resultIntent = Intent()  // Cria uma Intent para enviar o resultado.
            setResult(Activity.RESULT_OK, resultIntent)  // Define o resultado da Activity como OK.
            finish()  // Finaliza a Activity e retorna à anterior.
        }

        // Configuração do botão 'Adicionar Cliente', que chama o método para adicionar o cliente.
        btnAdicionarCliente = findViewById(R.id.btnAdicionarCliente)
        btnAdicionarCliente.setOnClickListener {
            // Valida os campos preenchidos pelo usuário antes de prosseguir.
            if (validarCampos()) {
                // Coleta os dados inseridos pelo usuário.
                val nome = edtNomeCliente.text.toString().trim()
                val cpf = edtCPFCliente.text.toString().trim()
                val endereco = edtEnderecoCliente.text.toString().trim()

                // Chama o controlador para adicionar o cliente com os dados fornecidos.
                clienteController.adicionarCliente(nome, cpf, "Nenhum", endereco, "") { sucesso, mensagem ->
                    if (sucesso) {
                        // Se o cliente foi adicionado com sucesso, exibe uma mensagem e finaliza a Activity.
                        Toast.makeText(this, "Cliente adicionado com sucesso!", Toast.LENGTH_SHORT).show()
                        val resultIntent = Intent()  // Cria uma Intent para enviar o resultado.
                        setResult(Activity.RESULT_OK, resultIntent)  // Define o resultado como OK.
                        finish()  // Finaliza a Activity.
                    } else {
                        // Se houve erro, exibe a mensagem de erro recebida no callback.
                        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Método para validar os campos antes de adicionar o cliente.
    private fun validarCampos(): Boolean {
        // Coleta os dados dos campos.
        val nome = edtNomeCliente.text.toString().trim()
        val cpf = edtCPFCliente.text.toString().trim()
        val endereco = edtEnderecoCliente.text.toString().trim()

        // Verifica se algum dos campos está vazio.
        if (nome.isEmpty() || cpf.isEmpty() || endereco.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verifica o comprimento máximo do nome (100 caracteres).
        if (nome.length > 100) {
            Toast.makeText(this, "Nome deve ter no máximo 100 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verifica se o CPF tem exatamente 11 caracteres.
        if (cpf.length != 11) {
            Toast.makeText(this, "CPF deve ter 11 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verifica o comprimento máximo do endereço (100 caracteres).
        if (endereco.length > 100) {
            Toast.makeText(this, "Endereço deve ter no máximo 100 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Se todos os campos forem válidos, retorna true.
        return true
    }
}
