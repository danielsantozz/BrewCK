package com.example.brewck

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditarPerfilController(private val context: Context) {
    // Instâncias do Firebase Firestore e Firebase Auth para interação com o banco de dados e autenticação.
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Função para atualizar o perfil do usuário. Recebe o campo (ex: nome ou senha) e o novo valor.
    fun atualizarPerfil(campo: String, newDado: String, onComplete: (Boolean) -> Unit) {

        // Obtém o usuário autenticado.
        val currentUser = auth.currentUser

        // Verifica se o usuário está autenticado.
        if (currentUser != null) {
            // Obtém o e-mail do usuário autenticado.
            val email = currentUser.email
            if (email != null) {
                // Chama a função para buscar o ID do usuário usando o e-mail.
                findUserIdByEmail(email, campo, newDado, onComplete)
            }
        } else {
            // Se o usuário não estiver autenticado, exibe uma mensagem de erro.
            Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            // Retorna false via callback indicando falha na operação.
            onComplete(false)
        }
    }

    // Função que busca o ID do usuário no Firestore usando o e-mail.
    private fun findUserIdByEmail(email: String, campo: String, newDado: String, onComplete: (Boolean) -> Unit) {
        // Consulta a coleção "users" no Firestore para encontrar o usuário com o e-mail fornecido.
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->

                // Verifica se documentos foram encontrados com o e-mail informado.
                if (!documents.isEmpty) {

                    // Para cada documento encontrado, pega o ID do usuário e chama a função para atualizar o perfil.
                    for (document in documents) {
                        val userId = document.id

                        // Atualiza o perfil do usuário com o ID encontrado.
                        updatePerfil(userId, campo, newDado, onComplete)
                    }
                } else {
                    // Se não encontrar o usuário, exibe uma mensagem e retorna false.
                    Toast.makeText(context, "Usuário não encontrado", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
            }
            .addOnFailureListener { exception ->
                // Se ocorrer um erro na busca, exibe a mensagem de erro e retorna false.
                Toast.makeText(context, "Erro ao buscar o usuário: ${exception.message}", Toast.LENGTH_LONG).show()
                onComplete(false)
            }
    }

    // Função para atualizar o perfil do usuário (nome ou senha) no Firestore.
    private fun updatePerfil(id: String, campo: String, data: String, onComplete: (Boolean) -> Unit) {

        // Verifica se o campo a ser atualizado é "Nome"
        if (campo == "Nome") {

            // Verifica se o nome tem menos de 3 caracteres
            if (data.length < 3) {
                Toast.makeText(context, "O nome não pode ter menos de 3 caracteres.", Toast.LENGTH_SHORT).show()
                onComplete(false)
                return
            }

            // Verifica se o nome tem mais de 50 caracteres
            if (data.length > 50) {
                Toast.makeText(context, "O nome não pode ter mais de 50 caracteres.", Toast.LENGTH_SHORT).show()
                onComplete(false)
                return
            }

            // Referência ao documento do usuário no Firestore.
            val userDoc = firestore.collection("users").document(id)

            // Atualiza o campo "nome" no documento do usuário.
            val userData = hashMapOf<String, Any>(
                "nome" to data
            )

            // Tenta atualizar o campo "nome"
            userDoc.update(userData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Perfil atualizado com sucesso.", Toast.LENGTH_SHORT).show()
                    onComplete(true)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Erro ao atualizar perfil: ${exception.message}", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }

        } else if (campo == "Senha") {

            // Verifica se a senha tem menos de 6 caracteres
            if (data.length < 6) {
                Toast.makeText(context, "A senha deve ter pelo menos 6 caracteres.", Toast.LENGTH_SHORT).show()
                onComplete(false)
                return
            }

            // Verifica se a senha tem mais de 20 caracteres
            if (data.length > 20) {
                Toast.makeText(context, "A senha não pode ter mais de 20 caracteres.", Toast.LENGTH_SHORT).show()
                onComplete(false)
                return
            }

            // Chama a função para alterar a senha.
            alterarSenha(data, onComplete)
        }
    }


    // Função para alterar a senha do usuário.
    private fun alterarSenha(novaSenha: String, onComplete: (Boolean) -> Unit) {

        // Obtém o usuário autenticado.
        val user = auth.currentUser
        // Se o usuário estiver autenticado, tenta atualizar a senha.
        user?.let {
            // Chama o método para atualizar a senha.
            it.updatePassword(novaSenha)
                .addOnCompleteListener { task ->

                    // Verifica se a operação foi bem-sucedida.
                    if (task.isSuccessful) {

                        // Se a senha for alterada com sucesso, exibe uma mensagem e retorna true.
                        Toast.makeText(context, "Senha alterada com sucesso", Toast.LENGTH_SHORT).show()
                        onComplete(true)
                    } else {

                        // Se ocorrer um erro, exibe a mensagem de erro.
                        task.exception?.let { exception ->
                            Toast.makeText(context, exception.message, Toast.LENGTH_LONG).show()
                        }
                        onComplete(false)
                    }
                }
        }
    }
}
