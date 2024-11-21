package com.example.brewck

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.brewck.controllers.QRCodeController

class QRCode : AppCompatActivity() {
    private lateinit var btnVoltar: Button

    // Declaração de uma variável para o controlador de QRCode
    private lateinit var controller: QRCodeController

    // Launcher para solicitar permissão de uso da câmera
    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            // Se a permissão for concedida, inicia a câmera
            if (isGranted) {
                controller.startCamera()
            } else {
                // Caso a permissão seja negada, exibe um Toast informando ao usuário
                Toast.makeText(this, "A permissão de câmera é necessária.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_qrcode)

        // Inicializa o controlador de QRCode
        controller = QRCodeController(this)
        // Verifica a permissão da câmera ao iniciar a activity
        controller.checkCameraPermission(requestPermissionLauncher)

        // Inicializa o botão Voltar e configura seu comportamento para finalizar a activity
        btnVoltar = findViewById(R.id.btnVoltarQRCODE)
        btnVoltar.setOnClickListener { finish() }
    }
}
