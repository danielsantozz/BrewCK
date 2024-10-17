package com.example.brewck.controllers

import android.content.Context
import android.content.Intent
import android.widget.EditText
import android.widget.Toast
import com.example.brewck.MainActivity
import com.example.brewck.RecuperarConta
import com.google.firebase.auth.FirebaseAuth

class RecuperarContaController(private val activity: RecuperarConta) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun recuperarConta(edtEmail: EditText) {
        val email = edtEmail.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(activity, "Por favor, insira seu e-mail", Toast.LENGTH_SHORT).show()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(activity, "E-mail de recuperação enviado.", Toast.LENGTH_SHORT).show()
                    activity.startActivity(Intent(activity, MainActivity::class.java))
                } else {
                    Toast.makeText(activity, "Erro ao enviar e-mail de recuperação.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun voltarParaMain() {
        activity.startActivity(Intent(activity, MainActivity::class.java))
    }
}
