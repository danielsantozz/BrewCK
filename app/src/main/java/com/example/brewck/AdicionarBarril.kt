package com.example.brewck

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.brewck.controllers.AdicionarBarrilController

class AdicionarBarril : AppCompatActivity() {

    private lateinit var edtNomeBarril: EditText
    private lateinit var edtCapacidadeBarril: EditText
    private lateinit var btnAdicionarBarril: Button
    private lateinit var spinner: Spinner
    private lateinit var btnVoltarAddBarril: Button
    private val controller = AdicionarBarrilController()  // Controlador para gerenciar a adição do barril

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_barril)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialização das variáveis com os componentes da interface
        edtNomeBarril = findViewById(R.id.edtNomeBarril)
        edtCapacidadeBarril = findViewById(R.id.edtCapacidadeBarril)
        btnVoltarAddBarril = findViewById(R.id.btnVoltarAddBarril)
        btnAdicionarBarril = findViewById(R.id.btnAdicionarBarril)
        spinner = findViewById(R.id.spinner)

        // Configuração do Spinner com as opções de propriedade do barril ("Próprio" ou "Terceiro")
        val items = listOf("Próprio", "Terceiro")
        val adapter = ArrayAdapter(this, R.layout.spinner_item_text_black, items)
        adapter.setDropDownViewResource(R.layout.spinner_item_text)  // Definindo o estilo do item do Spinner
        spinner.adapter = adapter  // Atribui o adaptador ao Spinner

        // Configuração do botão 'Voltar', que fecha a Activity e retorna à Activity anterior
        btnVoltarAddBarril.setOnClickListener {
            val resultIntent = Intent()  // Cria uma Intent para enviar um resultado
            setResult(Activity.RESULT_OK, resultIntent)  // Define o resultado da Activity como OK
            finish()  // Finaliza a Activity e retorna à anterior
        }

        // Configuração do botão 'Adicionar Barril', que chama o método para adicionar um novo barril
        btnAdicionarBarril.setOnClickListener {
            // Coleta os dados inseridos pelo usuário
            val nome = edtNomeBarril.text.toString().trim()
            val capacidade = edtCapacidadeBarril.text.toString().toIntOrNull() ?: 0  // Converte a capacidade para inteiro ou 0
            val propriedade = spinner.selectedItem.toString()  // Obtém o valor selecionado no Spinner

            // Chama o controlador para adicionar o barril
            controller.adicionarBarril(
                nome = nome, // Nome inicial do barril
                capacidade = capacidade, // Capacidade inicial do barril
                propriedade = propriedade, // Propriedade inicial do barril
                status = "Limpo",  // Status inicial do barril
                liquido = "Nenhum",  // Líquido inicial do barril
                isFavorite = false,  // Inicialmente, o barril não é favorito
                cliente = ""  // Inicialmente, não há cliente associado ao barril
            ) { sucesso, mensagem ->  // Callback para tratar o sucesso ou erro da operação
                if (sucesso) {
                    // Se o barril foi adicionado com sucesso, exibe uma mensagem e fecha a Activity
                    Toast.makeText(this, "Barril adicionado com sucesso!", Toast.LENGTH_SHORT).show()
                    val resultIntent = Intent()  // Cria uma Intent para enviar o resultado
                    setResult(Activity.RESULT_OK, resultIntent)  // Define o resultado como OK
                    finish()  // Finaliza a Activity
                } else {
                    // Se houve erro, exibe a mensagem de erro
                    Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
