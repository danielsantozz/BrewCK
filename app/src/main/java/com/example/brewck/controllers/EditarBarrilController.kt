package com.example.brewck.controllers

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class EditarBarrilController(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()

    fun atualizarBarril(
        barrilId: String,
        nome: String,
        capacidade: Int,
        propriedade: String,
        status: String,
        liquido: String,
        callback: (Boolean) -> Unit
    ) {
        val barrilRef = firestore.collection("barris").document(barrilId)

        val barrilData = hashMapOf<String, Any>(
            "nome" to nome,
            "capacidade" to capacidade,
            "propriedade" to propriedade,
            "status" to status,
            "liquido" to liquido
        )

        barrilRef.update(barrilData)
            .addOnSuccessListener {
                println("Barril atualizado com sucesso.")
                callback(true)
            }
            .addOnFailureListener { exception ->
                println("Erro ao atualizar barril: ${exception.message}")
                callback(false)
            }
    }

    fun deletarBarril(barrilId: String, callback: (Boolean) -> Unit) {
        val barrilRef = firestore.collection("barris").document(barrilId)

        barrilRef.delete()
            .addOnSuccessListener {
                println("Barril deletado com sucesso.")
                callback(true)
            }
            .addOnFailureListener { exception ->
                println("Erro ao deletar barril: ${exception.message}")
                callback(false)
            }
    }

    fun mostrarMensagem(mensagem: String) {
        Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
    }
}
