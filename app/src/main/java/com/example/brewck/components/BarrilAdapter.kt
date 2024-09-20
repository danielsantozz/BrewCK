import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brewck.R

data class Barril(val nome: String, val capacidade: Int, val propriedade: String, val status: String, val liquido: String, val isFavorite: Boolean)

class BarrilAdapter(
    private var barris: List<Barril>,
    private val clickListener: (Barril) -> Unit
) : RecyclerView.Adapter<BarrilAdapter.BarrilViewHolder>() {

    private val originalBarris: List<Barril> = barris.toList()

    class BarrilViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeBarril: TextView = itemView.findViewById(R.id.txtNomeBarril)
        val capacidadeBarril: TextView = itemView.findViewById(R.id.txtBarrilCapacidade)
        val propriedadeBarril: TextView = itemView.findViewById(R.id.txtBarrilPropriedade)
        val statusBarril: TextView = itemView.findViewById(R.id.txtBarrilStatus)
        val liquidoBarril: TextView = itemView.findViewById(R.id.txtBarrilLiquido)
        val imgBarril: ImageView = itemView.findViewById(R.id.imgBarril)
        val imgFavorite: ImageView = itemView.findViewById(R.id.imgFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarrilViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.cardbarril, parent, false)
        return BarrilViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BarrilViewHolder, position: Int) {
        val barril = barris[position]

        holder.nomeBarril.text = barril.nome
        holder.capacidadeBarril.text = barril.capacidade.toString()
        holder.propriedadeBarril.text = barril.propriedade
        holder.statusBarril.text = barril.status
        holder.liquidoBarril.text = barril.liquido

        when(barril.status) {
            "Cheio" -> holder.imgBarril.setImageResource(R.drawable.beerkeg_black)
            "No Cliente" -> holder.imgBarril.setImageResource(R.drawable.beerkegnocliente)
            "Sujo" -> holder.imgBarril.setImageResource(R.drawable.beerkegsujo)
            "Limpo" -> holder.imgBarril.setImageResource(R.drawable.beerkeglimpo)
        }

        when (barril.isFavorite) {
            true -> holder.imgFavorite.visibility = View.VISIBLE
            else -> holder.imgFavorite.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            clickListener(barril)
        }
    }

    fun updateBarris(newBarris: List<Barril>) {
        barris = newBarris
        notifyDataSetChanged()
    }

    override fun getItemCount() = barris.size

    fun getOriginalBarris(): List<Barril> = originalBarris
}
