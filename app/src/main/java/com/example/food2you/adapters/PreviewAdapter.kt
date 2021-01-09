package com.example.food2you.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.food2you.databinding.PreviewItemBinding

class PreviewAdapter(private val list: List<String>): RecyclerView.Adapter<PreviewAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = PreviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val preview = list[position]

        holder.textView.text = preview

    }

    override fun getItemCount(): Int = list.size

    class MyViewHolder(itemView: PreviewItemBinding): RecyclerView.ViewHolder(itemView.root) {
        val textView = itemView.previewTv
    }
}