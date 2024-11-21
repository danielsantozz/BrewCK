import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brewck.R
import com.example.brewck.models.Barril

// Adapter para exibir uma lista de barris no RecyclerView
class BarrilAdapter(
    private var barris: List<Barril>, // Lista de barris a serem exibidos
    private val clickListener: (Barril) -> Unit // Função de callback para quando um barril for clicado
) : RecyclerView.Adapter<BarrilAdapter.BarrilViewHolder>() {

    // ViewHolder para os itens do RecyclerView
    class BarrilViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Referências para os componentes da interface do item de lista
        val nomeBarril: TextView = itemView.findViewById(R.id.txtNomeBarril)
        val capacidadeBarril: TextView = itemView.findViewById(R.id.txtBarrilCapacidade)
        val propriedadeBarril: TextView = itemView.findViewById(R.id.txtBarrilPropriedade)
        val statusBarril: TextView = itemView.findViewById(R.id.txtBarrilStatus)
        val liquidoBarril: TextView = itemView.findViewById(R.id.txtBarrilLiquido)
        val imgBarril: ImageView = itemView.findViewById(R.id.imgBarril)
        val imgFavorite: ImageView = itemView.findViewById(R.id.imgFavorite)
    }

    // Criação de uma nova instância do ViewHolder para o item do RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarrilViewHolder {
        // Infla o layout para o item do RecyclerView
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.cardbarril, parent, false)
        return BarrilViewHolder(itemView) // Retorna a nova instância do ViewHolder
    }

    // Vincula os dados do barril ao ViewHolder
    override fun onBindViewHolder(holder: BarrilViewHolder, position: Int) {
        // Obtém o barril na posição atual
        val barril = barris[position]

        // Preenche os campos do ViewHolder com as informações do barril
        holder.nomeBarril.text = barril.nome
        holder.capacidadeBarril.text = barril.capacidade.toString()
        holder.propriedadeBarril.text = barril.propriedade
        holder.statusBarril.text = barril.status
        holder.liquidoBarril.text = barril.liquido

        // Atualiza a imagem do barril de acordo com o status
        when(barril.status) {
            "Cheio" -> holder.imgBarril.setImageResource(R.drawable.beerkeg_black) // Imagem para barril cheio
            "No Cliente" -> holder.imgBarril.setImageResource(R.drawable.beerkegnocliente) // Imagem para barril no cliente
            "Sujo" -> holder.imgBarril.setImageResource(R.drawable.beerkegsujo) // Imagem para barril sujo
            "Limpo" -> holder.imgBarril.setImageResource(R.drawable.beerkeglimpo) // Imagem para barril limpo
        }

        // Verifica se o barril é favoritado e mostra ou esconde o ícone de favorito
        when (barril.isFavorite) {
            true -> holder.imgFavorite.visibility = View.VISIBLE // Exibe o ícone se for favorito
            else -> holder.imgFavorite.visibility = View.GONE // Oculta o ícone se não for favorito
        }

        // Define o que acontece quando o item é clicado
        holder.itemView.setOnClickListener {
            clickListener(barril) // Chama a função de callback passando o barril clicado
        }
    }

    // Função para atualizar a lista de barris
    fun updateBarris(newBarris: List<Barril>) {
        // Atualiza a lista de barris e ordena por nome
        barris = newBarris.sortedBy { it.nome.toLowerCase() }
        notifyDataSetChanged() // Notifica o RecyclerView para atualizar a visualização
    }

    // Retorna o número de itens na lista de barris
    override fun getItemCount() = barris.size
}
