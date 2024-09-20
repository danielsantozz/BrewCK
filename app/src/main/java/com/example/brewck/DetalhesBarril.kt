package com.example.brewck

import android.content.Intent
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
            imgBarril.setImageResource(imgFav)
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
}
