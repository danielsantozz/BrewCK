package com.example.brewck

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class RecuperarConta : AppCompatActivity() {
    lateinit var btnVoltar : Button
    lateinit var edtEmail : EditText
    lateinit var btnRecuperar : TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recuperar_conta)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        edtEmail = findViewById(R.id.edtEmailRecuperacao)

        btnVoltar = findViewById(R.id.btnVoltarRecuperacao)
        btnVoltar.setOnClickListener {
            val intent2 = Intent(this, MainActivity::class.java)
            startActivity(intent2)
        }

        btnRecuperar = findViewById(R.id.btnRecuperarSenha)
        btnRecuperar.setOnClickListener {
                auth = FirebaseAuth.getInstance()

                val email = edtEmail.text.toString().trim()

                if (email.isEmpty()) {
                    Toast.makeText(this, "Por favor, insira seu e-mail", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "E-mail de recuperação enviado.", Toast.LENGTH_SHORT).show()
                            val intent2 = Intent(this, MainActivity::class.java)
                            startActivity(intent2)
                        } else {
                            Toast.makeText(this, "Erro ao enviar e-mail de recuperação.", Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }
}