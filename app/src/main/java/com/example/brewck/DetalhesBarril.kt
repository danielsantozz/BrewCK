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

        controller = DetalhesBarrilController(this)
        initViews()

        barrilId = intent.getStringExtra("id") ?: ""
        val nome = intent.getStringExtra("nome")
        val capacidade = intent.getIntExtra("capacidade", 0)
        val proprietario = intent.getStringExtra("propriedade")
        val status = intent.getStringExtra("status")
        val liquido = intent.getStringExtra("liquido")
        val cliente = intent.getStringExtra("cliente")

        controller.checkFavorito(barrilId) { favorito ->
            atualizarUI(nome, capacidade, proprietario, status, liquido, favorito, cliente)
        }

        val qrCodeBitmap = controller.gerarQRCode(barrilId)
        if (qrCodeBitmap != null) {
            imgQR.setImageBitmap(qrCodeBitmap)
            imgQR.setOnClickListener { exibirImagemAmpliada(qrCodeBitmap) }
        }

        setupButtonListeners(nome, capacidade, proprietario, status, liquido, cliente)
    }


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
    }

    private fun atualizarUI(nome: String?, capacidade: Int, proprietario: String?, status: String?, liquido: String?, favorito: Boolean, cliente: String?) {
        txtNomeBarril.text = nome
        txtCapacidadeBarril.text = capacidade.toString()
        txtProprietarioBarril.text = proprietario
        if (status == "No Cliente" && cliente != "") {
            txtStatusBarril.text = "${status} (${cliente})"
        } else {
            txtStatusBarril.text = status
        }
        txtLiquidoBarril.text = liquido
        btnFavorito.setImageResource(if (favorito) R.drawable.favorite else R.drawable.unfavorite)
    }

    private fun setupButtonListeners(nome: String?, capacidade: Int, proprietario: String?, status: String?, liquido: String?, cliente: String?) {
        btnFavorito.setOnClickListener {
            controller.toggleFavorito(barrilId)
            controller.checkFavorito(barrilId) { favorito ->
                atualizarUI(nome, capacidade, proprietario, status, liquido, favorito, cliente)
            }
        }

        btnVoltar.setOnClickListener { finish() }

        btnEditar.setOnClickListener {
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


    private fun exibirImagemAmpliada(qrCodeBitmap: Bitmap?) {
        val dialog = Dialog(this).apply {
            setContentView(R.layout.dialog_qrcode_ampliado)
        }

        val imageViewAmpliada = dialog.findViewById<ImageView>(R.id.qrCodeAmpliadoImageView)
        val salvarButton = dialog.findViewById<Button>(R.id.salvarButton)
        imageViewAmpliada.setImageBitmap(qrCodeBitmap)

        salvarButton.setOnClickListener {
            qrCodeBitmap?.let { bitmap ->
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                    } else {
                        controller.salvarImagemQRCodeLegacy(bitmap)
                    }
                } else {
                    controller.salvarImagemQRCode(bitmap)
                }
            }
        }

        dialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            Toast.makeText(this, if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) "Permissão concedida. Clique novamente para salvar o QR Code." else "Permissão negada. Não é possível salvar a imagem.", Toast.LENGTH_SHORT).show()
        }
    }
}
