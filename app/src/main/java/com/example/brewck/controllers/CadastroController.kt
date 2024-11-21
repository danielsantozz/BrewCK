package com.example.brewck.controllers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class CadastroController {

    // Instâncias do FirebaseAuth (para autenticação) e do FirebaseFirestore (para salvar dados no Firestore)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Função que cria um novo usuário no Firebase com email e senha
    fun criarUsuario(email: String, senha: String, nome: String, callback: (Boolean, String?) -> Unit) {

        // Validação do nome
        if (nome.length < 3) {
            callback(false, "O nome deve ter pelo menos 3 caracteres.")
            return
        }
        if (nome.length > 50) {
            callback(false, "O nome não pode ter mais de 50 caracteres.")
            return
        }

        // Validação da senha
        if (senha.length < 6) {
            callback(false, "A senha deve ter pelo menos 6 caracteres.")
            return
        }
        if (senha.length > 20) {
            callback(false, "A senha não pode ter mais de 20 caracteres.")
            return
        }

        // Chama o método para criar o usuário no Firebase Authentication
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    // Se a criação do usuário foi bem-sucedida, obtém o ID do usuário atual
                    val userId = auth.currentUser?.uid
                    if (userId != null) {

                        // Cria um mapa com as informações do usuário (nome e email)
                        val userMap = hashMapOf(
                            "nome" to nome,
                            "email" to email
                        )
                        // Salva as informações do usuário na coleção "users" no Firestore
                        db.collection("users").document(userId).set(userMap)
                            .addOnSuccessListener {
                                // Se salvar com sucesso, chama o callback com sucesso
                                callback(true, null)
                            }
                            .addOnFailureListener { e ->
                                // Se falhar ao salvar as informações no Firestore, chama o callback com erro
                                callback(false, "Falha ao salvar informações adicionais: ${e.localizedMessage}")
                            }
                    } else {
                        // Se o ID do usuário não for encontrado, chama o callback com erro
                        callback(false, "Erro ao obter ID do usuário")
                    }
                } else {
                    // Se a criação do usuário falhar, trata os possíveis erros
                    task.exception?.let { exception ->
                        when (exception) {
                            // Caso o email já esteja em uso por outro usuário
                            is FirebaseAuthUserCollisionException -> callback(false, "Email já está em uso")

                            // Caso a senha seja fraca (não atenda aos critérios de segurança do Firebase)
                            is FirebaseAuthWeakPasswordException -> callback(false, "A senha é muito fraca.")

                            // Caso o email tenha um formato inválido
                            is FirebaseAuthInvalidCredentialsException -> callback(false, "O formato do email é inválido.")

                            // Para qualquer outro tipo de erro, chama o callback com a mensagem de erro
                            else -> callback(false, exception.localizedMessage)
                        }
                    } ?: callback(false, "Erro desconhecido")  // Caso a exceção não seja reconhecida, chama com erro desconhecido
                }
            }
    }

}
