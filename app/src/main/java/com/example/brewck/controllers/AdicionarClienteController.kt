package com.example.brewck.controllers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdicionarClienteController {
    fun adicionarCliente(
        nome: String,
        cpf: String,
        barril: String,
        endereco: String,
        avaliacao: String,
        callback: (Boolean) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val email = currentUser.email
            val firestore = FirebaseFirestore.getInstance()
            val clientesCollection = firestore.collection("clientes")

            val clienteData = hashMapOf(
                "nome" to nome,
                "email" to email,
                "cpf" to cpf,
                "barril" to barril,
                "endereco" to endereco,
                "avaliacao" to avaliacao
            )

            clientesCollection.add(clienteData)
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener { exception ->
                    println("Erro ao adicionar cliente: ${exception.message}")
                    callback(false)
                }
        } else {
            println("Usuário não autenticado.")
            callback(false)
        }
    }
}
