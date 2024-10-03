package com.example.brewck

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import android.graphics.Bitmap
import android.os.Build
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import android.Manifest

class DetalhesBarril : AppCompatActivity() {
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

    private var isFavorito: Boolean = false
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

        
        barrilId = intent.getStringExtra("id") ?: ""
        val nome = intent.getStringExtra("nome")
        val capacidade = intent.getIntExtra("capacidade", 0)
        val proprietario = intent.getStringExtra("propriedade")
        val status = intent.getStringExtra("status")
        val liquido = intent.getStringExtra("liquido")
        isFavorito = intent.getBooleanExtra("favorito", false)

        
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

        
        txtNomeBarril.text = nome
        txtCapacidadeBarril.text = capacidade.toString()
        txtProprietarioBarril.text = proprietario
        txtStatusBarril.text = status
        txtLiquidoBarril.text = liquido

        
        atualizarImagemFavorito()

        btnFavorito.setOnClickListener {
            isFavorito = !isFavorito
            atualizarImagemFavorito()
        }

        btnVoltar.setOnClickListener {
            finish()
        }

        btnEditar.setOnClickListener {
            val intent = Intent(this, EditarBarril::class.java)
            intent.putExtra("id", barrilId)
            intent.putExtra("nome", nome)
            intent.putExtra("capacidade", capacidade)
            intent.putExtra("proprietario", proprietario)
            intent.putExtra("status", status)
            intent.putExtra("liquido", liquido)
            startActivity(intent)
        }

        val imgFav = if (isFavorito) {
            R.drawable.favorite 
        } else {
            R.drawable.unfavorite 
        }

        if (isFavorito) {
            btnFavorito.setImageResource(imgFav)
        }

        val qrCodeBitmap = gerarQRCode(barrilId)
        if (qrCodeBitmap != null) {
            imgQR.setImageBitmap(qrCodeBitmap)
        }

        imgQR.setOnClickListener {
            exibirImagemAmpliada(qrCodeBitmap)
        }

    }

    private fun atualizarImagemFavorito() {
        val drawableResId = if (isFavorito) {
            R.drawable.favorite 
        } else {
            R.drawable.unfavorite 
        }
        btnFavorito.setImageResource(drawableResId)
        atualizarFavoritoNoFirestore(isFavorito)
    }

    private fun atualizarFavoritoNoFirestore(isFavorito: Boolean) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("barris").document(barrilId)
            .update("isFavorite", isFavorito)
            .addOnSuccessListener {
                Log.d("DetalhesBarril", "Favorito atualizado com sucesso: $isFavorito")
            }
            .addOnFailureListener { e ->
                Log.e("DetalhesBarril", "Erro ao atualizar favorito: ${e.message}")
            }
    }

    fun gerarQRCode(barrilId: String): Bitmap? {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitMatrix: BitMatrix = barcodeEncoder.encode(barrilId, BarcodeFormat.QR_CODE, 400, 400)
            return barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: WriterException) {
            e.printStackTrace()
            return null
        }
    }

    private fun exibirImagemAmpliada(qrCodeBitmap: Bitmap?) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_qrcode_ampliado)

        val imageViewAmpliada = dialog.findViewById<ImageView>(R.id.qrCodeAmpliadoImageView)
        val salvarButton = dialog.findViewById<Button>(R.id.salvarButton)

        imageViewAmpliada.setImageBitmap(qrCodeBitmap)

        salvarButton.setOnClickListener {
            if (qrCodeBitmap != null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                    } else {
                        salvarImagemQRCodeLegacy(qrCodeBitmap)
                    }
                } else {
                    salvarImagemQRCode(qrCodeBitmap)
                }
            }
        }

        dialog.show()
    }

    fun salvarImagemQRCode(bitmap: Bitmap) {
        val fileName = "QRCode_${txtNomeBarril.text}.png"
        val resolver = contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QRCodeApp")
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let { uri ->
            val outputStream: OutputStream? = resolver.openOutputStream(uri)
            outputStream.use { out ->
                if (out != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
            Toast.makeText(this, "Imagem salva com sucesso", Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(this, "Erro ao salvar a imagem", Toast.LENGTH_SHORT).show()
        }
    }

    fun salvarImagemQRCodeLegacy(bitmap: Bitmap) {
        val directory = File(Environment.getExternalStorageDirectory().toString() + "/QRCodeApp")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val fileName = "QRCode_${txtNomeBarril.text}.png"
        val file = File(directory, fileName)

        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Toast.makeText(this, "Imagem salva com sucesso em ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao salvar a imagem", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão concedida. Clique novamente para salvar o QR Code.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissão negada. Não é possível salvar a imagem.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
