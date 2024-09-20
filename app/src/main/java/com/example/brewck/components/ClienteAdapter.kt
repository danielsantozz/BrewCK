import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.example.brewck.R

data class Cliente(val id: String, val nome: String, val cpf: String, val barril: String, val endereco: String, val avaliacao: String)

class ClienteAdapter(
    private var clientes: List<Cliente>,
    private val clickListener: (Cliente) -> Unit
) : RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

    // Instância do Firestore
    private val firestore = FirebaseFirestore.getInstance()

    class ClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeCliente: TextView = itemView.findViewById(R.id.txtClienteNome)
        val cpfCliente: TextView = itemView.findViewById(R.id.txtClienteCPF)
        val barrilCliente: TextView = itemView.findViewById(R.id.txtClienteStatus)
        val enderecoCliente: TextView = itemView.findViewById(R.id.txtClienteEndereco)
        val imgCliente: ImageView = itemView.findViewById(R.id.imgCliente)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.cardcliente, parent, false)
        return ClienteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = clientes[position]
        holder.nomeCliente.text = cliente.nome
        holder.cpfCliente.text = formatarCPF(cliente.cpf)
        holder.barrilCliente.text = cliente.barril
        holder.enderecoCliente.text = cliente.endereco

        // Definir a imagem de acordo com a avaliação
        when (cliente.avaliacao) {
            "Bom" -> holder.imgCliente.setImageResource(R.drawable.usergreen) // Icone para "Bom"
            "Ruim" -> holder.imgCliente.setImageResource(R.drawable.userred)   // Icone para "Ruim"
            else -> holder.imgCliente.setImageResource(R.drawable.user) // Ícone padrão
        }

        // Ação de clique no item
        holder.itemView.setOnClickListener {
            Log.d("ClienteAdapter", "Card clicado: ${cliente.nome}")
            clickListener(cliente)
        }
    }

    override fun getItemCount() = clientes.size

    // Método para atualizar a lista de clientes
    fun updateClientes(novosClientes: List<Cliente>) {
        this.clientes = novosClientes
        notifyDataSetChanged()
    }

    // Função para formatar o CPF
    fun formatarCPF(cpf: String): String {
        return if (cpf.length == 11) {
            cpf.substring(0, 3) + "." +
                    cpf.substring(3, 6) + "." +
                    cpf.substring(6, 9) + "-" +
                    cpf.substring(9)
        } else {
            cpf
        }
    }
}
