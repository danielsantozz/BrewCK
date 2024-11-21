import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.example.brewck.R
import com.example.brewck.models.Cliente

// Adapter para exibir uma lista de clientes no RecyclerView
class ClienteAdapter(
    private var clientes: List<Cliente>, // Lista de clientes a serem exibidos
    private val clickListener: (Cliente) -> Unit // Função de callback para quando um cliente for clicado
) : RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

    // Instância do Firestore, utilizado para interações com o banco de dados Firebase
    private val firestore = FirebaseFirestore.getInstance()

    // ViewHolder para os itens do RecyclerView
    class ClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Referências para os componentes da interface do item de lista
        val nomeCliente: TextView = itemView.findViewById(R.id.txtClienteNome)
        val cpfCliente: TextView = itemView.findViewById(R.id.txtClienteCPF)
        val barrilCliente: TextView = itemView.findViewById(R.id.txtClienteStatus)
        val enderecoCliente: TextView = itemView.findViewById(R.id.txtClienteEndereco)
        val imgCliente: ImageView = itemView.findViewById(R.id.imgCliente)
    }

    // Criação de uma nova instância do ViewHolder para o item do RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        // Infla o layout para o item do RecyclerView
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.cardcliente, parent, false)
        return ClienteViewHolder(itemView) // Retorna a nova instância do ViewHolder
    }

    // Vincula os dados do cliente ao ViewHolder
    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = clientes[position] // Obtém o cliente na posição atual

        // Preenche os campos do ViewHolder com as informações do cliente
        holder.nomeCliente.text = cliente.nome
        holder.cpfCliente.text = formatarCPF(cliente.cpf) // Formata o CPF antes de exibir

        // Exibe o primeiro barril com reticências se houver mais de um
        val barrilArray = cliente.barril // Supõe que seja uma lista ou array de barris
        holder.barrilCliente.text = if (!barrilArray.isNullOrEmpty()) {
            if (barrilArray.size > 1) {
                "Mais de 1 barril" // Exibe mensagem se houver mais de um barril
            } else {
                barrilArray[0] // Exibe o primeiro barril
            }
        } else {
            "Nenhum barril" // Exibe mensagem se não houver barris
        }

        // Exibe o endereço do cliente
        holder.enderecoCliente.text = cliente.endereco

        // Define a imagem do cliente com base na avaliação
        when (cliente.avaliacao) {
            "Bom" -> holder.imgCliente.setImageResource(R.drawable.usergreen) // Imagem para avaliação "Bom"
            "Ruim" -> holder.imgCliente.setImageResource(R.drawable.userred) // Imagem para avaliação "Ruim"
            else -> holder.imgCliente.setImageResource(R.drawable.user) // Imagem padrão para avaliação "Indefinida"
        }

        // Define o que acontece quando o item é clicado
        holder.itemView.setOnClickListener {
            Log.d("ClienteAdapter", "Card clicado: ${cliente.nome}") // Log para debugging
            clickListener(cliente) // Chama a função de callback passando o cliente clicado
        }
    }

    // Retorna o número de itens na lista de clientes
    override fun getItemCount() = clientes.size

    // Função para atualizar a lista de clientes
    fun updateClientes(novosClientes: List<Cliente>) {
        this.clientes = novosClientes // Atualiza a lista de clientes
        notifyDataSetChanged() // Notifica o RecyclerView para atualizar a visualização
    }

    // Função para formatar o CPF de um cliente
    fun formatarCPF(cpf: String): String {
        return if (cpf.length == 11) { // Verifica se o CPF tem 11 caracteres
            // Formata o CPF no formato "xxx.xxx.xxx-xx"
            cpf.substring(0, 3) + "." +
                    cpf.substring(3, 6) + "." +
                    cpf.substring(6, 9) + "-" +
                    cpf.substring(9)
        } else {
            cpf // Retorna o CPF sem formatação se não tiver 11 caracteres
        }
    }
}
