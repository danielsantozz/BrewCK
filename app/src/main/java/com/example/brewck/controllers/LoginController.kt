package com.example.brewck.controllers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginController {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun fazerLogin(email: String, senha: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    task.exception?.let { exception ->
                        when (exception) {
                            is FirebaseAuthInvalidUserException -> callback(false, "Email não encontrado.")
                            is FirebaseAuthInvalidCredentialsException -> callback(false, "Senha incorreta.")
                            else -> callback(false, exception.localizedMessage)
                        }
                    } ?: callback(false, "Erro desconhecido")
                }
            }
    }
}
