package com.example.brewck.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brewck.R
import com.example.brewck.models.Liquido

// Adapter para exibir uma lista de líquidos no RecyclerView
class LiquidoAdapter(
    private var liquidosList: MutableList<Liquido>, // Lista mutável de líquidos a serem exibidos
    private val onDeleteClicked: (Liquido) -> Unit // Função de callback para deletar um líquido
) : RecyclerView.Adapter<LiquidoAdapter.LiquidoViewHolder>() {

    // ViewHolder para os itens do RecyclerView
    class LiquidoViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        // Referências para os componentes da interface do item de lista
        val nomeTextView: TextView = view.findViewById(R.id.txtNomeLiquido) // TextView para o nome do líquido
        val imgTrash: ImageView = view.findViewById(R.id.imgTrash) // Imagem do ícone de lixeira (para excluir)
    }

    // Criação de uma nova instância do ViewHolder para o item do RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiquidoViewHolder {
        // Infla o layout para o item do RecyclerView
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardliquido, parent, false)
        return LiquidoViewHolder(view) // Retorna a nova instância do ViewHolder
    }

    // Vincula os dados do líquido ao ViewHolder
    override fun onBindViewHolder(holder: LiquidoViewHolder, position: Int) {
        val liquido = liquidosList[position] // Obtém o líquido na posição atual
        holder.nomeTextView.text = liquido.nome // Define o nome do líquido no TextView

        // Define o que acontece quando o ícone de lixeira é clicado
        holder.imgTrash.setOnClickListener {
            onDeleteClicked(liquido) // Chama a função de callback para deletar o líquido
        }
    }

    // Retorna o número de itens na lista de líquidos
    override fun getItemCount() = liquidosList.size

    // Função para atualizar a lista de líquidos com uma nova lista
    fun updateLiquidos(newList: List<Liquido>) {
        liquidosList.clear() // Limpa a lista atual
        liquidosList.addAll(newList) // Adiciona todos os novos líquidos à lista
        notifyDataSetChanged() // Notifica o RecyclerView para atualizar a visualização
    }

    // Função para remover um líquido da lista
    fun removeLiquido(liquido: Liquido) {
        val position = liquidosList.indexOf(liquido) // Obtém a posição do líquido na lista
        if (position != -1) { // Se o líquido for encontrado na lista
            liquidosList.removeAt(position) // Remove o líquido da lista
            notifyItemRemoved(position) // Notifica o RecyclerView sobre a remoção
        }
    }
}
