package com.example.brewck

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class DetalhesCliente : AppCompatActivity() {

    private lateinit var txtNomeCliente: TextView
    private lateinit var txtCPFCliente: TextView
    private lateinit var txtBarrilCliente: TextView
    private lateinit var txtEnderecoCliente: TextView
    private lateinit var imgCliente: ImageView
    private lateinit var btnEditar: Button
    private lateinit var btnVoltar: Button
    private lateinit var btnAvaliarBom: ImageButton
    private lateinit var btnAvaliarRuim: ImageButton

    
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalhes_cliente)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val id = intent.getStringExtra("id")
        val nome = intent.getStringExtra("nome")
        val cpf = intent.getStringExtra("cpf")
        val barril = intent.getStringExtra("barril")
        val endereco = intent.getStringExtra("endereco")
        val avaliacao = intent.getStringExtra("avaliacao")

        txtNomeCliente = findViewById(R.id.txtDetNomeCliente)
        txtCPFCliente = findViewById(R.id.txtDetCPF)
        txtBarrilCliente = findViewById(R.id.txtDetBarril)
        txtEnderecoCliente = findViewById(R.id.txtDetEndereco)
        btnEditar = findViewById(R.id.btnIntentEditar)
        imgCliente = findViewById(R.id.imgCliente)
        btnAvaliarBom = findViewById(R.id.btnAvaliarBom)
        btnAvaliarRuim = findViewById(R.id.btnAvaliarRuim)
        btnVoltar = findViewById(R.id.btnVoltar)

        btnVoltar.setOnClickListener {
            finish()
        }

        txtNomeCliente.text = nome
        txtCPFCliente.text = cpf
        txtBarrilCliente.text = barril
        txtEnderecoCliente.text = endereco

        
        when (avaliacao) {
            "Bom" -> imgCliente.setImageResource(R.drawable.usergreen)
            "Ruim" -> imgCliente.setImageResource(R.drawable.userred)
            else -> imgCliente.setImageResource(R.drawable.user) 
        }

        btnEditar.setOnClickListener {
            val intent = Intent(this, EditarCliente::class.java)
            intent.putExtra("id", id)
            intent.putExtra("nome", nome)
            intent.putExtra("cpf", cpf)
            intent.putExtra("barril", barril)
            intent.putExtra("endereco", endereco)
            intent.putExtra("avaliacao", avaliacao)
            startActivity(intent)
        }

        btnAvaliarBom.setOnClickListener {
            if (id != null) {
                avaliarCliente("Bom", id)
            }
        }

        btnAvaliarRuim.setOnClickListener {
            if (id != null) {
                avaliarCliente("Ruim", id)
            }
        }

        
        imgCliente.setOnClickListener {
            if (id != null) {
                resetarAvaliacao(id)
            }
        }
    }

    private fun avaliarCliente(avaliacao: String, clienteId: String) {
        
        val clienteRef = firestore.collection("clientes").document(clienteId)

        clienteRef.update("avaliacao", avaliacao)
            .addOnSuccessListener {
                when (avaliacao) {
                    "Bom" -> imgCliente.setImageResource(R.drawable.usergreen)
                    "Ruim" -> imgCliente.setImageResource(R.drawable.userred)
                }
                Toast.makeText(this, "Cliente avaliado como $avaliacao", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Falha ao atualizar avaliação: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun resetarAvaliacao(clienteId: String) {
        
        val clienteRef = firestore.collection("clientes").document(clienteId)

        clienteRef.update("avaliacao", "")
            .addOnSuccessListener {
                imgCliente.setImageResource(R.drawable.user) 
                Toast.makeText(this, "Avaliação resetada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Falha ao resetar avaliação: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
