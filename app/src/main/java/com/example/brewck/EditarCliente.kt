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

        // Obtém os dados passados pela Intent
        val nome = intent.getStringExtra("nome")
        val cpf = intent.getStringExtra("cpf")
        val endereco = intent.getStringExtra("endereco")

        // Inicializa os componentes de entrada
        edtClienteNome = findViewById(R.id.edtClienteNome)
        edtClienteCPF = findViewById(R.id.edtClienteCPF)
        edtClienteEndereco = findViewById(R.id.edtClienteEndereço)

        // Preenche os campos com os valores recebidos
        edtClienteNome.setText(nome)
        edtClienteCPF.setText(cpf)
        edtClienteEndereco.setText(endereco)

        // Inicializa os botões da interface
        btnVoltarCliente = findViewById(R.id.btnVoltarEdtCliente)
        btnEditarCliente = findViewById(R.id.btnEditarCliente)
        btnDeletarCliente = findViewById(R.id.btnDeletarCliente)

        // Inicializa o controlador para a edição do cliente
        controller = EditarClienteController(this)

        // Configura o botão de voltar para fechar a Activity e retornar ao estado anterior
        btnVoltarCliente.setOnClickListener {
            val resultIntent = Intent()  // Cria um Intent para retornar um resultado
            setResult(Activity.RESULT_OK, resultIntent)  // Define o resultado como OK
            finish()  // Fecha a Activity atual
        }

        // Configura o botão de editar para chamar a função de atualização do cliente
        btnEditarCliente.setOnClickListener {
            atualizarCliente()  // Chama a função que realiza a atualização dos dados do cliente
        }

        // Configura o botão de deletar para chamar a função de exclusão do cliente
        btnDeletarCliente.setOnClickListener {
            excluirCliente()  // Chama a função que realiza a exclusão do cliente
        }
    }

    // Função para atualizar os dados do cliente
    private fun atualizarCliente() {
        // Obtém o ID do cliente e os novos valores dos campos
        val id = intent.getStringExtra("id").toString()
        val newNome = edtClienteNome.text.toString()
        val newCPF = edtClienteCPF.text.toString()
        val newEndereco = edtClienteEndereco.text.toString()

        // Valida os campos antes de realizar a atualização
        if (controller.validarCampos(newNome, newCPF, newEndereco)) {
            // Chama o método de atualização no controller
            controller.atualizarCliente(id, newNome, newCPF, newEndereco) { sucesso ->
                if (sucesso) {
                    // Exibe uma mensagem de sucesso caso a atualização tenha sido bem-sucedida
                    Toast.makeText(this, "Cliente atualizado com sucesso", Toast.LENGTH_SHORT).show()
                } else {
                    // Exibe uma mensagem de erro caso a atualização falhe
                    Toast.makeText(this, "Erro ao atualizar cliente", Toast.LENGTH_SHORT).show()
                }
            }

            // Envia um resultado OK e fecha a Activity
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    // Função para excluir o cliente
    private fun excluirCliente() {
        // Obtém o ID do cliente
        val id = intent.getStringExtra("id").toString()

        // Chama o método de exclusão no controller
        controller.deletarCliente(id) { sucesso ->
            if (sucesso) {
                // Exibe uma mensagem de sucesso caso a exclusão tenha sido bem-sucedida
                Toast.makeText(this, "Cliente excluído com sucesso", Toast.LENGTH_SHORT).show()
            } else {
                // Exibe uma mensagem de erro caso a exclusão falhe
                Toast.makeText(this, "Erro ao excluir cliente", Toast.LENGTH_SHORT).show()
            }
        }

        // Envia um resultado OK e fecha a Activity
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
