package com.example.brewck.controllers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class CadastroController {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun criarUsuario(email: String, senha: String, nome: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val userMap = hashMapOf(
                            "nome" to nome,
                            "email" to email
                        )
                        db.collection("users").document(userId).set(userMap)
                            .addOnSuccessListener {
                                callback(true, null)
                            }
                            .addOnFailureListener { e ->
                                callback(false, "Falha ao salvar informações adicionais: ${e.localizedMessage}")
                            }
                    } else {
                        callback(false, "Erro ao obter ID do usuário")
                    }
                } else {
                    task.exception?.let { exception ->
                        when (exception) {
                            is FirebaseAuthUserCollisionException -> callback(false, "Email já está em uso")
                            is FirebaseAuthWeakPasswordException -> callback(false, "A senha é muito fraca.")
                            is FirebaseAuthInvalidCredentialsException -> callback(false, "O formato do email é inválido.")
                            else -> callback(false, exception.localizedMessage)
                        }
                    } ?: callback(false, "Erro desconhecido")
                }
            }
    }
}
