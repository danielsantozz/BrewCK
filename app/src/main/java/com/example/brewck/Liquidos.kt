package com.example.brewck

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.brewck.components.LiquidoAdapter
import com.example.brewck.controllers.LiquidosController

class Liquidos : AppCompatActivity() {

    private lateinit var btnVoltar: Button
    private lateinit var edtFiltro: EditText
    private lateinit var recycler: RecyclerView
    private lateinit var btnCadastrarLiquido: ImageView
    private val liquidosController = LiquidosController()  // Inicializa o controlador para manipulação de líquidos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_liquidos)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa os componentes da interface
        btnVoltar = findViewById(R.id.btnVoltar)
        edtFiltro = findViewById(R.id.edtFiltroLiquidos)
        recycler = findViewById(R.id.recyclerViewLiquido)

        // Configura o RecyclerView para exibição da lista de líquidos
        recycler.layoutManager = LinearLayoutManager(this)

        // Cria o adaptador para o RecyclerView, passando uma lista vazia de líquidos e uma ação para deletar líquidos
        val liquidosAdapter = LiquidoAdapter(mutableListOf()) { liquido ->
            liquidosController.deletarLiquido(liquido, {
                Toast.makeText(this, "Líquido excluído!", Toast.LENGTH_SHORT).show()
                loadLiquidos()  // Recarrega os líquidos após exclusão
            }, { error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            })
        }

        // Define o adaptador para o RecyclerView
        recycler.adapter = liquidosAdapter

        // Inicializa o botão de cadastro de líquido
        btnCadastrarLiquido = findViewById(R.id.btnCadastrarLiquido)

        // Configura o clique do botão para mostrar o diálogo de adição de líquido
        btnCadastrarLiquido.setOnClickListener {
            showAddLiquidoDialog()
        }

        // Carrega a lista de líquidos quando a Activity é criada
        loadLiquidos()
    }

    // Exibe o diálogo para adicionar um novo líquido
    private fun showAddLiquidoDialog() {
        val editText = EditText(this)  // Cria um EditText para o nome do líquido
        val dialog = AlertDialog.Builder(this)
            .setTitle("Adicionar Líquido")  // Define o título do diálogo
            .setMessage("Digite o nome do líquido:")  // Define a mensagem do diálogo
            .setView(editText)  // Define o EditText como a view do diálogo
            .setPositiveButton("Adicionar") { dialog, _ ->
                // Quando o botão "Adicionar" for clicado, obtém o nome do líquido e chama o controlador
                val liquidoNome = editText.text.toString()
                liquidosController.addLiquido(liquidoNome, {
                    Toast.makeText(this, "Líquido adicionado com sucesso!", Toast.LENGTH_SHORT).show()
                    loadLiquidos()  // Recarrega os líquidos após adicionar
                }, { error ->
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                })
                dialog.dismiss()  // Fecha o diálogo após adicionar
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()  // Fecha o diálogo se o botão "Cancelar" for clicado
            }
            .create()

        dialog.show()  // Exibe o diálogo
    }

    // Método para carregar a lista de líquidos
    private fun loadLiquidos() {
        liquidosController.loadLiquidos({ liquidos ->
            // Atualiza o adaptador com os líquidos carregados
            (recycler.adapter as? LiquidoAdapter)?.updateLiquidos(liquidos)
        }, { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        })
    }
}
