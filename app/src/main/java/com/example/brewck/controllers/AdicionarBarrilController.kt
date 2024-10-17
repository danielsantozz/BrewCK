package com.example.brewck.controllers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdicionarBarrilController {

    private fun validarCampos(nome: String, capacidade: Int): Pair<Boolean, String> {
        if (nome.isEmpty() || capacidade <= 0) {
            return Pair(false, "Por favor, preencha todos os campos corretamente.")
        }

        if (nome.length > 100) {
            return Pair(false, "Nome deve ter no máximo 100 caracteres.")
        }

        if (capacidade > 9999) {
            return Pair(false, "Capacidade deve ser no máximo 9999 litros.")
        }

        return Pair(true, "")
    }

    fun adicionarBarril(
        nome: String,
        capacidade: Int,
        propriedade: String,
        status: String,
        liquido: String,
        isFavorite: Boolean,
        cliente: String,
        callback: (Boolean, String) -> Unit
    ) {
        val (valido, mensagem) = validarCampos(nome, capacidade)
        if (!valido) {
            callback(false, mensagem)
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        val barrisCollection = firestore.collection("barris")
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val email = currentUser.email

            val barrilData = hashMapOf(
                "nome" to nome,
                "email" to email,
                "capacidade" to capacidade,
                "propriedade" to propriedade,
                "status" to status,
                "isFavorite" to isFavorite,
                "liquido" to liquido,
                "cliente" to cliente
            )

            barrisCollection.add(barrilData)
                .addOnSuccessListener {
                    callback(true, "")
                }
                .addOnFailureListener { exception ->
                    callback(false, "Erro ao adicionar barril: ${exception.message}")
                }
        } else {
            callback(false, "Usuário não autenticado.")
        }
    }
}
