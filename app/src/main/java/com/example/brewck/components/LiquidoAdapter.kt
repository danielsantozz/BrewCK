package com.example.brewck.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.brewck.R
import com.example.brewck.models.Liquido

class LiquidoAdapter(
    private var liquidosList: MutableList<Liquido>,
    private val onDeleteClicked: (Liquido) -> Unit
) : RecyclerView.Adapter<LiquidoAdapter.LiquidoViewHolder>() {

    class LiquidoViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val nomeTextView: TextView = view.findViewById(R.id.txtNomeLiquido)
        val imgTrash: ImageView = view.findViewById(R.id.imgTrash)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiquidoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardliquido, parent, false)
        return LiquidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: LiquidoViewHolder, position: Int) {
        val liquido = liquidosList[position]
        holder.nomeTextView.text = liquido.nome

        holder.imgTrash.setOnClickListener {
            onDeleteClicked(liquido)
        }
    }

    override fun getItemCount() = liquidosList.size

    fun updateLiquidos(newList: List<Liquido>) {
        liquidosList.clear()
        liquidosList.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeLiquido(liquido: Liquido) {
        val position = liquidosList.indexOf(liquido)
        if (position != -1) {
            liquidosList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
