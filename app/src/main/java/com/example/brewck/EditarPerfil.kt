package com.example.brewck

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EditarPerfil : AppCompatActivity() {
    private lateinit var txtTituloEditar: TextView
    private lateinit var txtNovidade: TextView
    private lateinit var edtNovidade: EditText
    private lateinit var btnVoltar: Button
    private lateinit var btnAplicar: Button
    private lateinit var perfilController: EditarPerfilController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_perfil)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtém os dados passados pela Intent
        val titulo = intent.getStringExtra("Titulo")
        val subtitulo = intent.getStringExtra("Subtitulo")

        // Inicializa os componentes da interface
        txtTituloEditar = findViewById(R.id.txtTituloEditar)
        txtNovidade = findViewById(R.id.txtNovidade)
        edtNovidade = findViewById(R.id.edtNovidade)
        btnVoltar = findViewById(R.id.btnVoltarEditarPerfil)
        btnAplicar = findViewById(R.id.btnAplicar)

        // Atualiza os textos dos elementos com os dados recebidos
        txtTituloEditar.text = "Editar $titulo"
        txtNovidade.text = subtitulo

        // Inicializa o controlador para a edição do perfil
        perfilController = EditarPerfilController(this)

        // Configura o botão de voltar para fechar a Activity atual e retornar à Activity anterior
        btnVoltar.setOnClickListener { finish() }

        // Configura o botão de aplicar para atualizar as informações no perfil
        btnAplicar.setOnClickListener {
            // Chama o método de atualização do perfil no controlador, passando o título e o novo valor da novidade
            perfilController.atualizarPerfil(titulo.toString(), edtNovidade.text.toString()) { sucesso ->
                if (sucesso) {
                    // Se a atualização for bem-sucedida, inicia a Activity MenuPrincipal
                    val intent = Intent(this, MenuPrincipal::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}
