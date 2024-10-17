package com.example.brewck.controllers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class ConfiguracoesController {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun deletarConta(callback: (Boolean, String) -> Unit) {
        val user: FirebaseUser? = auth.currentUser

        user?.let {
            it.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        deletarContaFirestore(user.email ?: "") { sucesso ->
                            if (sucesso) {
                                callback(true, "Conta excluída com sucesso")
                            } else {
                                callback(false, "Erro ao excluir conta no Firestore")
                            }
                        }
                    } else {
                        task.exception?.let { exception ->
                            callback(false, exception.message ?: "Erro ao excluir conta")
                        }
                    }
                }
        } ?: callback(false, "Usuário não autenticado")
    }

    private fun deletarContaFirestore(email: String, callback: (Boolean) -> Unit) {
        val usersCollection = firestore.collection("users")

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
