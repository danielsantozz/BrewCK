package com.example.brewck.controllers

import android.util.Log
import android.widget.Toast
import com.example.brewck.PostQRCode
import com.example.brewck.R
import com.example.brewck.models.Barril
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PostQRCodeController(private val activity: PostQRCode) {
    private val firestore = FirebaseFirestore.getInstance()

    fun buscarBarrilPorId(barrilId: String, onResult: (Barril?) -> Unit) {
        val docRef = firestore.collection("barris").document(barrilId)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val nome = document.getString("nome") ?: "Barril não encontrado"
                    val capacidade = document.getLong("capacidade")?.toInt() ?: 0
                    val propriedade = document.getString("propriedade") ?: "Barril não encontrado"
                    val status = document.getString("status") ?: "Desconhecido"
                    val liquido = document.getString("liquido") ?: "Nenhum"
                    val isFavorite = document.getBoolean("isFavorite") ?: false
                    val cliente = document.getString("cliente") ?: ""

                    onResult(Barril(barrilId, nome, capacidade, propriedade, status, liquido, isFavorite, cliente))
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("PostQRCode", "Erro ao buscar barril: ", exception)
                onResult(null)
            }
    }

    fun atualizarStatus(barrilId: String, status: String, onResult: (Boolean) -> Unit) {
        val docRef = firestore.collection("barris").document(barrilId)

        if (status == "Sujo") {
            val data = hashMapOf<String, Any>(
                "status" to status,
                "liquido" to "Nenhum"
            )
            docRef.update(data)
                .addOnSuccessListener {
                    onResult(true)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity, "Erro ao atualizar barril: ${exception.message}", Toast.LENGTH_LONG).show()
                    onResult(false)
                }
        }
        else {
            val data = hashMapOf<String, Any>("status" to status)
            docRef.update(data)
                .addOnSuccessListener {
                    onResult(true)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity, "Erro ao atualizar barril: ${exception.message}", Toast.LENGTH_LONG).show()
                    onResult(false)
                }
        }
    }

    fun atualizarUI(barril: Barril?) {
        barril?.let {
            activity.txtNome.text = it.nome
            activity.txtCapacidade.text = "${it.capacidade} L"
            activity.txtStatus.text = it.status

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

    fun buscarLiquidos(onResult: (List<String>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email

        if (email != null) {
            firestore.collection("liquidos")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    val liquidos = documents.map { it.getString("nome") ?: "" }
                    onResult(liquidos)
                }
                .addOnFailureListener { e ->
                    Log.e("PostQRCode", "Erro ao buscar líquidos: ${e.message}", e)
                    onResult(emptyList())
                }
        } else {
            Toast.makeText(activity, "Usuário não logado", Toast.LENGTH_SHORT).show()
            onResult(emptyList())
        }
    }

    fun atualizarLiquidoDoBarril(barrilId: String, liquido: String) {
        val docRef = FirebaseFirestore.getInstance().collection("barris").document(barrilId)

        val data = hashMapOf<String, Any>("liquido" to liquido, "status" to "Cheio")

        docRef.update(data)
            .addOnSuccessListener {
                Toast.makeText(activity, "Barril atualizado com o líquido $liquido", Toast.LENGTH_SHORT).show()
                buscarBarrilPorId(barrilId) { atualizarUI(it) }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(activity, "Erro ao atualizar barril: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    fun buscarClientes(onResult: (List<String>) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val email = currentUser.email
            firestore.collection("clientes")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val clientes = querySnapshot.documents.mapNotNull { it.getString("nome") }
                    onResult(clientes)
                }
                .addOnFailureListener { exception ->
                    Log.e("PostQRCode", "Erro ao buscar clientes: ${exception.message}", exception)
                    onResult(emptyList())
                }
        } else {
            Toast.makeText(activity, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            onResult(emptyList())
        }
    }

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

    fun atualizarBarrilNoCliente(nomeCliente: String, nomeBarril: String, barrilId: String) {
        firestore.collection("clientes")
            .whereEqualTo("nome", nomeCliente)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val clienteDoc = querySnapshot.documents[0]
                    val clienteId = clienteDoc.id

                    val data = hashMapOf<String, Any>(
                        "barril" to nomeBarril
                    )

                    firestore.collection("clientes").document(clienteId)
                        .update(data)
                        .addOnSuccessListener {
                            Toast.makeText(activity, "Cliente $nomeCliente atualizado com o barril $nomeBarril", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(activity, "Erro ao atualizar cliente: ${exception.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(activity, "Cliente não encontrado.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("PostQRCode", "Erro ao buscar cliente: ${exception.message}", exception)
            }
    }

}


