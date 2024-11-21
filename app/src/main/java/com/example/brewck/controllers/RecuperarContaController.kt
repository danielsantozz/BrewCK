package com.example.brewck.controllers

import android.content.Intent
import android.widget.EditText
import android.widget.Toast
import com.example.brewck.MainActivity
import com.example.brewck.RecuperarConta
import com.google.firebase.auth.FirebaseAuth

class RecuperarContaController(private val activity: RecuperarConta) {

    // Instância do FirebaseAuth para interação com o Firebase Authentication
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Função responsável por enviar o e-mail de recuperação de senha
    fun recuperarConta(edtEmail: EditText) {
        // Obtém o e-mail digitado pelo usuário
        val email = edtEmail.text.toString().trim()

        // Verifica se o e-mail está vazio
        if (email.isEmpty()) {
            // Exibe uma mensagem de erro caso o e-mail não tenha sido informado
            Toast.makeText(activity, "Por favor, insira seu e-mail", Toast.LENGTH_SHORT).show()
            return
        }

        // Envia o e-mail de recuperação de senha
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Caso o envio seja bem-sucedido, exibe uma mensagem e redireciona para a tela principal
                    Toast.makeText(activity, "E-mail de recuperação enviado.", Toast.LENGTH_SHORT).show()
                    activity.startActivity(Intent(activity, MainActivity::class.java))
                } else {
                    // Caso ocorra um erro, exibe uma mensagem de falha no envio
                    Toast.makeText(activity, "Erro ao enviar e-mail de recuperação.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Função para voltar para a tela principal
    fun voltarParaMain() {
        // Inicia a MainActivity
        activity.startActivity(Intent(activity, MainActivity::class.java))
    }
}
