package com.example.brewck

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.brewck.controllers.EditarBarrilController

class EditarBarril : AppCompatActivity() {
    private lateinit var edtBarrilNome: EditText
    private lateinit var edtBarrilCapacidade: EditText
    private lateinit var edtBarrilPropriedade: Spinner
    private lateinit var btnVoltar: Button
    private lateinit var btnEditar: Button
    private lateinit var btnDeletar: Button
    private lateinit var controller: EditarBarrilController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_barril)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa o controlador responsável pela lógica de edição de barril
        controller = EditarBarrilController(this)

        // Obtém os dados passados pela Intent
        val nome = intent.getStringExtra("nome")
        val capacidade = intent.getIntExtra("capacidade", 0)
        val propriedade = intent.getStringExtra("propriedade")

        // Inicializa os componentes da interface
        edtBarrilNome = findViewById(R.id.edtBarrilNome)
        edtBarrilCapacidade = findViewById(R.id.edtBarrilCapacidade)
        edtBarrilPropriedade = findViewById(R.id.edtBarrilPropriedade)

        // Configura o botão de voltar para fechar a Activity e retornar ao estado anterior
        btnVoltar = findViewById(R.id.btnVoltarEdtBarril)
        btnVoltar.setOnClickListener {
            val resultIntent = Intent()  // Cria um Intent para retornar um resultado
            setResult(Activity.RESULT_OK, resultIntent)  // Define o resultado como OK
            finish()  // Fecha a Activity atual
        }

        // Configura o botão de editar para chamar a função de atualização do barril
        btnEditar = findViewById(R.id.btnEditar)
        btnEditar.setOnClickListener { atualizarBarril() }

        // Configura o botão de deletar para chamar a função de exclusão do barril
        btnDeletar = findViewById(R.id.btnDeletar)
        btnDeletar.setOnClickListener { excluirBarril() }

        // Cria o array de opções para o Spinner (Propriedade)
        val itemPropriedade = arrayOf("Próprio", "Terceiro")
        val adapterPropriedade = ArrayAdapter(this, R.layout.spinner_item_text_black, itemPropriedade)
        adapterPropriedade.setDropDownViewResource(R.layout.spinner_item_text)
        edtBarrilPropriedade.adapter = adapterPropriedade  // Aplica o adapter ao Spinner

        // Preenche os campos com os dados obtidos pela Intent
        edtBarrilNome.setText(nome)
        edtBarrilCapacidade.setText(capacidade.toString())
        edtBarrilPropriedade.setSelection(adapterPropriedade.getPosition(propriedade.toString()))
    }

    // Função para validar os campos do formulário
    private fun validarCampos(): Boolean {
        val nome = edtBarrilNome.text.toString().trim()
        val capacidadeText = edtBarrilCapacidade.text.toString().trim()

        // Verifica se os campos obrigatórios estão preenchidos
        if (nome.isEmpty() || capacidadeText.isEmpty()) {
            controller.mostrarMensagem("Por favor, preencha todos os campos.")  // Exibe uma mensagem de erro
            return false
        }

        // Verifica o comprimento do nome (máximo 100 caracteres)
        if (nome.length > 100) {
            controller.mostrarMensagem("Nome deve ter no máximo 100 caracteres.")  // Exibe mensagem de erro
            return false
        }

        // Verifica o comprimento da capacidade (máximo 4 caracteres)
        if (capacidadeText.length > 4) {
            controller.mostrarMensagem("Capacidade deve ter no máximo 4 caracteres.")  // Exibe mensagem de erro
            return false
        }

        return true  // Retorna true se os campos forem válidos
    }

    // Função para atualizar as informações do barril
    private fun atualizarBarril() {
        if (!validarCampos()) {  // Verifica se os campos são válidos antes de prosseguir
            return
        }

        // Obtém os dados dos campos preenchidos pelo usuário
        val id = intent.getStringExtra("id").toString()
        val newNome = edtBarrilNome.text.toString()
        val newCapacidade = edtBarrilCapacidade.text.toString().toInt()
        val newPropriedade = edtBarrilPropriedade.selectedItem.toString()

        // Chama o método de atualização do barril e exibe uma mensagem de sucesso ou erro
        controller.atualizarBarril(id, newNome, newCapacidade, newPropriedade) { sucesso ->
            if (sucesso) {
                controller.mostrarMensagem("Barril atualizado com sucesso")  // Mensagem de sucesso
            } else {
                controller.mostrarMensagem("Erro ao atualizar barril")  // Mensagem de erro
            }
        }

        // Envia um resultado OK e fecha a Activity
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    // Função para excluir o barril
    private fun excluirBarril() {
        val id = intent.getStringExtra("id").toString()

        // Chama o método de exclusão do barril e exibe uma mensagem de sucesso ou erro
        controller.deletarBarril(id) { sucesso ->
            if (sucesso) {
                controller.mostrarMensagem("Barril excluído com sucesso")  // Mensagem de sucesso
            } else {
                controller.mostrarMensagem("Erro ao excluir barril")  // Mensagem de erro
            }
        }

        // Envia um resultado OK e fecha a Activity
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
