package com.example.brewck.models

// Definição de um modelo de dados para representar um "Cliente" no aplicativo
data class Cliente(
    val id: String,             // Identificador único do cliente
    val nome: String,           // Nome do cliente
    val cpf: String,            // CPF do cliente, utilizado como identificação única (no Brasil)
    val barril: List<String>,   // Lista de barris associados ao cliente
    val endereco: String,       // Endereço do cliente, onde ele reside ou onde o barril pode ser encontrado
    val avaliacao: String       // Avaliação do cliente, provavelmente relacionada ao serviço ou ao estado do barril
)
