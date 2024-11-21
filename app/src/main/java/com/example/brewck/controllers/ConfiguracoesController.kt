package com.example.brewck.controllers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class ConfiguracoesController {

    // Instâncias do FirebaseAuth (para autenticação) e FirebaseFirestore (para manipulação de dados no Firestore)
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Função para deletar a conta do usuário
    fun deletarConta(callback: (Boolean, String) -> Unit) {
        val user: FirebaseUser? = auth.currentUser // Obtém o usuário autenticado

        // Verifica se o usuário está autenticado
        user?.let {
            // Tenta deletar a conta do Firebase Authentication
            it.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Se a exclusão do usuário for bem-sucedida, tenta excluir dados do usuário no Firestore
                        deletarContaFirestore(user.email ?: "") { sucesso ->
                            if (sucesso) {
                                // Se a exclusão no Firestore for bem-sucedida
                                callback(true, "Conta excluída com sucesso")
                            } else {
                                // Se ocorrer um erro ao excluir dados no Firestore
                                callback(false, "Erro ao excluir conta no Firestore")
                            }
                        }
                    } else {
                        // Caso a exclusão do Firebase Authentication falhe, retorna o erro
                        task.exception?.let { exception ->
                            callback(false, exception.message ?: "Erro ao excluir conta")
                        }
                    }
                }
        } ?: callback(false, "Usuário não autenticado") // Se o usuário não estiver autenticado, retorna erro
    }

    // Função privada para deletar dados da conta no Firestore
    private fun deletarContaFirestore(email: String, callback: (Boolean) -> Unit) {
        val usersCollection = firestore.collection("users") // Coleção de usuários no Firestore

        // Realiza uma consulta para encontrar o usuário pelo email
        usersCollection.whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Se o usuário for encontrado, deleta o documento correspondente
                    val documentId = querySnapshot.documents[0].id
                    usersCollection.document(documentId)
                        .delete()
                        .addOnSuccessListener {
                            // Se a exclusão for bem-sucedida, retorna sucesso
                            callback(true)
                        }
                        .addOnFailureListener {
                            // Se a exclusão falhar, retorna falha
                            callback(false)
                        }
                } else {
                    // Se o usuário não for encontrado no Firestore
                    callback(false)
                }
            }
            .addOnFailureListener {
                // Em caso de erro na consulta, retorna falha
                callback(false)
            }
    }

    // Função para obter o nome do usuário
    fun getNome(callback: (String?) -> Unit) {
        val currentUser = auth.currentUser // Obtém o usuário autenticado
        if (currentUser != null) {
            val userId = currentUser.uid // Obtém o UID do usuário
            val userDocRef = firestore.collection("users").document(userId) // Referência para o documento do usuário no Firestore
            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Se o documento do usuário existir, retorna o nome armazenado
                        val nome = documentSnapshot.getString("nome")
                        callback(nome)
                    } else {
                        // Se o documento não existir, retorna null
                        callback(null)
                    }
                }
        } else {
            // Se o usuário não estiver autenticado, retorna null
            callback(null)
        }
    }

    // Função para obter o email do usuário
    fun getEmail(callback: (String?) -> Unit) {
        val currentUser = auth.currentUser // Obtém o usuário autenticado

        if (currentUser != null) {
            // Se o usuário estiver autenticado, retorna o email
            val email = currentUser.email
            callback(email)
        } else {
            // Se o usuário não estiver autenticado, retorna null
            callback(null)
        }
    }
}
