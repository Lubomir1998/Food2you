package com.example.food2you.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.food2you.R
import com.example.food2you.adapters.PostOrderAdapter
import com.example.food2you.databinding.PostOrderFragmentBinding

class PostOrderFragment: Fragment(R.layout.post_order_fragment) {

    private lateinit var binding: PostOrderFragmentBinding
    private val args: PostOrderFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = PostOrderFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val order = args.order
        val foodList = order.food

        binding.headTv.text = "You have ordered from ${args.restaurantName}"
        binding.orderIdTv.text = order.id

        binding.postOrderRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = PostOrderAdapter(foodList)
            setHasFixedSize(true)
        }

        binding.totalPriceOfOrderTv.text = formattedStringPrice(order.price.toString()) + " €"

        Glide
                .with(requireContext())
                .load(args.imgUrl)
                .into(binding.logoImg)




    }

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

}