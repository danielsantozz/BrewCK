package com.example.brewck.controllers

import com.example.brewck.models.Cliente
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ClientesController {

    // Instâncias do FirebaseAuth (para autenticação) e FirebaseFirestore (para salvar e recuperar dados)
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Função para buscar clientes associados ao email do usuário autenticado
    fun buscarClientesPorEmail(callback: (Boolean, List<Pair<String, Cliente>>?, String?) -> Unit) {
        val currentUser = auth.currentUser // Obtém o usuário autenticado atualmente

        // Verifica se o usuário está autenticado
        if (currentUser != null) {
            val email = currentUser.email // Obtém o email do usuário autenticado

            // Realiza uma consulta na coleção "clientes" do Firestore, filtrando pelo email
            firestore.collection("clientes")
                .whereEqualTo("email", email) // Filtra por clientes que possuem o mesmo email do usuário
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val listaClientes = mutableListOf<Pair<String, Cliente>>() // Lista correta para armazenar os clientes recuperados

                    // Itera sobre os documentos retornados da consulta
                    for (document in querySnapshot.documents) {
                        val id = document.id // ID do documento
                        val nome = document.getString("nome") ?: "" // Obtém o nome do cliente ou uma string vazia se não existir
                        val cpf = document.getString("cpf") ?: "" // Obtém o CPF do cliente ou uma string vazia
                        val barril = document.get("barril") as? List<String> ?: emptyList() // Obtém o barril associado ao cliente
                        val endereco = document.getString("endereco") ?: "" // Obtém o endereço do cliente
                        val avaliacao = document.getString("avaliacao") ?: "" // Obtém a avaliação do cliente

                        // Adiciona um par (id, Cliente) à lista
                        listaClientes.add(id to Cliente(id, nome, cpf, barril, endereco, avaliacao))
                    }

                    // Chama o callback com sucesso, passando a lista de clientes
                    callback(true, listaClientes, null)
                }
                .addOnFailureListener { exception ->
                    // Em caso de erro ao carregar os clientes, chama o callback com falha
                    callback(false, null, "Erro ao carregar clientes: ${exception.message}")
                }
        } else {
            // Se o usuário não estiver autenticado, chama o callback com falha
            callback(false, null, "Usuário não autenticado.")
        }
    }


    // Função para filtrar a lista de clientes baseada em nome e avaliação
    fun filtrarClientes(listaClientes: MutableList<Pair<String, Cliente>>, nomeFiltro: String, avaliacaoFiltro: String?): List<Cliente> {
        return listaClientes.filter {
            // Filtra os clientes pelo nome, ignorando o caso
            val matchesNome = it.second.nome.contains(nomeFiltro, ignoreCase = true)

            // Filtra pela avaliação, se fornecida, ou permite todos se a avaliação for nula, vazia ou "Todos"
            val matchesAvaliacao = when (avaliacaoFiltro) {
                null, "", "Todos" -> true
                else -> it.second.avaliacao == avaliacaoFiltro
            }

            // Retorna apenas os clientes que correspondem tanto ao nome quanto à avaliação
            matchesNome && matchesAvaliacao
        }.map { it.second } // Mapeia o resultado para retornar apenas a lista de clientes, sem os IDs
    }
}
