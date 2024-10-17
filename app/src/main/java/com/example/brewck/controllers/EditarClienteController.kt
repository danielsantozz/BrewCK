package com.example.brewck.controllers

import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.example.brewck.models.Barril
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditarClienteController(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val listaBarris = mutableListOf<Pair<String, Barril>>()

    fun buscarBarrisPorEmail(spinnerBarris: Spinner) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val email = currentUser.email

            firestore.collection("barris")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    listaBarris.clear()
                    for (document in querySnapshot.documents) {
                        val id = document.id
                        val nome = document.getString("nome") ?: ""
                        val capacidade = document.getLong("capacidade")?.toInt() ?: 0
                        val propriedade = document.getString("propriedade") ?: ""
                        val status = document.getString("status") ?: ""
                        val liquido = document.getString("liquido") ?: ""
                        val cliente = document.getString("cliente") ?: ""
                        listaBarris.add(id to Barril(id, nome, capacidade, propriedade, status, liquido, false, cliente))
                    }

                    if (listaBarris.isEmpty()) {
                        Toast.makeText(context, "Nenhum barril encontrado.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val barrilNomes = listaBarris.map { it.second.nome }
                    val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, barrilNomes)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerBarris.adapter = adapter
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Erro ao carregar barris: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.d("EditarClienteController", "Usuário não autenticado.")
        }
    }

    fun validarCampos(nome: String, cpf: String, endereco: String): Boolean {
        if (nome.isEmpty() || cpf.isEmpty() || endereco.isEmpty()) {
            Toast.makeText(context, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (nome.length > 100) {
            Toast.makeText(context, "Nome deve ter no máximo 100 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (cpf.length != 11) {
            Toast.makeText(context, "CPF deve ter 11 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (endereco.length > 100) {
            Toast.makeText(context, "Endereço deve ter no máximo 100 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    fun atualizarCliente(
        clienteId: String,
        nome: String,
        cpf: String,
        barril: String,
        endereco: String,
        callback: (Boolean) -> Unit
    ) {
        val clienteRef = firestore.collection("clientes").document(clienteId)

        val clienteData: Map<String, Any> = mapOf(
            "nome" to nome,
            "cpf" to cpf,
            "barril" to barril,
            "endereco" to endereco
        )

        clienteRef.update(clienteData)
            .addOnSuccessListener {
                Log.d("EditarClienteController", "Cliente atualizado com sucesso.")
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e("EditarClienteController", "Erro ao atualizar cliente: ${exception.message}")
                callback(false)
            }
    }

    fun deletarCliente(clienteId: String, callback: (Boolean) -> Unit) {
        val clienteRef = firestore.collection("clientes").document(clienteId)

        clienteRef.delete()
            .addOnSuccessListener {
                Log.d("EditarClienteController", "Cliente deletado com sucesso.")
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e("EditarClienteController", "Erro ao deletar cliente: ${exception.message}")
                callback(false)
            }

    }

    fun getListaBarris() = listaBarris
}
