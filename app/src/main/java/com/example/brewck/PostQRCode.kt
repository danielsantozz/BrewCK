package com.example.brewck

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class PostQRCode : AppCompatActivity() {

    private lateinit var txtNome: TextView
    private lateinit var txtCapacidade: TextView
    private lateinit var txtStatus: TextView
    private lateinit var imgBarril: ImageView
    private lateinit var btnVoltar: Button

    private lateinit var btnEncher: Button
    private lateinit var btnVender: Button
    private lateinit var btnPegar: Button
    private lateinit var btnLimpar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_post_qrcode)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        txtNome = findViewById(R.id.txtQRNomeBarril)
        txtCapacidade = findViewById(R.id.txtCapacidade)
        txtStatus = findViewById(R.id.txtStatus)
        imgBarril = findViewById(R.id.imgBarril)
        btnVoltar = findViewById(R.id.btnVoltar)
        btnVoltar.setOnClickListener {
            finish()
        }

        val extras = intent.extras
        val id = extras?.getString("id")

        btnEncher = findViewById(R.id.btnEncher)
        btnEncher.setOnClickListener {
            if (id != null) {
                atualizarStatus(id, "Cheio")
                buscarBarrilPorId(id)
            }
        }
        btnVender = findViewById(R.id.btnVender)
        btnVender.setOnClickListener {
            if (id != null) {
                atualizarStatus(id, "No Cliente")
                buscarBarrilPorId(id)
            }
        }
        btnPegar = findViewById(R.id.btnPegar)
        btnPegar.setOnClickListener {
            if (id != null) {
                atualizarStatus(id, "Sujo")
                buscarBarrilPorId(id)
            }
        }
        btnLimpar = findViewById(R.id.btnLimpar)
        btnLimpar.setOnClickListener {
            if (id != null) {
                atualizarStatus(id, "Limpo")
                buscarBarrilPorId(id)
            }
        }

        if (id != null) {
            buscarBarrilPorId(id)
        } else {
            Toast.makeText(this, "ID do barril não fornecido", Toast.LENGTH_SHORT).show()
        }
    }

    fun buscarBarrilPorId(barrilId: String) {
        val firestore = FirebaseFirestore.getInstance()

        val docRef = firestore.collection("barris").document(barrilId)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val nome = document.getString("nome") ?: "Barril não encontrado"
                    val capacidade = document.getLong("capacidade")?.toInt() ?: 0
                    val status = document.getString("status") ?: "Desconhecido"

                    txtNome.text = "${nome}"
                    txtCapacidade.text = "$capacidade L"
                    txtStatus.text = "$status"

                    when(status) {
                        "Cheio" -> imgBarril.setImageResource(R.drawable.beerkeg_black)
                        "No Cliente" -> imgBarril.setImageResource(R.drawable.beerkegnocliente)
                        "Sujo" -> imgBarril.setImageResource(R.drawable.beerkegsujo)
                        "Limpo" -> imgBarril.setImageResource(R.drawable.beerkeglimpo)
                    }
                } else {
                    Toast.makeText(this, "Barril não encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.w("PostQRCode", "Erro ao buscar barril: ", exception)
                Toast.makeText(this, "Erro ao buscar barril", Toast.LENGTH_SHORT).show()
            }
    }

    fun atualizarStatus(barrilId: String, status: String) {
        val firestore = FirebaseFirestore.getInstance()

        val docRef = firestore.collection("barris").document(barrilId)

        val data = hashMapOf<String, Any>(
            "status" to status,
        )

        docRef.update(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Barril atualizado com sucesso.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao atualizar barril: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}