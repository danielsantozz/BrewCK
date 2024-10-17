package com.example.brewck.controllers

import android.content.Context
import android.widget.ImageView
import android.widget.Toast
import com.example.brewck.R
import com.google.firebase.firestore.FirebaseFirestore

class DetalhesClienteController(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()

    fun avaliarCliente(avaliacao: String, clienteId: String, imgCliente: ImageView) {
        val clienteRef = firestore.collection("clientes").document(clienteId)

        clienteRef.update("avaliacao", avaliacao)
            .addOnSuccessListener {
                when (avaliacao) {
                    "Bom" -> imgCliente.setImageResource(R.drawable.usergreen)
                    "Ruim" -> imgCliente.setImageResource(R.drawable.userred)
                }
                Toast.makeText(context, "Cliente avaliado como $avaliacao", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Falha ao atualizar avaliação: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    fun resetarAvaliacao(clienteId: String, imgCliente: ImageView) {
        val clienteRef = firestore.collection("clientes").document(clienteId)

        clienteRef.update("avaliacao", "")
            .addOnSuccessListener {
                imgCliente.setImageResource(R.drawable.user)
                Toast.makeText(context, "Avaliação resetada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Falha ao resetar avaliação: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
