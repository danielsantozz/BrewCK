package com.example.brewck.models

// Definição de um modelo de dados para representar um "Barril" no aplicativo
data class Barril(
    val id: String,            // Identificador único do barril
    val nome: String,          // Nome do barril
    val capacidade: Int,       // Capacidade do barril, geralmente medida em litros ou outra unidade de volume
    val propriedade: String,   // Propriedade ou dono do barril, por exemplo, o nome do proprietário
    val status: String,        // Status do barril, como "Cheio", "Limpo", "Sujo", etc.
    val liquido: String,       // Tipo de líquido armazenado no barril (ex: cerveja, água, etc.)
    val isFavorite: Boolean,   // Indica se o barril está marcado como favorito pelo usuário (true ou false)
    val cliente: String        // Nome do cliente associado ao barril (caso o barril esteja no cliente)
)
