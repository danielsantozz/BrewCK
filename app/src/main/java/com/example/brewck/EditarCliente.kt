package com.example.brewck

import Barril
import FirebaseRepository
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class EditarCliente : AppCompatActivity() {
    private lateinit var edtClienteNome : EditText
    private lateinit var edtClienteCPF : EditText
    private lateinit var edtClienteEndereco : EditText
    private lateinit var btnVoltarCliente : Button
    private lateinit var btnEditarCliente : Button
    private lateinit var btnDeletarCliente : Button
    private lateinit var spinnerBarris: Spinner
    private var listaBarris = mutableListOf<Pair<String, Barril>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_cliente)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val nome = intent.getStringExtra("nome")
        val cpf = intent.getStringExtra("cpf")
        val barril = intent.getStringExtra("barril")
        val endereco = intent.getStringExtra("endereco")
        val avaliacao = intent.getStringExtra("avaliacao")

        edtClienteNome = findViewById(R.id.edtClienteNome)
        edtClienteCPF = findViewById(R.id.edtClienteCPF)
        spinnerBarris = findViewById(R.id.spinnerBarris)
        edtClienteEndereco = findViewById(R.id.edtClienteEndereço)

        edtClienteNome.setText(nome.toString())
        edtClienteCPF.setText(cpf.toString())
        edtClienteEndereco.setText(endereco.toString())

        btnVoltarCliente = findViewById(R.id.btnVoltarEdtCliente)
        btnEditarCliente = findViewById(R.id.btnEditarCliente)
        btnDeletarCliente = findViewById(R.id.btnDeletarCliente)

        btnVoltarCliente.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        spinnerBarris = findViewById(R.id.spinnerBarris)

        buscarBarrilPorEmail()

        btnEditarCliente.setOnClickListener {
            atualizarCliente()
        }

        btnDeletarCliente.setOnClickListener {
            excluirCliente()
        }
    }

    private fun validarCampos(): Boolean {
        val nome = edtClienteNome.text.toString().trim()
        val cpf = edtClienteCPF.text.toString().trim()
        val endereco = edtClienteEndereco.text.toString().trim()

        if (nome.isEmpty() || cpf.isEmpty() || endereco.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (nome.length > 100) {
            Toast.makeText(this, "Nome deve ter no máximo 100 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (cpf.length != 11) {
            Toast.makeText(this, "CPF deve ter 11 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (endereco.length > 100) {
            Toast.makeText(this, "Capacidade deve ter no máximo 4 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun atualizarCliente() {
        if (!validarCampos()) {
            return
        }

        val firebaseRepository = FirebaseRepository()

        val id = intent.getStringExtra("id").toString()
        val newNome = edtClienteNome.text.toString()
        val newCPF = edtClienteCPF.text.toString()
        val newEndereco = edtClienteEndereco.text.toString()

        // Obtém o barril selecionado no Spinner
        val barrilSelecionado = spinnerBarris.selectedItem.toString()

        firebaseRepository.atualizarCliente(id, newNome, newCPF, barrilSelecionado, newEndereco) { sucesso ->
            if (sucesso) {
                Toast.makeText(this, "Cliente atualizado com sucesso", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erro ao atualizar cliente", Toast.LENGTH_SHORT).show()
            }
        }

        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun excluirCliente() {
        val firebaseRepository = FirebaseRepository()
        val id = intent.getStringExtra("id").toString()

        firebaseRepository.deletarCliente(id) { sucesso ->
            if (sucesso) {
                Toast.makeText(this, "Cliente excluído com sucesso", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erro ao excluir cliente", Toast.LENGTH_SHORT).show()
            }
        }

        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun buscarBarrilPorEmail() {
        FirebaseRepository().pegarEmail { email ->
            if (email != null) {
                val firestore = FirebaseFirestore.getInstance()
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
                            listaBarris.add(id to Barril(nome, capacidade, propriedade, status, liquido, false))
                        }

                        if (listaBarris.isEmpty()) {
                            Toast.makeText(this, "Nenhum barril encontrado.", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }

                        val barrilNomes = listaBarris.map { it.second.nome }
                        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, barrilNomes)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerBarris.adapter = adapter

                        // Preencher o Spinner com o barril atual do cliente (se houver)
                        val barrilAtual = intent.getStringExtra("barril")
                        val position = barrilNomes.indexOf(barrilAtual)
                        if (position >= 0) {
                            spinnerBarris.setSelection(position)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Erro ao carregar barris: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}