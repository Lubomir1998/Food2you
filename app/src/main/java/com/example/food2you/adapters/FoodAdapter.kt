package com.example.food2you.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.food2you.data.local.entities.Food
import com.example.food2you.databinding.FoodItemBinding

class FoodAdapter(private val context: Context, private val listener: OnFoodClickListener): RecyclerView.Adapter<FoodAdapter.MyViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Food>() {
        override fun areItemsTheSame(oldItem: Food, newItem: Food): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Food, newItem: Food): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var meals: List<Food>
        get() = differ.currentList
        set(value) = differ.submitList(value)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = FoodItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodAdapter.MyViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val food = meals[position]

        holder.nameTextView.text = food.name
        holder.descriptionTextView.text = food.description

        holder.priceTextView.text = "${formattedStringPrice(food.price.toString())} EUR"

        Glide
            .with(context)
            .load(food.imgUrl)
            .into(holder.foodImg)


        holder.onFoodClicked(food, listener)

    }

    override fun getItemCount(): Int = meals.size


//    fun formattedStringPrice(price: String): String {
//
//        val stotinki: String
//
//        if(price.contains(".")) {
//            val leva = price.split(".")[0]
//            stotinki = price.split(".")[1]
//
//            if(stotinki.length == 1) {
//                return "${leva}.${stotinki}0"
//            }
//            return price
//        }
//
//        return "${price}.00"
//    }

    class MyViewHolder(itemView: FoodItemBinding): RecyclerView.ViewHolder(itemView.root) {
        val nameTextView = itemView.nameTextView
        val descriptionTextView = itemView.descriptionTextView
        val priceTextView = itemView.priceTextView
        val foodImg = itemView.foodImg

        fun onFoodClicked(food: Food, listener: OnFoodClickListener) {
            itemView.setOnClickListener {
                listener.onFoodClicked(food)
            }
        }


    }

    interface OnFoodClickListener {
        fun onFoodClicked(food: Food)
    }

}