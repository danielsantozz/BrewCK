package com.example.brewck

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditarPerfilController(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun atualizarPerfil(campo: String, newDado: String, onComplete: (Boolean) -> Unit) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val email = currentUser.email
            if (email != null) {
                findUserIdByEmail(email, campo, newDado, onComplete)
            }
        } else {
            Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            onComplete(false)
        }
    }

    private fun findUserIdByEmail(email: String, campo: String, newDado: String, onComplete: (Boolean) -> Unit) {
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val userId = document.id
                        updatePerfil(userId, campo, newDado, onComplete)
                    }
                } else {
                    Toast.makeText(context, "Usuário não encontrado", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Erro ao buscar o usuário: ${exception.message}", Toast.LENGTH_LONG).show()
                onComplete(false)
            }
    }

    private fun updatePerfil(id: String, campo: String, data: String, onComplete: (Boolean) -> Unit) {
        val userDoc = firestore.collection("users").document(id)

        if (campo == "Nome") {
            val userData = hashMapOf<String, Any>(
                "nome" to data
            )

            userDoc.update(userData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Perfil atualizado com sucesso.", Toast.LENGTH_SHORT).show()
                    onComplete(true)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Erro ao atualizar perfil: ${exception.message}", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
        } else {
            alterarSenha(data, onComplete)
        }
    }

    private fun alterarSenha(novaSenha: String, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser

        user?.let {
            it.updatePassword(novaSenha)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Senha alterada com sucesso", Toast.LENGTH_SHORT).show()
                        onComplete(true)
                    } else {
                        task.exception?.let { exception ->
                            Toast.makeText(context, exception.message, Toast.LENGTH_LONG).show()
                        }
                        onComplete(false)
                    }
                }
        }
    }
}
