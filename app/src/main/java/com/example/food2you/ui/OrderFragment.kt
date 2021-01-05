package com.example.food2you.ui

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.food2you.R
import com.example.food2you.adapters.OrderAdapter
import com.example.food2you.data.remote.models.FoodItem
import com.example.food2you.databinding.OrderFragmentBinding
import com.example.food2you.other.Constants.KEY_ADDRESS
import dagger.hilt.android.AndroidEntryPoint
import java.math.RoundingMode
import javax.inject.Inject

@AndroidEntryPoint
class OrderFragment: Fragment(R.layout.order_fragment) {

    private lateinit var binding: OrderFragmentBinding
    private val args: OrderFragmentArgs by navArgs()
    var list = mutableListOf<String>()
    private lateinit var orderAdapter: OrderAdapter

    @Inject lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = OrderFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        orderAdapter = OrderAdapter(listOf())

        setUpRecyclerView()

        val list: MutableList<FoodItem> = fillFoodList(args.FoodItems.toList())

        displayData(list)


        val orderPrice = args.OrderPrice.toBigDecimal().setScale(2, RoundingMode.FLOOR).toString()
        val deliveryPrice = args.deliveryPrice.toBigDecimal().setScale(2, RoundingMode.FLOOR).toFloat()

        showPrices(orderPrice, deliveryPrice)


        val address = sharedPrefs.getString(KEY_ADDRESS, "") ?: ""
        binding.addressTv.setText(address)


        showWarningMessageIfPriceNotEnough()



        binding.closeImg.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.orderFragment, true)
                .setPopUpTo(R.id.detailRestaurantFragment, true)
                .build()
            findNavController().navigate(R.id.action_orderFragment_to_detailRestaurantFragment, savedInstanceState, navOptions)
        }

    }


    private fun fillFoodList(list: List<FoodItem>): MutableList<FoodItem> {
        val map: Map<FoodItem, Int> = list.groupingBy {
            it
        }.eachCount().filter {
            it.value > 0
        }

        val foodItemList = mutableListOf<FoodItem>()

        for(entry in map) {
            val item = entry.key
            val quantity = entry.value

            val foodItem = FoodItem(item.name, quantity * item.price, quantity)

            foodItemList.add(foodItem)
        }

        return foodItemList
    }

    @SuppressLint("SetTextI18n")
    private fun showWarningMessageIfPriceNotEnough() {
        if(args.OrderPrice < args.minimumPrice) {
            binding.warningMessage.visibility = View.VISIBLE
            binding.messageTv.visibility = View.VISIBLE

            binding.remainingSumTv.text = (args.minimumPrice - args.OrderPrice).toBigDecimal().setScale(2, RoundingMode.FLOOR).toString() + " €"
            binding.messageTv.text =
                    "You can't order. ${args.restaurantName} delivers food for a minimum of ${orderAdapter.formattedStringPrice(args.minimumPrice.toString())}€ without the price of the delivery."

        }
        else {
            binding.warningMessage.visibility = View.GONE
            binding.messageTv.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showPrices(orderPrice: String, deliveryPrice: Float) {
        binding.totalPriceTv.text = orderAdapter.formattedStringPrice(orderPrice) + " €"
        binding.deliveryPriceTv.text = if(deliveryPrice > 0) {
            orderAdapter.formattedStringPrice(deliveryPrice.toString()) + " €"
        }
        else {
            "FREE"
        }
        val total = args.OrderPrice + args.deliveryPrice
        binding.totalTv.text = orderAdapter.formattedStringPrice(total.toString()) + " €"
    }

    private fun displayData(list: List<FoodItem>) {
        orderAdapter.list = list
        orderAdapter.notifyDataSetChanged()
    }

    private fun setUpRecyclerView() {
        binding.orderRecyclerView.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

}