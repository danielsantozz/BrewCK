package com.example.brewck.controllers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdicionarBarrilController {

    // Função privada para validar os campos antes de adicionar o barril.
    private fun validarCampos(nome: String, capacidade: Int): Pair<Boolean, String> {
        // Verifica se o nome está vazio ou a capacidade é menor ou igual a 0.
        if (nome.isEmpty() || capacidade <= 0) {
            return Pair(false, "Por favor, preencha todos os campos corretamente.")
        }

        // Verifica se o nome ultrapassa 100 caracteres.
        if (nome.length > 100) {
            return Pair(false, "Nome deve ter no máximo 100 caracteres.")
        }

        // Verifica se a capacidade é maior que 9999 litros.
        if (capacidade > 9999) {
            return Pair(false, "Capacidade deve ser no máximo 9999 litros.")
        }

        // Se as validações passarem, retorna true com uma mensagem vazia.
        return Pair(true, "")
    }

    // Função pública para adicionar um barril.
    fun adicionarBarril(
        nome: String,
        capacidade: Int,
        propriedade: String,
        status: String,
        liquido: String,
        isFavorite: Boolean,
        cliente: String,
        callback: (Boolean, String) -> Unit
    ) {
        // Valida os campos de nome e capacidade usando a função de validação.
        val (valido, mensagem) = validarCampos(nome, capacidade)

        // Se a validação falhar, chama o callback com um erro.
        if (!valido) {
            callback(false, mensagem)
            return
        }

        // Obtém a instância do Firestore e da autenticação Firebase.
        val firestore = FirebaseFirestore.getInstance()
        val barrisCollection = firestore.collection("barris")
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Verifica se o usuário está autenticado.
        if (currentUser != null) {
            // Obtém o email do usuário autenticado.
            val email = currentUser.email

            // Verifica se já existe um barril com o mesmo nome e email do usuário.
            barrisCollection
                .whereEqualTo("nome", nome)
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        // Caso não exista duplicata, adiciona o novo barril.
                        val barrilData = hashMapOf(
                            "nome" to nome,
                            "email" to email,
                            "capacidade" to capacidade,
                            "propriedade" to propriedade,
                            "status" to status,
                            "isFavorite" to isFavorite,
                            "liquido" to liquido,
                            "cliente" to cliente
                        )

                        barrisCollection.add(barrilData)
                            .addOnSuccessListener {
                                callback(true, "")
                            }
                            .addOnFailureListener { exception ->
                                callback(false, "Erro ao adicionar barril: ${exception.message}")
                            }
                    } else {
                        // Caso exista duplicata, retorna erro.
                        callback(false, "Já existe um barril com esse nome.")
                    }
                }
                .addOnFailureListener { exception ->
                    callback(false, "Erro ao verificar duplicidade: ${exception.message}")
                }
        } else {
            // Se o usuário não estiver autenticado, chama o callback com erro de autenticação.
            callback(false, "Usuário não autenticado.")
        }
    }

}
