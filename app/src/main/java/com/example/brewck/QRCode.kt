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
    private lateinit var controller: QRCodeController

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                controller.startCamera()
            } else {
                Toast.makeText(this, "A permissão de câmera é necessária.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_qrcode)

        controller = QRCodeController(this)
        controller.checkCameraPermission(requestPermissionLauncher)

        btnVoltar = findViewById(R.id.btnVoltarQRCODE)
        btnVoltar.setOnClickListener { finish() }
    }
}
