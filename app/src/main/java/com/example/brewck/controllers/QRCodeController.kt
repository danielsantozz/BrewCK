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
import com.example.brewck.models.BarrilSimple
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.common.Barcode

class QRCodeController(private val activity: QRCode) {
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    var qrCodeProcessed = false

    fun checkCameraPermission(requestPermissionLauncher: ActivityResultLauncher<String>) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(activity))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val previewView = activity.findViewById<PreviewView>(R.id.previewView)
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val imageAnalysis = ImageAnalysis.Builder().build().also {
            it.setAnalyzer(ContextCompat.getMainExecutor(activity), { imageProxy ->
                processImageProxy(imageProxy)
            })
        }

        cameraProvider.bindToLifecycle(activity, cameraSelector, preview, imageAnalysis)
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null && !qrCodeProcessed) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scannerOptions = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
            val scanner = BarcodeScanning.getClient(scannerOptions)

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        Log.d("QRCode", "QR Code detectado: $rawValue")
                        qrCodeProcessed = true
                        verificarBarril(rawValue.toString()) { existe ->
                            if (existe) {
                                val intent = Intent(activity, PostQRCode::class.java)
                                intent.putExtra("id", rawValue)
                                activity.startActivity(intent)
                                Log.d("PostQRCode", "Barril encontrado!")
                            } else {
                                Toast.makeText(activity, "Barril não encontrado", Toast.LENGTH_SHORT).show()
                                Log.d("PostQRCode", "Barril não encontrado.")
                                activity.finish()
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("QRCode", "Erro ao detectar QR Code", it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    fun verificarBarril(barrilId: String, callback: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val docRef = firestore.collection("barris").document(barrilId)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    callback(true)
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("PostQRCode", "Erro ao buscar barril: ", exception)
                Toast.makeText(activity, "Erro ao buscar barril", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }

}
