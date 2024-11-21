package com.example.brewck.controllers

import android.util.Log
import android.widget.Toast
import com.example.brewck.models.Liquido
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LiquidosController(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    // Função para adicionar um novo líquido à coleção 'liquidos' no Firestore.
    fun addLiquido(nome: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        // Obtém o e-mail do usuário autenticado.
        val email = auth.currentUser?.email
        if (email != null) {
            // Cria um mapa com os dados do líquido a ser adicionado.
            val liquidoData = hashMapOf(
                "email" to email,
                "nome" to nome
            )

            // Adiciona o líquido à coleção 'liquidos' no Firestore.
            firestore.collection("liquidos")
                .add(liquidoData)
                .addOnSuccessListener {
                    // Log de sucesso e chamada do callback onSuccess() se o líquido for adicionado com sucesso.
                    Log.d("LiquidosController", "Líquido adicionado com sucesso!")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    // Log de erro e chamada do callback onFailure() se ocorrer um erro ao adicionar o líquido.
                    Log.e("LiquidosController", "Erro ao adicionar líquido: ${e.message}", e)
                    onFailure("Erro ao adicionar líquido: ${e.message}")
                }
        } else {
            // Se o usuário não estiver logado, chama onFailure() com uma mensagem de erro.
            onFailure("Usuário não logado. Não é possível adicionar líquido.")
        }
    }

    // Função para carregar a lista de líquidos do Firestore associados ao usuário autenticado.
    fun loadLiquidos(onSuccess: (List<Liquido>) -> Unit, onFailure: (String) -> Unit) {
        // Obtém o e-mail do usuário autenticado.
        val email = auth.currentUser?.email
        if (email != null) {
            // Consulta os líquidos da coleção 'liquidos' com o e-mail do usuário autenticado.
            firestore.collection("liquidos")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    // Mapeia os documentos retornados para uma lista de objetos 'Liquido'.
                    val liquidos = documents.map { doc -> doc.toObject(Liquido::class.java) }
                    // Chama o callback onSuccess() com a lista de líquidos.
                    onSuccess(liquidos)
                }
                .addOnFailureListener { e ->
                    // Log de erro e chama o callback onFailure() se ocorrer um erro ao carregar os líquidos.
                    Log.e("LiquidosController", "Erro ao buscar líquidos: ${e.message}", e)
                    onFailure("Erro ao buscar líquidos: ${e.message}")
                }
        } else {
            // Se o usuário não estiver logado, chama onFailure() com uma mensagem de erro.
            onFailure("Usuário não logado.")
        }
    }

    // Função para deletar um líquido específico do Firestore.
    fun deletarLiquido(liquido: Liquido, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        // Obtém o e-mail do usuário autenticado.
        val email = auth.currentUser?.email
        if (email != null) {
            // Consulta os líquidos na coleção 'liquidos' do Firestore que correspondem ao e-mail e nome fornecido.
            firestore.collection("liquidos")
                .whereEqualTo("email", email)
                .whereEqualTo("nome", liquido.nome)
                .get()
                .addOnSuccessListener { documents ->
                    // Para cada documento encontrado, tenta deletá-lo.
                    for (document in documents) {
                        firestore.collection("liquidos").document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                // Log de sucesso e chamada do callback onSuccess() se o líquido for deletado com sucesso.
                                Log.d("LiquidosController", "Líquido removido com sucesso!")
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                // Log de erro e chamada do callback onFailure() se ocorrer um erro ao deletar o líquido.
                                Log.e("LiquidosController", "Erro ao remover líquido: ${e.message}", e)
                                onFailure("Erro ao excluir líquido: ${e.message}")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    // Log de erro e chamada do callback onFailure() se ocorrer um erro ao buscar o líquido para exclusão.
                    Log.e("LiquidosController", "Erro ao buscar líquido para exclusão: ${e.message}", e)
                    onFailure("Erro ao excluir líquido: ${e.message}")
                }
        } else {
            // Se o usuário não estiver logado, chama onFailure() com uma mensagem de erro.
            onFailure("Usuário não logado.")
        }
    }
}
