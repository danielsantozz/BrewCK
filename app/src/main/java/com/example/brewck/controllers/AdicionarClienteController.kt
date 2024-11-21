package com.example.brewck.controllers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdicionarClienteController {
    // Função para adicionar um novo cliente
    fun adicionarCliente(
        nome: String, // Nome do cliente
        cpf: String, // CPF do cliente
        barril: String, // Barril associado ao cliente
        endereco: String, // Endereço do cliente
        avaliacao: String, // Avaliação do cliente
        callback: (Boolean, String) -> Unit // Função de callback que retorna sucesso/erro
    ) {
        // Obtém a instância do FirebaseAuth para verificar o usuário autenticado
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Verifica se o usuário está autenticado
        if (currentUser != null) {
            val email = currentUser.email // Obtém o email do usuário autenticado
            val firestore = FirebaseFirestore.getInstance() // Instância do Firestore
            val clientesCollection = firestore.collection("clientes") // Referência à coleção de clientes no Firestore

            // Verifica se já existe um cliente com o mesmo nome
            clientesCollection.whereEqualTo("nome", nome).get()
                .addOnSuccessListener { querySnapshot ->
                    // Se não houver cliente com o mesmo nome
                    if (querySnapshot.isEmpty) {
                        // Verifica se já existe um cliente com o mesmo CPF
                        clientesCollection.whereEqualTo("cpf", cpf).get()
                            .addOnSuccessListener { cpfQuerySnapshot ->
                                // Se não houver cliente com o mesmo CPF
                                if (cpfQuerySnapshot.isEmpty) {
                                    // Cria um novo cliente com os dados fornecidos
                                    val clienteData = hashMapOf(
                                        "nome" to nome,
                                        "email" to email,
                                        "cpf" to cpf,
                                        "barril" to barril,
                                        "endereco" to endereco,
                                        "avaliacao" to avaliacao
                                    )

                                    // Adiciona o novo cliente na coleção "clientes"
                                    clientesCollection.add(clienteData)
                                        .addOnSuccessListener {
                                            // Callback de sucesso
                                            callback(true, "")
                                        }
                                        .addOnFailureListener { exception ->
                                            // Em caso de erro ao adicionar o cliente
                                            println("Erro ao adicionar cliente: ${exception.message}")
                                            callback(false, "Erro ao adicionar cliente: ${exception.message}")
                                        }
                                } else {
                                    // Se já existir um cliente com o mesmo CPF
                                    callback(false, "Já existe um cliente com esse CPF.")
                                }
                            }
                            .addOnFailureListener { exception ->
                                // Em caso de erro ao verificar o CPF existente
                                println("Erro ao verificar CPF existente: ${exception.message}")
                                callback(false, "Erro ao verificar CPF existente.")
                            }
                    } else {
                        // Se já existir um cliente com o mesmo nome
                        callback(false, "Já existe um cliente com esse nome.")
                    }
                }
                .addOnFailureListener { exception ->
                    // Em caso de erro ao verificar o nome do cliente
                    println("Erro ao verificar cliente existente: ${exception.message}")
                    callback(false, "Erro ao verificar cliente existente.")
                }
        } else {
            // Caso o usuário não esteja autenticado
            println("Usuário não autenticado.")
            callback(false, "Usuário não autenticado.")
        }
    }
}
