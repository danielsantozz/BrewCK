package com.example.brewck.models

data class Barril(
    val id: String,
    val nome: String,
    val capacidade: Int,
    val propriedade: String,
    val status: String,
    val liquido: String,
    val isFavorite: Boolean,
    val cliente: String)