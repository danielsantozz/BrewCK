package com.example.brewck.controllers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginController {

    // Instância do FirebaseAuth para gerenciar autenticação de usuários.
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Função que verifica se o usuário está autenticado/logado no Firebase.
    // Retorna true se o usuário estiver logado, caso contrário, retorna false.
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // Função para realizar o login do usuário com email e senha.
    // Ela utiliza um callback para informar o sucesso ou falha da operação.
    fun fazerLogin(email: String, senha: String, callback: (Boolean, String?) -> Unit) {

        // Tenta autenticar o usuário com o método signInWithEmailAndPassword.
        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Caso o login seja bem-sucedido, chama o callback com 'true' e nenhum erro.
                    callback(true, null)
                } else {
                    // Caso ocorra uma falha, verifica qual exceção foi gerada.
                    task.exception?.let { exception ->
                        when (exception) {

                            // Se a exceção for do tipo 'FirebaseAuthInvalidUserException', significa que o email não foi encontrado.
                            is FirebaseAuthInvalidUserException -> callback(false, "Email não encontrado.")

                            // Se a exceção for do tipo 'FirebaseAuthInvalidCredentialsException', significa que a senha está incorreta.
                            is FirebaseAuthInvalidCredentialsException -> callback(false, "Senha incorreta.")

                            // Para qualquer outra exceção, retorna a mensagem de erro genérica.
                            else -> callback(false, exception.localizedMessage)
                        }
                    } ?: callback(false, "Erro desconhecido") // Caso nenhuma exceção tenha sido capturada, retorna um erro genérico.
                }
            }
    }
}
