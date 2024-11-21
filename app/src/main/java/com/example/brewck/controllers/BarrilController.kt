package com.example.brewck.controllers

import com.example.brewck.models.Barril
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BarrilController {
    // Lista que armazenará os barris recuperados do Firestore
    private var barrisOriginais: List<Barril> = listOf()

    // Função para buscar barris no Firestore com base no email do usuário autenticado
    fun buscarBarrilPorEmail(callback: (List<Barril>) -> Unit) {
        // Obtém a instância do FirebaseAuth para acessar informações sobre o usuário autenticado
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Verifica se o usuário está autenticado
        if (currentUser != null) {

            // Obtém o email do usuário autenticado
            val email = currentUser.email

            // Obtém a instância do Firestore para acessar o banco de dados
            val firestore = FirebaseFirestore.getInstance()

            // Consulta a coleção "barris" no Firestore, filtrando por email do usuário
            firestore.collection("barris")
                .whereEqualTo("email", email)  // Filtra barris associados ao email do usuário
                .get()  // Executa a consulta
                .addOnSuccessListener { querySnapshot ->

                    // Mapeia os documentos retornados pela consulta para uma lista de objetos Barril
                    val barris = querySnapshot.documents.mapNotNull { document ->

                        // Para cada documento, tenta obter os dados do barril e cria um objeto Barril
                        val id = document.id
                        val nome = document.getString("nome") ?: ""  // Nome do barril (caso não exista, coloca uma string vazia)
                        val capacidade = document.getLong("capacidade")?.toInt() ?: 0  // Capacidade do barril
                        val propriedade = document.getString("propriedade") ?: ""  // Propriedade do barril
                        val status = document.getString("status") ?: ""  // Status do barril
                        val liquido = document.getString("liquido") ?: ""  // Tipo de líquido no barril
                        val isFavorite = document.getBoolean("isFavorite") ?: false  // Indica se o barril é favorito
                        val cliente = document.getString("cliente") ?: ""  // Cliente associado ao barril

                        // Cria e retorna um objeto Barril com os dados extraídos do Firestore
                        Barril(id, nome, capacidade, propriedade, status, liquido, isFavorite, cliente)
                    }
                    // Armazena a lista original de barris
                    barrisOriginais = barris

                    // Ordena a lista de barris por nome (ignorando maiúsculas/minúsculas)
                    val barrisOrdenados = barrisOriginais.sortedBy { it.nome.toLowerCase() }

                    // Chama o callback com a lista de barris ordenada
                    callback(barrisOrdenados)
                }
                .addOnFailureListener { exception ->
                    // Se houver erro ao carregar os barris, imprime o erro e chama o callback com uma lista vazia
                    println("Erro ao carregar barris: ${exception.message}")
                    callback(emptyList())
                }
        } else {
            // Se o usuário não estiver autenticado, imprime uma mensagem de erro e chama o callback com uma lista vazia
            println("Usuário não autenticado.")
            callback(emptyList())
        }
    }

    // Função para filtrar os barris com base no nome e no status
    fun filtrarBarris(nomeFiltro: String, filtroStatus: String, callback: (List<Barril>) -> Unit) {
        // Filtra os barris por nome (ignora maiúsculas/minúsculas)
        val barrisFiltradosPorNome = barrisOriginais.filter {
            it.nome.contains(nomeFiltro, ignoreCase = true)
        }

        // Filtra os barris por status, considerando diferentes opções
        val barrisFiltradosPorStatus = when (filtroStatus) {
            "Cheio" -> barrisFiltradosPorNome.filter { it.status == "Cheio" }
            "No Cliente" -> barrisFiltradosPorNome.filter { it.status == "No Cliente" }
            "Sujo" -> barrisFiltradosPorNome.filter { it.status == "Sujo" }
            "Limpo" -> barrisFiltradosPorNome.filter { it.status == "Limpo" }
            "Favoritos" -> barrisFiltradosPorNome.filter { it.isFavorite }  // Filtra barris favoritos
            else -> barrisFiltradosPorNome  // Se não houver filtro de status, usa apenas o filtro de nome
        }

        // Ordena os barris filtrados por nome (ignorando maiúsculas/minúsculas)
        val barrisOrdenados = barrisFiltradosPorStatus.sortedBy { it.nome.toLowerCase() }

        // Chama o callback com a lista filtrada e ordenada
        callback(barrisOrdenados)
    }
}
