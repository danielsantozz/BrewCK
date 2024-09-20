import com.example.brewck.models.UserKegInfo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class FirebaseRepository {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    fun adicionarBarril(
        nome: String,
        email: String,
        capacidade: Int,
        propriedade: String,
        status: String,
        liquido: String,
        isFavorite: Boolean,
        callback: (Boolean) -> Unit
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val barrisCollection = firestore.collection("barris")

        val barrilData = hashMapOf(
            "nome" to nome,
            "email" to email,
            "capacidade" to capacidade,
            "propriedade" to propriedade,
            "status" to status,
            "isFavorite" to isFavorite,
            "liquido" to liquido
        )

        barrisCollection.add(barrilData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { exception ->
                println("Erro ao adicionar barril: ${exception.message}")
                callback(false)
            }
    }

    fun adicionarCliente(
        nome: String,
        email: String,
        cpf: String,
        barril: String,
        endereco: String,
        avaliacao: String,
        callback: (Boolean) -> Unit
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val clientesCollection = firestore.collection("clientes")

        val clienteData = hashMapOf(
            "nome" to nome,
            "email" to email,
            "cpf" to cpf,
            "barril" to barril,
            "endereco" to endereco,
            "avaliacao" to avaliacao
        )

        clientesCollection.add(clienteData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { exception ->
                println("Erro ao adicionar cliente: ${exception.message}")
                callback(false)
            }
    }

    fun criarUsuario(email: String, senha: String, nome: String, callback: (Boolean, String?) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Usuário criado com sucesso, agora adiciona informações adicionais no Firestore
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val userMap = hashMapOf(
                            "nome" to nome,
                            "email" to email
                        )
                        db.collection("users").document(userId).set(userMap)
                            .addOnSuccessListener {
                                callback(true, null)
                            }
                            .addOnFailureListener { e ->
                                callback(false, "Falha ao salvar informações adicionais: ${e.localizedMessage}")
                            }
                    } else {
                        callback(false, "Erro ao obter ID do usuário")
                    }
                } else {
                    task.exception?.let { exception ->
                        if (exception is FirebaseAuthUserCollisionException) {
                            callback(false, "Email já está em uso")
                        } else if (exception is FirebaseAuthWeakPasswordException) {
                            callback(false, "A senha é muito fraca.")
                        } else if (exception is FirebaseAuthInvalidCredentialsException)  {
                            callback(false, "O formato do email é inválido.")
                        } else {
                            callback(false, exception.localizedMessage)
                        }
                    } ?: callback(false, "Erro desconhecido")
                }
            }
    }

    fun fazerLogin(email: String, senha: String, callback: (Boolean, String?) -> Unit) {
        val auth = FirebaseAuth.getInstance()

        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    task.exception?.let { exception ->
                        if (exception is FirebaseAuthInvalidUserException) {
                            callback(false, "Email não encontrado.")
                        } else if (exception is FirebaseAuthInvalidCredentialsException) {
                            callback(false, "Senha incorreta.")
                        } else {
                            callback(false, exception.localizedMessage)
                        }
                    } ?: callback(false, "Erro desconhecido")
                }
            }
    }

    fun recuperarNomeDoUsuario(callback: (String?) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userDocRef = firestore.collection("users").document(userId)
            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val nome = documentSnapshot.getString("nome")
                        callback(nome)
                    } else {
                        callback(null)
                    }
                }
        } else {
            callback(null)
        }
    }

    fun buscarUserKegInfo(emailUsuario: String, callback: (UserKegInfo?) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val usersCollection = firestore.collection("users")

        usersCollection
            .whereEqualTo("email", emailUsuario)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        val bc = document.getLong("barrisCheios")?.toInt()
                        val bs = document.getLong("barrisSujos")?.toInt()
                        val bnc = document.getLong("barrisNoCliente")?.toInt()
                        val bl = document.getLong("barrisLimpos")?.toInt()

                        val userKegInfo = UserKegInfo(bc, bs, bnc, bl)
                        callback(userKegInfo)
                        return@addOnSuccessListener
                    }
                }
                callback(null)
            }
            .addOnFailureListener { exception ->
                println("Erro ao consultar Firestore: ${exception.message}")
                callback(null)
            }
    }

    fun pegarEmail(callback: (String?) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val email = currentUser.email
            callback(email)
        } else {
            callback(null)
        }
    }

    fun atualizarBarril(
        barrilId: String,
        nome: String,
        capacidade: Int,
        propriedade: String,
        status: String,
        liquido: String,
        callback: (Boolean) -> Unit
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val barrilRef = firestore.collection("barris").document(barrilId)

        val barrilData = hashMapOf<String, Any>(
            "nome" to nome,
            "capacidade" to capacidade,
            "propriedade" to propriedade,
            "status" to status,
            "liquido" to liquido
        )

        barrilRef.update(barrilData)
            .addOnSuccessListener {
                println("Barril atualizado com sucesso.")
                callback(true)
            }
            .addOnFailureListener { exception ->
                println("Erro ao atualizar barril: ${exception.message}")
                callback(false)
            }
    }

    fun deletarBarril(barrilId: String, callback: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val barrilRef = firestore.collection("barris").document(barrilId)

        barrilRef.delete()
            .addOnSuccessListener {
                println("Barril deletado com sucesso.")
                callback(true)
            }
            .addOnFailureListener { exception ->
                println("Erro ao deletar barril: ${exception.message}")
                callback(false)
            }
    }

    fun atualizarCliente(
        clienteId: String,
        nome: String,
        cpf: String,
        barril: String,
        endereco: String,
        callback: (Boolean) -> Unit
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val clienteRef = firestore.collection("clientes").document(clienteId)

        val clienteData = hashMapOf<String, Any>(
            "nome" to nome,
            "cpf" to cpf,
            "barril" to barril,
            "endereco" to endereco
        )

        clienteRef.update(clienteData)
            .addOnSuccessListener {
                println("Cliente atualizado com sucesso.")
                callback(true)
            }
            .addOnFailureListener { exception ->
                println("Erro ao atualizar cliente: ${exception.message}")
                callback(false)
            }
    }

    fun deletarCliente(clienteId: String, callback: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val barrilRef = firestore.collection("clientes").document(clienteId)

        barrilRef.delete()
            .addOnSuccessListener {
                println("Cliente deletado com sucesso.")
                callback(true)
            }
            .addOnFailureListener { exception ->
                println("Erro ao deletar cliente: ${exception.message}")
                callback(false)
            }
    }


}