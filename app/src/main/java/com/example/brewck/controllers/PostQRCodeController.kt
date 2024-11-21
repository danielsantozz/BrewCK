package com.example.brewck.controllers

import android.util.Log
import android.widget.Toast
import com.example.brewck.PostQRCode
import com.example.brewck.R
import com.example.brewck.models.Barril
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PostQRCodeController(private val activity: PostQRCode) {
    // Instância do FirebaseFirestore para interação com o Firestore
    private val firestore = FirebaseFirestore.getInstance()

    // Função para buscar os dados do barril com base no ID
    fun buscarBarrilPorId(barrilId: String, onResult: (Barril?) -> Unit) {
        val docRef = firestore.collection("barris").document(barrilId)

        docRef.get()
            .addOnSuccessListener { document ->
                // Verifica se o documento existe
                if (document != null && document.exists()) {
                    val nome = document.getString("nome") ?: "Barril não encontrado"
                    val capacidade = document.getLong("capacidade")?.toInt() ?: 0
                    val propriedade = document.getString("propriedade") ?: "Barril não encontrado"
                    val status = document.getString("status") ?: "Desconhecido"
                    val liquido = document.getString("liquido") ?: "Nenhum"
                    val isFavorite = document.getBoolean("isFavorite") ?: false
                    val cliente = document.getString("cliente") ?: ""

                    // Retorna o barril encontrado
                    onResult(Barril(barrilId, nome, capacidade, propriedade, status, liquido, isFavorite, cliente))
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { exception ->
                // Log de erro caso falhe ao buscar o barril
                Log.w("PostQRCode", "Erro ao buscar barril: ", exception)
                onResult(null)
            }
    }

    // Função para atualizar o status do barril
    fun atualizarStatusBarril(barrilId: String, nomeBarril: String, status: String, statusNovo: String, onResult: (Boolean) -> Unit) {
        val docRef = firestore.collection("barris").document(barrilId)

        // Se o barril estiver "No Cliente", remove o cliente associado antes de atualizar
        if (status == "No Cliente") {
            removerBarrilCliente(nomeBarril) {
                // Atualiza o status do barril
                val data = hashMapOf<String, Any>(
                    "status" to statusNovo,
                    "liquido" to "Nenhum",
                    "cliente" to ""
                )
                docRef.update(data)
                    .addOnSuccessListener {
                        Toast.makeText(activity, "Status do barril atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                        onResult(true)
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(activity, "Erro ao atualizar barril: ${exception.message}", Toast.LENGTH_LONG).show()
                        onResult(false)
                    }
            }
        } else {
            // Se o barril não estiver "No Cliente", apenas atualiza o status e líquido
            val data = hashMapOf<String, Any>(
                "status" to statusNovo,
                "liquido" to "Nenhum"
            )
            docRef.update(data)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Status do barril atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    onResult(true)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity, "Erro ao atualizar barril: ${exception.message}", Toast.LENGTH_LONG).show()
                    onResult(false)
                }
        }
    }

    // Função para atualizar a interface do usuário com os dados do barril
    fun atualizarUI(barril: Barril?) {
        barril?.let {
            activity.txtNome.text = it.nome
            activity.txtCapacidade.text = "${it.capacidade} L"
            activity.txtStatus.text = it.status

            // Define a imagem do barril com base no status
            val imagemId = when (it.status) {
                "Cheio" -> R.drawable.beerkeg_black
                "No Cliente" -> R.drawable.beerkegnocliente
                "Sujo" -> R.drawable.beerkegsujo
                "Limpo" -> R.drawable.beerkeglimpo
                else -> R.drawable.beerkeg_black
            }
            activity.imgBarril.setImageResource(imagemId)
        } ?: run {
            Toast.makeText(activity, "Barril não encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    // Função para buscar os líquidos disponíveis
    fun buscarLiquidos(onResult: (List<String>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email

        if (email != null) {
            firestore.collection("liquidos")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    // Retorna uma lista com os nomes dos líquidos
                    val liquidos = documents.map { it.getString("nome") ?: "" }
                    onResult(liquidos)
                }
                .addOnFailureListener { e ->
                    // Log de erro ao buscar os líquidos
                    Log.e("PostQRCode", "Erro ao buscar líquidos: ${e.message}", e)
                    onResult(emptyList())
                }
        } else {
            Toast.makeText(activity, "Usuário não logado", Toast.LENGTH_SHORT).show()
            onResult(emptyList())
        }
    }

    // Função para atualizar o líquido de um barril
    fun atualizarLiquidoDoBarril(barrilId: String, nomeBarril: String, liquido: String, status: String) {
        val docRef = FirebaseFirestore.getInstance().collection("barris").document(barrilId)

        if (status == "No Cliente") {
            removerBarrilCliente(nomeBarril) {
                val data = hashMapOf<String, Any>("liquido" to liquido, "status" to "Cheio", "cliente" to "")

                docRef.update(data)
                    .addOnSuccessListener {
                        Toast.makeText(activity, "Barril atualizado com o líquido $liquido", Toast.LENGTH_SHORT).show()
                        buscarBarrilPorId(barrilId) { atualizarUI(it) }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(activity, "Erro ao atualizar barril: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
            }
        } else {
            val data = hashMapOf<String, Any>("liquido" to liquido, "status" to "Cheio", "cliente" to "")

            docRef.update(data)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Barril atualizado com o líquido $liquido", Toast.LENGTH_SHORT).show()
                    buscarBarrilPorId(barrilId) { atualizarUI(it) }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity, "Erro ao atualizar barril: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    // Função para buscar os clientes cadastrados
    fun buscarClientes(onResult: (List<String>) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val email = currentUser.email
            firestore.collection("clientes")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    // Retorna a lista de nomes dos clientes
                    val clientes = querySnapshot.documents.mapNotNull { it.getString("nome") }
                    onResult(clientes)
                }
                .addOnFailureListener { exception ->
                    // Log de erro ao buscar os clientes
                    Log.e("PostQRCode", "Erro ao buscar clientes: ${exception.message}", exception)
                    onResult(emptyList())
                }
        } else {
            Toast.makeText(activity, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            onResult(emptyList())
        }
    }

    // Função para atualizar o cliente associado a um barril
    fun atualizarClienteNoBarril(barrilId: String, nomeCliente: String) {
        val docRef = firestore.collection("barris").document(barrilId)

        val data = hashMapOf<String, Any>(
            "cliente" to nomeCliente,
            "status" to "No Cliente"
        )

        docRef.update(data)
            .addOnSuccessListener {
                Toast.makeText(activity, "Barril vendido para o cliente $nomeCliente", Toast.LENGTH_SHORT).show()
                buscarBarrilPorId(barrilId) { atualizarUI(it) }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(activity, "Erro ao atualizar barril: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Função para atualizar o barril na coleção de clientes
    fun atualizarBarrilNoCliente(nomeCliente: String, nomeBarril: String) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("clientes")
            .whereEqualTo("nome", nomeCliente)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val clienteDoc = querySnapshot.documents[0]
                    val clienteId = clienteDoc.id

                    val clienteRef = firestore.collection("clientes").document(clienteId)
                    clienteRef.update("barril", nomeBarril)
                        .addOnSuccessListener {
                            Log.d("PostQRCode", "Barril associado ao cliente $nomeCliente")
                        }
                        .addOnFailureListener { exception ->
                            Log.w("PostQRCode", "Erro ao associar barril ao cliente: ${exception.message}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("PostQRCode", "Erro ao atualizar barril no cliente: ${exception.message}")
            }
    }

    // Função para remover um barril do cliente
    private fun removerBarrilCliente(nomeBarril: String, onResult: () -> Unit) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("clientes")
            .whereEqualTo("barril", nomeBarril)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val clienteDoc = querySnapshot.documents[0]
                    val clienteId = clienteDoc.id
                    val clienteRef = firestore.collection("clientes").document(clienteId)

                    clienteRef.update("barril", FieldValue.delete())
                        .addOnSuccessListener {
                            Log.d("PostQRCode", "Barril $nomeBarril removido do cliente")
                            onResult()
                        }
                        .addOnFailureListener { exception ->
                            Log.w("PostQRCode", "Erro ao remover barril do cliente: ${exception.message}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("PostQRCode", "Erro ao remover barril do cliente: ${exception.message}")
            }
    }
}
