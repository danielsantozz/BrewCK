package com.example.brewck.controllers

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.common.InputImage
import com.google.common.util.concurrent.ListenableFuture
import androidx.camera.core.ImageProxy
import com.example.brewck.PostQRCode
import com.example.brewck.QRCode
import com.example.brewck.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.common.Barcode

class QRCodeController(private val activity: QRCode) {
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>  // Objeto para lidar com a inicialização da câmera
    var qrCodeProcessed = false  // Flag para garantir que o QR Code é processado uma única vez

    // Função para verificar e solicitar permissão para usar a câmera
    fun checkCameraPermission(requestPermissionLauncher: ActivityResultLauncher<String>) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()  // Se a permissão for concedida, inicia a câmera
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)  // Caso contrário, solicita a permissão
        }
    }

    // Função para iniciar a câmera
    fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(activity)  // Inicializa o provedor da câmera
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)  // Após a câmera estar pronta, vincula a pré-visualização
        }, ContextCompat.getMainExecutor(activity))  // Usando o executor principal para garantir que a ação aconteça no thread principal
    }

    // Função para configurar e vincular a pré-visualização da câmera e a análise de imagem
    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build()  // Cria uma instância da pré-visualização
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA  // Seleciona a câmera traseira
        val previewView = activity.findViewById<PreviewView>(R.id.previewView)  // Referência à view que exibe a câmera
        preview.setSurfaceProvider(previewView.surfaceProvider)  // Define o provedor de superfície para exibição da câmera

        // Configura a análise de imagem, que processará os quadros da câmera
        val imageAnalysis = ImageAnalysis.Builder().build().also {
            it.setAnalyzer(ContextCompat.getMainExecutor(activity), { imageProxy ->  // Configura o analisador para processar imagens
                processImageProxy(imageProxy)  // Chama a função que processa a imagem
            })
        }

        // Vincula a câmera à atividade, associando a pré-visualização e a análise de imagem
        cameraProvider.bindToLifecycle(activity, cameraSelector, preview, imageAnalysis)
    }

    @OptIn(ExperimentalGetImage::class)
    // Função para processar cada imagem da câmera em busca de um QR Code
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image  // Obtém a imagem da câmera
        if (mediaImage != null && !qrCodeProcessed) {  // Verifica se a imagem foi capturada e se o QR Code ainda não foi processado
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)  // Cria um objeto InputImage
            val scannerOptions = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)  // Configura o scanner para buscar apenas QR Codes
                .build()
            val scanner = BarcodeScanning.getClient(scannerOptions)  // Inicializa o scanner de QR Code

            // Processa a imagem com o scanner
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->  // Quando o QR Code é detectado
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue  // Extrai o valor do QR Code
                        Log.d("QRCode", "QR Code detectado: $rawValue")  // Exibe o valor no log
                        qrCodeProcessed = true  // Marca que o QR Code foi processado
                        verificarBarril(rawValue.toString()) { existe ->  // Verifica se o barril existe no Firestore
                            if (existe) {
                                val intent = Intent(activity, PostQRCode::class.java)  // Cria uma intent para abrir a tela de detalhes do barril
                                intent.putExtra("id", rawValue)  // Passa o ID do barril para a próxima tela
                                activity.startActivity(intent)  // Inicia a nova atividade
                                Log.d("PostQRCode", "Barril encontrado!")
                            } else {
                                Toast.makeText(activity, "Barril não encontrado", Toast.LENGTH_SHORT).show()  // Exibe uma mensagem de erro caso o barril não seja encontrado
                                Log.d("PostQRCode", "Barril não encontrado.")
                                activity.finish()  // Finaliza a atividade atual
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("QRCode", "Erro ao detectar QR Code", it)  // Registra qualquer erro ao processar o QR Code
                }
                .addOnCompleteListener {
                    imageProxy.close()  // Fecha o proxy de imagem para liberar recursos
                }
        } else {
            imageProxy.close()  // Fecha o proxy se não houver imagem ou se o QR Code já foi processado
        }
    }

    // Função para verificar se um barril existe no Firestore
    fun verificarBarril(barrilId: String, callback: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()  // Obtém uma instância do Firestore
        val docRef = firestore.collection("barris").document(barrilId)  // Referência ao documento do barril

        // Tenta recuperar o documento do barril
        docRef.get()
            .addOnSuccessListener { document ->  // Se a consulta for bem-sucedida
                if (document != null && document.exists()) {  // Verifica se o barril existe
                    callback(true)  // Retorna verdadeiro se o barril for encontrado
                } else {
                    callback(false)  // Retorna falso caso contrário
                }
            }
            .addOnFailureListener { exception ->  // Em caso de erro na consulta
                Log.w("PostQRCode", "Erro ao buscar barril: ", exception)
                Toast.makeText(activity, "Erro ao buscar barril", Toast.LENGTH_SHORT).show()  // Exibe uma mensagem de erro
                callback(false)  // Retorna falso em caso de falha
            }
    }
}
