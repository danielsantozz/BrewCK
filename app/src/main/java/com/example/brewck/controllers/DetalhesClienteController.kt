package com.example.brewck.controllers

import android.content.Context
import android.widget.ImageView
import android.widget.Toast
import com.example.brewck.R
import com.google.firebase.firestore.FirebaseFirestore

class DetalhesClienteController(private val context: Context) {

    // Instância do Firestore para acessar e modificar dados no banco de dados.
    private val firestore = FirebaseFirestore.getInstance()

    // Função para avaliar um cliente (Bom ou Ruim) e atualizar a avaliação no banco de dados.
    fun avaliarCliente(avaliacao: String, clienteId: String, imgCliente: ImageView) {

        // Referência ao documento do cliente no Firestore usando o ID do cliente.
        val clienteRef = firestore.collection("clientes").document(clienteId)

        // Atualiza a avaliação do cliente no Firestore com o valor passado na função.
        clienteRef.update("avaliacao", avaliacao)
            .addOnSuccessListener {

                // Dependendo da avaliação, a imagem do cliente é alterada.
                when (avaliacao) {
                    "Bom" -> imgCliente.setImageResource(R.drawable.usergreen)  // Imagem de cliente com avaliação boa.
                    "Ruim" -> imgCliente.setImageResource(R.drawable.userred)   // Imagem de cliente com avaliação ruim.
                }

                // Exibe uma mensagem de sucesso ao usuário indicando que a avaliação foi realizada.
                Toast.makeText(context, "Cliente avaliado como $avaliacao", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Exibe uma mensagem de erro caso a atualização da avaliação no Firestore falhe.
                Toast.makeText(context, "Falha ao atualizar avaliação: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Função para resetar a avaliação de um cliente (limpando o campo de avaliação no Firestore).
    fun resetarAvaliacao(clienteId: String, imgCliente: ImageView) {

        // Referência ao documento do cliente no Firestore usando o ID do cliente.
        val clienteRef = firestore.collection("clientes").document(clienteId)

        // Atualiza a avaliação do cliente para um valor vazio, resetando a avaliação.
        clienteRef.update("avaliacao", "")
            .addOnSuccessListener {
                // Após o reset da avaliação, a imagem do cliente é restaurada ao estado padrão.
                imgCliente.setImageResource(R.drawable.user)  // Imagem padrão do cliente sem avaliação.
                // Exibe uma mensagem indicando que a avaliação foi resetada com sucesso.
                Toast.makeText(context, "Avaliação resetada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Exibe uma mensagem de erro caso o reset da avaliação no Firestore falhe.
                Toast.makeText(context, "Falha ao resetar avaliação: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
