package com.example.food2you.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.food2you.data.remote.models.Order
import com.example.food2you.databinding.MyOrderItemBinding
import java.text.SimpleDateFormat
import java.util.*

class UserOrderAdapter(val context: Context, val listener: OnOrderClickListener): RecyclerView.Adapter<UserOrderAdapter.MyViewHolder>() {

    private val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<Order>(){
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    })

    var orders: List<Order>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = MyOrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val order = orders[position]

        holder.apply {
            addressTv.text = order.address
            priceTv.text = formattedStringPrice(order.price.toString()) + " EUR"

            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val date = dateFormat.format(order.timestamp)

            timestampTv.text = date

            Glide
                .with(context)
                .load(order.resImgUrl)
                .centerCrop()
                .into(resImage)

            updateBtn.visibility = if(order.status == "Waiting") {
                View.VISIBLE
            }
            else {
                View.GONE
            }

            restaurantNameTv.text = order.restaurantName

            onOrderClicked(listener, order)
            onUpdateBtnClicked(listener, order)
        }



    }

    override fun getItemCount(): Int = orders.size


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

    class MyViewHolder(itemView: MyOrderItemBinding): RecyclerView.ViewHolder(itemView.root) {
        val resImage = itemView.logoImg
        val restaurantNameTv = itemView.resTitleTextView
        val timestampTv = itemView.timestampTextView
        val addressTv = itemView.addressTextView
        val priceTv = itemView.totalPriceTextView
        val updateBtn = itemView.updateBtn

        fun onOrderClicked(listener: OnOrderClickListener, order: Order) {
            itemView.setOnClickListener {
                listener.onOrderClicked(order)
            }
        }

        fun onUpdateBtnClicked(listener: OnOrderClickListener, order: Order) {
            updateBtn.setOnClickListener {
                listener.onUpdateButtonClicked(order)
            }
        }

    }



    interface OnOrderClickListener {
        fun onOrderClicked(order: Order)
        fun onUpdateButtonClicked(order: Order)
    }
}