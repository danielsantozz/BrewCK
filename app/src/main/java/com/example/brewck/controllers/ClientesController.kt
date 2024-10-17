package com.example.brewck.controllers

import com.example.brewck.models.Cliente
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ClientesController {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun buscarClientesPorEmail(callback: (Boolean, List<Pair<String, Cliente>>?, String?) -> Unit) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val email = currentUser.email

            firestore.collection("clientes")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val listaClientes = mutableListOf<Pair<String, Cliente>>()

                    for (document in querySnapshot.documents) {
                        val id = document.id
                        val nome = document.getString("nome") ?: ""
                        val cpf = document.getString("cpf") ?: ""
                        val barril = document.getString("barril") ?: ""
                        val endereco = document.getString("endereco") ?: ""
                        val avaliacao = document.getString("avaliacao") ?: ""
                        listaClientes.add(id to Cliente(id, nome, cpf, barril, endereco, avaliacao))
                    }
                    callback(true, listaClientes, null)
                }
                .addOnFailureListener { exception ->
                    callback(false, null, "Erro ao carregar clientes: ${exception.message}")
                }
        } else {
            callback(false, null, "Usuário não autenticado.")
        }
    }

    fun filtrarClientes(listaClientes: List<Pair<String, Cliente>>, nomeFiltro: String, avaliacaoFiltro: String?): List<Cliente> {
        return listaClientes.filter {
            val matchesNome = it.second.nome.contains(nomeFiltro, ignoreCase = true)
            val matchesAvaliacao = when (avaliacaoFiltro) {
                null, "", "Todos" -> true
                else -> it.second.avaliacao == avaliacaoFiltro
            }
            matchesNome && matchesAvaliacao
        }.map { it.second }
    }
}
