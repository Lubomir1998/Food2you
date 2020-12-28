package com.example.food2you.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.food2you.data.local.entities.Restaurant
import com.example.food2you.databinding.RestaurantItemBinding

class RestaurantAdapter(var listOfRestaurants: List<Restaurant>, private val context: Context, private val listener: OnRestaurantClickListener): RecyclerView.Adapter<RestaurantAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = RestaurantItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val restaurant = listOfRestaurants[position]

        holder.nameTextView.text = restaurant.name
        holder.deliveryTimeTextView.text = "${restaurant.deliveryTimeMinutes} min"

        holder.deliveryPriceTextView.text = if(restaurant.deliveryPrice == 0f) {
            "FREE"
        }
        else {
            "${formattedStringPrice(restaurant.deliveryPrice.toString())} EUR"
        }

        holder.minPriceTextView.text = "Minimum ${formattedStringPrice(restaurant.minimalPrice.toString())} EUR"


        Glide
            .with(context)
            .load(restaurant.imgUrl)
            .centerCrop()
            .into(holder.logoImage)


        holder.onRestaurantClicked(restaurant, listener)


    }


    override fun getItemCount(): Int = listOfRestaurants.size


    private fun formattedStringPrice(price: String): String {

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

    class MyViewHolder(itemView: RestaurantItemBinding): RecyclerView.ViewHolder(itemView.root) {

        val logoImage = itemView.logoImg
        val nameTextView = itemView.resTitleTextView
        val deliveryTimeTextView = itemView.deliveryTimeTextView
        val deliveryPriceTextView = itemView.deliveryPriceTextView
        val minPriceTextView = itemView.minPriceTextView

        fun onRestaurantClicked(restaurant: Restaurant, listener: OnRestaurantClickListener) {
            itemView.setOnClickListener {
                listener.onRestaurantClicked(restaurant)
            }
        }


    }


    interface OnRestaurantClickListener {
        fun onRestaurantClicked(restaurant: Restaurant)
    }

}