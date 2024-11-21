package com.example.brewck.controllers

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class EditarBarrilController(private val context: Context) {

    // Instância do Firestore, utilizada para interagir com o banco de dados Firebase.
    private val firestore = FirebaseFirestore.getInstance()

    // Função para atualizar os dados de um barril no Firestore.
    fun atualizarBarril(
        barrilId: String,  // ID do barril que será atualizado
        nome: String,      // Nome do barril
        capacidade: Int,   // Capacidade do barril
        propriedade: String, // Propriedade do barril
        callback: (Boolean) -> Unit  // Função de callback que retorna se a atualização foi bem-sucedida ou não
    ) {
        // Referência ao documento do barril no Firestore.
        val barrilRef = firestore.collection("barris").document(barrilId)

        // HashMap contendo os dados do barril que serão atualizados.
        val barrilData = hashMapOf<String, Any>(
            "nome" to nome,  // Atualiza o nome do barril
            "capacidade" to capacidade,  // Atualiza a capacidade do barril
            "propriedade" to propriedade  // Atualiza a propriedade do barril
        )

        // Atualiza os dados do barril no Firestore com os novos valores.
        barrilRef.update(barrilData)
            .addOnSuccessListener {
                // Se a atualização for bem-sucedida, imprime uma mensagem de sucesso e chama o callback com 'true'.
                println("Barril atualizado com sucesso.")
                callback(true)  // Informa que a atualização foi bem-sucedida
            }
            .addOnFailureListener { exception ->
                // Se ocorrer um erro na atualização, imprime a mensagem de erro e chama o callback com 'false'.
                println("Erro ao atualizar barril: ${exception.message}")
                callback(false)  // Informa que a atualização falhou
            }
    }

    // Função para deletar um barril do Firestore.
    fun deletarBarril(barrilId: String, callback: (Boolean) -> Unit) {

        // Referência ao documento do barril no Firestore.
        val barrilRef = firestore.collection("barris").document(barrilId)

        // Deleta o documento do barril no Firestore.
        barrilRef.delete()
            .addOnSuccessListener {
                // Se a exclusão for bem-sucedida, imprime uma mensagem de sucesso e chama o callback com 'true'.
                println("Barril deletado com sucesso.")
                callback(true)  // Informa que a exclusão foi bem-sucedida
            }
            .addOnFailureListener { exception ->
                // Se ocorrer um erro na exclusão, imprime a mensagem de erro e chama o callback com 'false'.
                println("Erro ao deletar barril: ${exception.message}")
                callback(false)  // Informa que a exclusão falhou
            }
    }

    // Função para mostrar uma mensagem em um Toast para o usuário.
    fun mostrarMensagem(mensagem: String) {
        // Exibe um Toast com a mensagem recebida.
        Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
    }
}
