package com.example.brewck

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.brewck.controllers.DetalhesBarrilController

class DetalhesBarril : AppCompatActivity() {

    // Instância do controlador para manipulação dos dados do barril
    private lateinit var controller: DetalhesBarrilController

    private lateinit var txtNomeBarril: TextView
    private lateinit var txtCapacidadeBarril: TextView
    private lateinit var txtProprietarioBarril: TextView
    private lateinit var txtStatusBarril: TextView
    private lateinit var txtLiquidoBarril: TextView
    private lateinit var imgBarril: ImageView
    private lateinit var btnEditar: Button
    private lateinit var btnVoltar: Button
    private lateinit var btnFavorito: ImageButton
    private lateinit var imgQR: ImageView
    private lateinit var imgEditar: ImageView

    // Variável que armazena o ID do barril para utilizá-lo entre as atividades
    private lateinit var barrilId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalhes_barril)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa o controlador responsável por gerenciar os dados do barril
        controller = DetalhesBarrilController(this)

        // Inicializa os componentes da interface
        initViews()

        // Obtém os dados do barril passados pela Intent
        barrilId = intent.getStringExtra("id") ?: ""  // ID do barril
        val nome = intent.getStringExtra("nome")
        val capacidade = intent.getIntExtra("capacidade", 0)
        val proprietario = intent.getStringExtra("propriedade")
        val status = intent.getStringExtra("status")
        val liquido = intent.getStringExtra("liquido")
        val cliente = intent.getStringExtra("cliente")

        // Verifica se o barril está marcado como favorito e atualiza a interface com essas informações
        controller.checkFavorito(barrilId) { favorito ->
            atualizarUI(nome, capacidade, proprietario, status, liquido, favorito, cliente)
        }

        // Gera o QR Code para o barril e exibe na interface
        val qrCodeBitmap = controller.gerarQRCode(barrilId)
        if (qrCodeBitmap != null) {
            imgQR.setImageBitmap(qrCodeBitmap)  // Exibe o QR code na imagem
            // Configura o comportamento de ampliar a imagem quando clicada
            imgQR.setOnClickListener { exibirImagemAmpliada(qrCodeBitmap) }
        }

        // Configura os listeners dos botões para realizar as ações de edição, favoritar e voltar
        setupButtonListeners(nome, capacidade, proprietario, status, liquido, cliente)
    }

    // Método que inicializa os componentes da interface
    private fun initViews() {
        txtNomeBarril = findViewById(R.id.txtDetNomeBarril)
        txtCapacidadeBarril = findViewById(R.id.txtDetCapacidade)
        txtProprietarioBarril = findViewById(R.id.txtDetProprietario)
        txtStatusBarril = findViewById(R.id.txtDetStatus)
        txtLiquidoBarril = findViewById(R.id.txtDetLiquido)
        btnEditar = findViewById(R.id.btnIntentEditar)
        imgBarril = findViewById(R.id.imgBarril)
        btnFavorito = findViewById(R.id.btnFavorito)
        btnVoltar = findViewById(R.id.btnVoltar)
        imgQR = findViewById(R.id.imgQR)
        imgEditar = findViewById(R.id.imgEditar)
    }

    // Atualiza a interface com as informações do barril
    private fun atualizarUI(
        nome: String?, capacidade: Int, proprietario: String?, status: String?, liquido: String?,
        favorito: Boolean, cliente: String?
    ) {
        // Atualiza os textos com as informações do barril
        txtNomeBarril.text = nome
        txtCapacidadeBarril.text = capacidade.toString()
        txtProprietarioBarril.text = proprietario
        // Se o status for "No Cliente", mostra o nome do cliente
        if (status == "No Cliente" && cliente != "") {
            txtStatusBarril.text = "${status} (${cliente})"
        } else {
            txtStatusBarril.text = status
        }
        txtLiquidoBarril.text = liquido
        // Define o ícone de favorito de acordo com o status
        btnFavorito.setImageResource(if (favorito) R.drawable.favorite else R.drawable.unfavorite)
    }

    // Método que configura os listeners de clique para os botões
    private fun setupButtonListeners(
        nome: String?, capacidade: Int, proprietario: String?, status: String?, liquido: String?, cliente: String?
    ) {
        // Listener para o botão de favoritar
        btnFavorito.setOnClickListener {
            controller.toggleFavorito(barrilId)  // Alterna o estado de favorito
            // Atualiza o estado de favorito após a alteração
            controller.checkFavorito(barrilId) { favorito ->
                atualizarUI(nome, capacidade, proprietario, status, liquido, favorito, cliente)
            }
        }

        // Listener para o botão de edição (abre a tela de ações do barril)
        btnEditar.setOnClickListener {
            val intent = Intent(this, PostQRCode::class.java)
            intent.putExtra("id", barrilId)
            intent.putExtra("nome", nome)
            intent.putExtra("status", status)
            startActivity(intent)
        }

        // Listener para o botão de voltar (finaliza a activity atual)
        btnVoltar.setOnClickListener { finish() }

        // Listener para a imagem de edição (abre a tela para editar o barril)
        imgEditar.setOnClickListener {
            val intent = Intent(this, EditarBarril::class.java).apply {
                putExtra("id", barrilId)
                putExtra("nome", nome)
                putExtra("capacidade", capacidade)
                putExtra("proprietario", proprietario)
                putExtra("status", status)
                putExtra("liquido", liquido)
            }
            startActivity(intent)
        }
    }

    // Exibe uma versão ampliada do QR Code em um diálogo
    private fun exibirImagemAmpliada(qrCodeBitmap: Bitmap?) {
        // Cria o diálogo para mostrar o QR Code ampliado
        val dialog = Dialog(this).apply {
            setContentView(R.layout.dialog_qrcode_ampliado)
        }

        val imageViewAmpliada = dialog.findViewById<ImageView>(R.id.qrCodeAmpliadoImageView)
        val salvarButton = dialog.findViewById<Button>(R.id.salvarButton)
        imageViewAmpliada.setImageBitmap(qrCodeBitmap)  // Exibe o QR Code ampliado

        // Configura o listener para o botão de salvar a imagem
        salvarButton.setOnClickListener {
            qrCodeBitmap?.let { bitmap ->
                // Verifica a versão do Android e trata a permissão de salvar a imagem
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                    } else {
                        controller.salvarImagemQRCodeLegacy(bitmap)  // Salva a imagem no armazenamento
                    }
                } else {
                    controller.salvarImagemQRCode(bitmap)  // Salva a imagem em dispositivos com Android Q ou superior
                }
            }
        }

        dialog.show()  // Exibe o diálogo
    }

    // Método que lida com o resultado da solicitação de permissões
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Verifica se a permissão foi concedida
        if (requestCode == 1) {
            Toast.makeText(this, if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) "Permissão concedida. Clique novamente para salvar o QR Code." else "Permissão negada. Não é possível salvar a imagem.", Toast.LENGTH_SHORT).show()
        }
    }
}
