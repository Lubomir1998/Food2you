package com.example.food2you.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.food2you.data.remote.models.FoodItem
import com.example.food2you.databinding.PostOrderItemBinding

class PostOrderAdapter(private val list: List<FoodItem>): RecyclerView.Adapter<PostOrderAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = PostOrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val food = list[position]

        holder.foodNameTextView.text = food.name
        holder.foodPriceTextView.text = formattedStringPrice(food.price.toString()) + " €"
        holder.quantityTextView.text = food.quantity.toString()

    }

    override fun getItemCount(): Int = list.size


    fun formattedStringPrice(price: String): String {

        val stotinki: String

        if(price.contains(".")) {
            val leva = price.split(".")[0]
            stotinki = price.split(".")[1]

            if(stotinki.length == 1) {
                return "${leva}.${stotinki}0"
            }
            return price
        }

        return "${price}.00"
    }

    class MyViewHolder(itemView: PostOrderItemBinding): RecyclerView.ViewHolder(itemView.root) {
        val foodNameTextView = itemView.foodNameTextView
        val foodPriceTextView = itemView.foodPriceTextView
        val quantityTextView = itemView.quantityTextView
    }
}