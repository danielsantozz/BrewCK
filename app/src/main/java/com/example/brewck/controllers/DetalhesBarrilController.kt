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
    // Variável para armazenar o estado de "favorito" do barril
    var isFavorito: Boolean = false

    // Função para alternar o estado de "favorito" do barril
    fun toggleFavorito(barrilId: String) {
        isFavorito = !isFavorito // Alterna o estado de "favorito"
        atualizarFavoritoNoFirestore(barrilId, isFavorito) // Atualiza no Firestore
    }

    // Função para verificar se o barril é favorito no Firestore
    fun checkFavorito(barrilId: String, onResult: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val docRef = firestore.collection("barris").document(barrilId)

        // Busca o documento do barril no Firestore
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Obtém o valor do campo "isFavorite" no Firestore
                    val isFavoritoFromFirestore = document.getBoolean("isFavorite") ?: false
                    onResult(isFavoritoFromFirestore) // Retorna o resultado através do callback
                } else {
                    onResult(false) // Caso não encontre o documento
                }
            }
            .addOnFailureListener { e ->
                // Log de erro e retorno falso no callback caso ocorra falha
                Log.e("DetalhesBarrilController", "Erro ao buscar documento: ${e.message}")
                onResult(false)
            }
    }

    // Função para atualizar o status de favorito do barril no Firestore
    private fun atualizarFavoritoNoFirestore(barrilId: String, isFavorito: Boolean) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("barris").document(barrilId)
            .update("isFavorite", isFavorito) // Atualiza o campo "isFavorite"
            .addOnSuccessListener {
                Log.d("DetalhesBarrilController", "Favorito atualizado com sucesso: $isFavorito")
            }
            .addOnFailureListener { e ->
                Log.e("DetalhesBarrilController", "Erro ao atualizar favorito: ${e.message}")
            }
    }

    // Função para gerar o QR Code a partir do ID do barril
    fun gerarQRCode(barrilId: String): Bitmap? {
        return try {
            val barcodeEncoder = BarcodeEncoder()
            // Gera o QR code com a codificação do barrilId
            val bitMatrix = barcodeEncoder.encode(barrilId, BarcodeFormat.QR_CODE, 400, 400)
            barcodeEncoder.createBitmap(bitMatrix) // Converte para bitmap
        } catch (e: WriterException) {
            e.printStackTrace() // Trata a exceção caso falhe ao gerar o QR Code
            null // Retorna null caso ocorra erro
        }
    }

    // Função para salvar a imagem do QR Code no armazenamento externo (Android 10+)
    fun salvarImagemQRCode(bitmap: Bitmap) {
        val fileName = "QRCode_${System.currentTimeMillis()}.png" // Nome do arquivo com timestamp
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            // Configurações para o arquivo de imagem
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QRCodeApp") // Caminho do diretório
        }

        // Insere o arquivo no MediaStore, que é usado para armazenar imagens no armazenamento externo
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let { uri ->
            val outputStream: OutputStream? = resolver.openOutputStream(uri)
            outputStream.use { out ->
                if (out != null) {
                    // Comprime o bitmap e salva a imagem no stream
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
            // Exibe mensagem de sucesso
            Toast.makeText(context, "Imagem salva com sucesso", Toast.LENGTH_SHORT).show()
        } ?: run {
            // Exibe mensagem de erro se não conseguir salvar
            Toast.makeText(context, "Erro ao salvar a imagem", Toast.LENGTH_SHORT).show()
        }
    }

    // Função para salvar a imagem do QR Code no armazenamento externo (Android 9-)
    fun salvarImagemQRCodeLegacy(bitmap: Bitmap) {
        val directory = File(Environment.getExternalStorageDirectory().toString() + "/QRCodeApp")
        if (!directory.exists()) {
            directory.mkdirs() // Cria o diretório se não existir
        }

        val fileName = "QRCode_${System.currentTimeMillis()}.png"
        val file = File(directory, fileName)

        try {
            val outputStream = FileOutputStream(file) // Cria um FileOutputStream para salvar o arquivo
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush() // Garante que os dados sejam gravados
            outputStream.close() // Fecha o stream após salvar

            // Exibe mensagem de sucesso com o caminho do arquivo
            Toast.makeText(context, "Imagem salva com sucesso em ${file.absolutePath}", Toast.LENGTH_SHORT).show()

        } catch (e: IOException) {
            e.printStackTrace() // Trata a exceção caso falhe ao salvar a imagem

            // Exibe mensagem de erro
            Toast.makeText(context, "Erro ao salvar a imagem", Toast.LENGTH_SHORT).show()
        }
    }
}
