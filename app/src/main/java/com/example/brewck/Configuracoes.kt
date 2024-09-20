package com.example.brewck

import FirebaseRepository
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class Configuracoes : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    lateinit var btnVoltar : Button
    lateinit var btnAlterarNome : Button
    lateinit var btnAlterarSenha : Button
    lateinit var btnExcluirConta : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_configuracoes)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        btnVoltar = findViewById(R.id.btnVoltarCfg)
        btnAlterarNome = findViewById(R.id.btnAlterarNome)
        btnAlterarSenha = findViewById(R.id.btnAlterarSenha)
        btnExcluirConta = findViewById(R.id.btnExcluirConta)

        btnVoltar.setOnClickListener { finish() }
        btnAlterarNome.setOnClickListener {
            val intent = Intent(this, EditarPerfil::class.java)
            intent.putExtra("Titulo", "Nome")
            intent.putExtra("Subtitulo", "Digite seu novo nome")
            startActivity(intent)
        }
        btnAlterarSenha.setOnClickListener {
            val intent = Intent(this, EditarPerfil::class.java)
            intent.putExtra("Titulo", "Senha")
            intent.putExtra("Subtitulo", "Digite sua nova senha")
            startActivity(intent)
        }
        btnExcluirConta.setOnClickListener { deletarConta(); deletarContaFirestore() }
    }

    private fun deletarConta() {
        val user: FirebaseUser? = auth.currentUser
        user?.let {
            it.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        deletarContaFirestore()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        task.exception?.let { exception ->
                            Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
        }
    }

    private fun deletarContaFirestore() {
        val firestore = FirebaseFirestore.getInstance()

        val repository = FirebaseRepository()


        repository.pegarEmail { email ->
            if (email != null) {
                deleteUserByEmail(email) { success ->
                    if (success) {
                        Toast.makeText(this, "Usuário excluído com sucesso", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Erro ao excluir usuário", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun deleteUserByEmail(email: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        val usersCollection = db.collection("users")

        usersCollection.whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentId = querySnapshot.documents[0].id
                    usersCollection.document(documentId)
                        .delete()
                        .addOnSuccessListener {
                            callback(true)
                        }
                        .addOnFailureListener {
                            callback(false)
                        }
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}