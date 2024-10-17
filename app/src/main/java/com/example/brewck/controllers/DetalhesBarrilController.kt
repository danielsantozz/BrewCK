package com.example.brewck.controllers

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class DetalhesBarrilController(private val context: Context) {
    var isFavorito: Boolean = false

    fun toggleFavorito(barrilId: String) {
        isFavorito = !isFavorito
        atualizarFavoritoNoFirestore(barrilId, isFavorito)
    }

    fun checkFavorito(barrilId: String, onResult: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val docRef = firestore.collection("barris").document(barrilId)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Obtém o valor do campo "isFavorito"
                    val isFavoritoFromFirestore = document.getBoolean("isFavorite") ?: false
                    onResult(isFavoritoFromFirestore)
                } else {
                    onResult(false)
                }
            }
            .addOnFailureListener { e ->
                Log.e("DetalhesBarrilController", "Erro ao buscar documento: ${e.message}")
                onResult(false)
            }
    }



    private fun atualizarFavoritoNoFirestore(barrilId: String, isFavorito: Boolean) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("barris").document(barrilId)
            .update("isFavorite", isFavorito)
            .addOnSuccessListener {
                Log.d("DetalhesBarrilController", "Favorito atualizado com sucesso: $isFavorito")
            }
            .addOnFailureListener { e ->
                Log.e("DetalhesBarrilController", "Erro ao atualizar favorito: ${e.message}")
            }
    }

    fun gerarQRCode(barrilId: String): Bitmap? {
        return try {
            val barcodeEncoder = BarcodeEncoder()
            val bitMatrix = barcodeEncoder.encode(barrilId, BarcodeFormat.QR_CODE, 400, 400)
            barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    fun salvarImagemQRCode(bitmap: Bitmap) {
        val fileName = "QRCode_${System.currentTimeMillis()}.png"
        val resolver = context.contentResolver
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
            Toast.makeText(context, "Imagem salva com sucesso", Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(context, "Erro ao salvar a imagem", Toast.LENGTH_SHORT).show()
        }
    }

    fun salvarImagemQRCodeLegacy(bitmap: Bitmap) {
        val directory = File(Environment.getExternalStorageDirectory().toString() + "/QRCodeApp")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val fileName = "QRCode_${System.currentTimeMillis()}.png"
        val file = File(directory, fileName)

        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Toast.makeText(context, "Imagem salva com sucesso em ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao salvar a imagem", Toast.LENGTH_SHORT).show()
        }
    }
}
