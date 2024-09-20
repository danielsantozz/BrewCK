package com.example.brewck

import FirebaseRepository
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditarPerfil : AppCompatActivity() {
    lateinit var txtTituloEditar : TextView
    lateinit var txtNovidade : TextView
    lateinit var edtNovidade : EditText
    lateinit var btnVoltar : Button
    lateinit var btnAplicar : Button

    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_perfil)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val titulo = intent.getStringExtra("Titulo")
        val subtitulo = intent.getStringExtra("Subtitulo")

        txtTituloEditar = findViewById(R.id.txtTituloEditar)
        txtNovidade = findViewById(R.id.txtNovidade)
        edtNovidade = findViewById(R.id.edtNovidade)
        btnVoltar = findViewById(R.id.btnVoltarEditarPerfil)
        btnAplicar = findViewById(R.id.btnAplicar)

        txtTituloEditar.setText("Editar " + titulo)
        txtNovidade.setText(subtitulo)

        btnVoltar.setOnClickListener { finish() }
        btnAplicar.setOnClickListener { atualizarPerfil(titulo.toString(), edtNovidade.text.toString()) }

    }

    private fun atualizarPerfil(campo: String, newDado: String) {
        val repository = FirebaseRepository()

        repository.pegarEmail { email ->
            if (email != null) {
                findUserIdByEmail(email, campo, newDado)

            } else {
                Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun findUserIdByEmail(email: String, campo: String, newDado: String) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val userId = document.id
                        updatePerfil(userId, campo, newDado) { callback ->
                            if (callback) {
                                Toast.makeText(this, "Nome de usuário alterado com sucesso!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MenuPrincipal::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, "Usuário não encontrado ou dados não disponíveis.", Toast.LENGTH_SHORT).show()
                            }
                        }

                    }
                } else {
                    Toast.makeText(this, "Usuário não encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao buscar o usuário: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }


    fun updatePerfil(
        id: String,
        campo: String,
        data: String,
        callback: (Boolean) -> Unit
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val users = firestore.collection("users").document(id)

        if (campo == "Nome") {
            val userData = hashMapOf<String, Any>(
                "nome" to data
            )
            users.update(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Perfil atualizado com sucesso.", Toast.LENGTH_SHORT).show()
                    println("Perfil atualizado com sucesso.")
                    callback(true)
                }
                .addOnFailureListener { exception ->
                    println("Erro ao atualizar perfil: ${exception.message}")
                    callback(false)
                }
        } else {
            alterarSenha(data)
        }
    }

    fun alterarSenha(novaSenha: String) {
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        user?.let {
            it.updatePassword(novaSenha)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Senha alterada com sucesso", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        task.exception?.let { exception ->
                            Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
        }
    }
}