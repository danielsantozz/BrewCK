package com.example.brewck.controllers

import com.example.brewck.models.Barril
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BarrilController {
    private var barrisOriginais: List<Barril> = listOf()

    fun buscarBarrilPorEmail(callback: (List<Barril>) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val email = currentUser.email
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection("barris")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val barris = querySnapshot.documents.mapNotNull { document ->
                        val id = document.id
                        val nome = document.getString("nome") ?: ""
                        val capacidade = document.getLong("capacidade")?.toInt() ?: 0
                        val propriedade = document.getString("propriedade") ?: ""
                        val status = document.getString("status") ?: ""
                        val liquido = document.getString("liquido") ?: ""
                        val isFavorite = document.getBoolean("isFavorite") ?: false
                        val cliente = document.getString("cliente") ?: ""

                        Barril(id, nome, capacidade, propriedade, status, liquido, isFavorite, cliente)
                    }
                    barrisOriginais = barris
                    val barrisOrdenados = barrisOriginais.sortedBy { it.nome.toLowerCase() }
                    callback(barrisOrdenados)
                }
                .addOnFailureListener { exception ->
                    println("Erro ao carregar barris: ${exception.message}")
                    callback(emptyList())
                }
        } else {
            println("Usuário não autenticado.")
            callback(emptyList())
        }
    }


    fun filtrarBarris(nomeFiltro: String, filtroStatus: String, callback: (List<Barril>) -> Unit) {
        val barrisFiltradosPorNome = barrisOriginais.filter {
            it.nome.contains(nomeFiltro, ignoreCase = true)
        }

        val barrisFiltradosPorStatus = when (filtroStatus) {
            "Cheio" -> barrisFiltradosPorNome.filter { it.status == "Cheio" }
            "No Cliente" -> barrisFiltradosPorNome.filter { it.status == "No Cliente" }
            "Sujo" -> barrisFiltradosPorNome.filter { it.status == "Sujo" }
            "Limpo" -> barrisFiltradosPorNome.filter { it.status == "Limpo" }
            "Favoritos" -> barrisFiltradosPorNome.filter { it.isFavorite }
            else -> barrisFiltradosPorNome
        }

        val barrisOrdenados = barrisFiltradosPorStatus.sortedBy { it.nome.toLowerCase() }
        callback(barrisOrdenados)
    }

}
