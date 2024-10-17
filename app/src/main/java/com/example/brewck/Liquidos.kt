package com.example.brewck

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.brewck.components.LiquidoAdapter
import com.example.brewck.models.Liquido
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Liquidos : AppCompatActivity() {

    private lateinit var btnVoltar: Button
    private lateinit var edtFiltro: EditText
    private lateinit var recycler: RecyclerView
    private lateinit var btnCadastrarLiquido: ImageView
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_liquidos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnVoltar = findViewById(R.id.btnVoltar)
        btnVoltar.setOnClickListener {
            finish()
        }

        edtFiltro = findViewById(R.id.edtFiltroLiquidos)
        recycler = findViewById(R.id.recyclerViewLiquido)
        recycler.layoutManager = LinearLayoutManager(this)
        val liquidosAdapter = LiquidoAdapter(mutableListOf()) { liquido ->
            deletarLiquidoFirestore(liquido)
        }
        recycler.adapter = liquidosAdapter

        btnCadastrarLiquido = findViewById(R.id.btnCadastrarLiquido)
        btnCadastrarLiquido.setOnClickListener {
            showAddLiquidoDialog()
        }
    }

    private fun showAddLiquidoDialog() {
        val editText = EditText(this)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Adicionar Líquido")
            .setMessage("Digite o nome do líquido:")
            .setView(editText)
            .setPositiveButton("Adicionar") { dialog, _ ->
                val liquidoNome = editText.text.toString()
                addLiquidoToFirestore(liquidoNome)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
        loadLiquidos()
    }

    private fun addLiquidoToFirestore(nome: String) {
        val email = auth.currentUser?.email
        if (email != null) {
            val liquidoData = hashMapOf(
                "email" to email,
                "nome" to nome
            )

            Log.d("Liquidos", "Tentando adicionar líquido: $liquidoData")

            firestore.collection("liquidos")
                .add(liquidoData)
                .addOnSuccessListener {
                    Log.d("Liquidos", "Líquido adicionado com sucesso!")
                    Toast.makeText(this, "Líquido adicionado com sucesso!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("Liquidos", "Erro ao adicionar líquido: ${e.message}", e)
                    Toast.makeText(this, "Erro ao adicionar líquido: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.w("Liquidos", "Usuário não logado. Não é possível adicionar líquido.")
            Toast.makeText(this, "Usuário não logado. Não é possível adicionar líquido.", Toast.LENGTH_SHORT).show()
        }
        loadLiquidos()
    }

    private fun loadLiquidos() {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        val email = auth.currentUser?.email
        if (email != null) {
            firestore.collection("liquidos")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    val liquidos = documents.map { doc ->
                        doc.toObject(Liquido::class.java)
                    }
                    (recycler.adapter as? LiquidoAdapter)?.updateLiquidos(liquidos)
                }
                .addOnFailureListener { e ->
                    Log.e("Liquidos", "Erro ao buscar líquidos: ${e.message}", e)
                    Toast.makeText(this, "Erro ao buscar líquidos.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Usuário não logado.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deletarLiquidoFirestore(liquido: Liquido) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("liquidos")
            .whereEqualTo("email", FirebaseAuth.getInstance().currentUser?.email)
            .whereEqualTo("nome", liquido.nome)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    firestore.collection("liquidos").document(document.id)
                        .delete()
                        .addOnSuccessListener {
                            Log.d("Liquidos", "Líquido removido com sucesso!")
                            (recycler.adapter as? LiquidoAdapter)?.removeLiquido(liquido)
                            Toast.makeText(this, "Líquido excluído!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Liquidos", "Erro ao remover líquido: ${e.message}", e)
                            Toast.makeText(this, "Erro ao excluir líquido.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Liquidos", "Erro ao buscar líquido para exclusão: ${e.message}", e)
                Toast.makeText(this, "Erro ao excluir líquido.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        loadLiquidos()
    }
}
