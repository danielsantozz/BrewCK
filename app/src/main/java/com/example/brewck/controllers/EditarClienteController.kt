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

    // Instâncias do Firebase Firestore e Firebase Auth para interação com o banco de dados e autenticação.
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Lista de barris que será populada com dados do Firestore.
    private val listaBarris = mutableListOf<Pair<String, Barril>>()

    // Função para buscar barris relacionados ao e-mail do usuário autenticado e preencher um Spinner.
    fun buscarBarrisPorEmail(spinnerBarris: Spinner) {

        // Obtém o usuário autenticado.
        val currentUser = auth.currentUser

        // Verifica se há um usuário autenticado.
        if (currentUser != null) {

            // Pega o e-mail do usuário autenticado.
            val email = currentUser.email

            // Realiza uma consulta no Firestore para pegar os barris relacionados ao e-mail do usuário.
            firestore.collection("barris")
                .whereEqualTo("email", email)  // Filtra barris com o e-mail do usuário
                .get()
                .addOnSuccessListener { querySnapshot ->

                    // Limpa a lista de barris para evitar duplicação de dados.
                    listaBarris.clear()

                    // Itera sobre os documentos retornados pela consulta.
                    for (document in querySnapshot.documents) {

                        // Obtém os dados de cada barril e os adiciona à lista de barris.
                        val id = document.id
                        val nome = document.getString("nome") ?: ""
                        val capacidade = document.getLong("capacidade")?.toInt() ?: 0
                        val propriedade = document.getString("propriedade") ?: ""
                        val status = document.getString("status") ?: ""
                        val liquido = document.getString("liquido") ?: ""
                        val cliente = document.getString("cliente") ?: ""
                        listaBarris.add(id to Barril(id, nome, capacidade, propriedade, status, liquido, false, cliente))
                    }

                    // Se não houver barris, exibe um Toast com uma mensagem.
                    if (listaBarris.isEmpty()) {
                        Toast.makeText(context, "Nenhum barril encontrado.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // Cria uma lista de nomes dos barris para mostrar no Spinner.
                    val barrilNomes = listaBarris.map { it.second.nome }

                    // Cria um adaptador para popular o Spinner com os nomes dos barris.
                    val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, barrilNomes)

                    // Define o layout do dropdown do Spinner.
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                    // Define o adaptador no Spinner para exibir os nomes dos barris.
                    spinnerBarris.adapter = adapter
                }
                .addOnFailureListener { exception ->
                    // Se ocorrer um erro ao buscar os barris, exibe um Toast com a mensagem de erro.
                    Toast.makeText(context, "Erro ao carregar barris: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Se o usuário não estiver autenticado, loga uma mensagem.
            Log.d("EditarClienteController", "Usuário não autenticado.")
        }
    }

    // Função para validar os campos de entrada do cliente antes de enviar os dados para o Firestore.
    fun validarCampos(nome: String, cpf: String, endereco: String): Boolean {
        // Verifica se algum campo está vazio.
        if (nome.isEmpty() || cpf.isEmpty() || endereco.isEmpty()) {
            Toast.makeText(context, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verifica se o nome tem mais de 100 caracteres.
        if (nome.length > 100) {
            Toast.makeText(context, "Nome deve ter no máximo 100 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verifica se o CPF tem 11 caracteres.
        if (cpf.length != 11) {
            Toast.makeText(context, "CPF deve ter 11 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verifica se o endereço tem mais de 100 caracteres.
        if (endereco.length > 100) {
            Toast.makeText(context, "Endereço deve ter no máximo 100 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Se todas as validações passarem, retorna verdadeiro.
        return true
    }

    // Função para atualizar os dados de um cliente no Firestore.
    fun atualizarCliente(
        clienteId: String,  // ID do cliente que será atualizado
        nome: String,       // Novo nome do cliente
        cpf: String,        // Novo CPF do cliente
        endereco: String,   // Novo endereço do cliente
        callback: (Boolean) -> Unit  // Função de callback que retorna se a atualização foi bem-sucedida ou não
    ) {
        // Referência ao documento do cliente no Firestore.
        val clienteRef = firestore.collection("clientes").document(clienteId)

        // Dados do cliente a serem atualizados.
        val clienteData: Map<String, Any> = mapOf(
            "nome" to nome,  // Atualiza o nome do cliente
            "cpf" to cpf,    // Atualiza o CPF do cliente
            "endereco" to endereco  // Atualiza o endereço do cliente
        )

        // Atualiza os dados do cliente no Firestore.
        clienteRef.update(clienteData)
            .addOnSuccessListener {

                // Se a atualização for bem-sucedida, loga uma mensagem e chama o callback com 'true'.
                Log.d("EditarClienteController", "Cliente atualizado com sucesso.")
                callback(true)  // Informa que a atualização foi bem-sucedida
            }
            .addOnFailureListener { exception ->

                // Se ocorrer um erro na atualização, loga a mensagem de erro e chama o callback com 'false'.
                Log.e("EditarClienteController", "Erro ao atualizar cliente: ${exception.message}")
                callback(false)  // Informa que a atualização falhou
            }
    }

    // Função para deletar um cliente no Firestore.
    fun deletarCliente(clienteId: String, callback: (Boolean) -> Unit) {

        // Referência ao documento do cliente no Firestore.
        val clienteRef = firestore.collection("clientes").document(clienteId)

        // Deleta o documento do cliente no Firestore.
        clienteRef.delete()
            .addOnSuccessListener {

                // Se a exclusão for bem-sucedida, loga uma mensagem e chama o callback com 'true'.
                Log.d("EditarClienteController", "Cliente deletado com sucesso.")
                callback(true)  // Informa que a exclusão foi bem-sucedida
            }
            .addOnFailureListener { exception ->

                // Se ocorrer um erro na exclusão, loga a mensagem de erro e chama o callback com 'false'.
                Log.e("EditarClienteController", "Erro ao deletar cliente: ${exception.message}")
                callback(false)  // Informa que a exclusão falhou
            }
    }

    // Função para retornar a lista de barris.
    fun getListaBarris() = listaBarris
}
