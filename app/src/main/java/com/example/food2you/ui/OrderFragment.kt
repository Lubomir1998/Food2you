package com.example.food2you.ui

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.food2you.R
import com.example.food2you.adapters.OrderAdapter
import com.example.food2you.data.remote.models.FoodItem
import com.example.food2you.data.remote.models.Order
import com.example.food2you.databinding.OrderFragmentBinding
import com.example.food2you.notify.AlertReceiver
import com.example.food2you.other.Constants.KEY_ADDRESS
import com.example.food2you.other.Constants.KEY_TIMESTAMP
import com.example.food2you.other.Constants.KEY_PHONE
import com.example.food2you.other.Constants.KEY_RESTAURANT_ID
import com.example.food2you.other.Constants.KEY_TOKEN
import com.example.food2you.other.Status
import com.example.food2you.viewmodels.OrderViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.math.RoundingMode
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class OrderFragment: Fragment(R.layout.order_fragment) {

    private lateinit var binding: OrderFragmentBinding
    private val viewModel: OrderViewModel by viewModels()
    private val args: OrderFragmentArgs by navArgs()
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var listener: OrderAdapter.OnButtonClickListener
    var list: MutableList<FoodItem> = mutableListOf()
    var _list: MutableList<FoodItem> = mutableListOf()
    private var orderPrice: String = ""

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

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        listener = object : OrderAdapter.OnButtonClickListener {
            override fun minusClicked(foodItem: FoodItem) {
                val price = foodItem.price / foodItem.quantity
                val item = FoodItem(foodItem.name, price)

                _list.remove(item)
                goBackIfBasketIsEmpty(_list)
                val currentList = fillFoodList(_list)
                displayData(currentList)

                orderPrice = calculateOrderPrice(_list)
                showWarningMessageIfPriceNotEnough(orderPrice.toFloat())
                showPrices(orderPrice, args.deliveryPrice.toBigDecimal().setScale(2, RoundingMode.FLOOR).toFloat())
            }

            override fun plusClicked(foodItem: FoodItem) {
                val price = foodItem.price / foodItem.quantity
                val item = FoodItem(foodItem.name, price)

                _list.add(item)
                val currentList = fillFoodList(_list)
                displayData(currentList)

                orderPrice = calculateOrderPrice(_list)
                showWarningMessageIfPriceNotEnough(orderPrice.toFloat())
                showPrices(orderPrice, args.deliveryPrice.toBigDecimal().setScale(2, RoundingMode.FLOOR).toFloat())
            }
        }

        orderAdapter = OrderAdapter(listOf(), listener)

        setUpRecyclerView()

        list = fillFoodList(args.FoodItems.toList())
        _list = args.FoodItems.toMutableList()

        goBackIfBasketIsEmpty(_list)

        displayData(list)

        orderPrice = args.OrderPrice.toBigDecimal().setScale(2, RoundingMode.FLOOR).toString()
        val deliveryPrice = args.deliveryPrice.toBigDecimal().setScale(2, RoundingMode.FLOOR).toFloat()

        showPrices(orderPrice, deliveryPrice)


        val address = sharedPrefs.getString(KEY_ADDRESS, "") ?: ""
        binding.addressEt.setText(address)

        val phone = sharedPrefs.getLong(KEY_PHONE, 0L)
        if(phone != 0L) {
            binding.phoneEditText.setText(phone.toString())
        }

        showWarningMessageIfPriceNotEnough(orderPrice.toFloat())



        binding.orderBtn.setOnClickListener {
            if(binding.addressEt.text.toString().isNotEmpty() && binding.phoneEditText.text.toString().isNotEmpty()) {
                val dialog = MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Make order?")
                        .setMessage("Are you sure you want to make this order?")
                        .setPositiveButton("Yes") { _, _ ->
                            makeOrder()
                        }
                        .setNegativeButton("Cancel") { dialogInterface, _ ->
                            dialogInterface.cancel()
                        }
                        .create()

                dialog.show()
            }
            else {
                Snackbar.make(requireView(), "Empty fields", Snackbar.LENGTH_LONG).show()
            }
        }

        binding.closeImg.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.orderFragment, true)
                .setPopUpTo(R.id.detailRestaurantFragment, true)
                .build()

            val action = OrderFragmentDirections.actionOrderFragmentToDetailRestaurantFragment(args.restaurantId, args.restaurantName)

            findNavController().navigate(action, navOptions)
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
    private fun showWarningMessageIfPriceNotEnough(orderPrice: Float) {
        if(orderPrice < args.minimumPrice) {
            binding.warningMessage.visibility = View.VISIBLE
            binding.messageTv.visibility = View.VISIBLE
            binding.addressEtLayout.visibility = View.GONE
            binding.phoneEditTextLayout.visibility = View.GONE
            binding.orderBtn.visibility = View.GONE

            binding.remainingSumTv.text = (args.minimumPrice - orderPrice).toBigDecimal().setScale(2, RoundingMode.FLOOR).toString() + " €"
            binding.messageTv.text =
                    "You can't order. ${args.restaurantName} delivers food for a minimum of ${orderAdapter.formattedStringPrice(args.minimumPrice.toString())}€ without the price of the delivery."

        }
        else {
            binding.warningMessage.visibility = View.GONE
            binding.messageTv.visibility = View.GONE
            binding.addressEtLayout.visibility = View.VISIBLE
            binding.phoneEditTextLayout.visibility = View.VISIBLE
            binding.orderBtn.visibility = View.VISIBLE
        }
    }

    private fun calculateOrderPrice(list: MutableList<FoodItem>): String {
        var sum = 0f
        for(item in list) {
            sum += item.price
        }
        return sum.toBigDecimal().setScale(2, RoundingMode.FLOOR).toString()
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
        val total = orderPrice.toFloat() + args.deliveryPrice
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

    private fun goBackIfBasketIsEmpty(list: MutableList<FoodItem>) {
        if(list.isEmpty()) {
            val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.orderFragment, true)
                    .setPopUpTo(R.id.detailRestaurantFragment, true)
                    .build()

            val action = OrderFragmentDirections.actionOrderFragmentToDetailRestaurantFragment(args.restaurantId, args.restaurantName)
            findNavController().navigate(action, navOptions)
        }
    }

    private fun makeOrder() {
        val token = sharedPrefs.getString(KEY_TOKEN, "empty") ?: "empty"
        val order = Order(args.restaurantOwner, binding.addressEt.text.toString(), token, binding.phoneEditText.text.toString().toLong(), fillFoodList(_list), orderPrice.toFloat() + args.deliveryPrice, System.currentTimeMillis(), "Waiting")
        viewModel.order(order)
        subscribeToObservers(order)
    }

    private fun subscribeToObservers(order: Order) {
        viewModel.orderStatus.observe(viewLifecycleOwner, {
            it?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        binding.progressBar4.visibility = View.GONE

//                        val alarmManager: AlarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

//                        val intent = Intent(requireContext(), AlertReceiver::class.java).also { intent ->
//                            intent.putExtra(KEY_TIMESTAMP, order.timestamp)
//                            intent.putExtra(KEY_RESTAURANT_ID, args.restaurantId)
//                        }

//                        val pendingIntent = PendingIntent.getBroadcast(requireContext(), order.timestamp.toInt(), intent, 0)
//                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, order.timestamp + TimeUnit.MINUTES.toMillis(1), pendingIntent)


                        val action = OrderFragmentDirections.actionOrderFragmentToPostOrderFragment(order, args.restaurantName, args.restaurantImgUrl)
                        val navOptions = NavOptions.Builder()
                                .setPopUpTo(R.id.orderFragment, true)
                                .setPopUpTo(R.id.detailRestaurantFragment, true)
                                .build()

                        findNavController().navigate(action, navOptions)
                    }
                    Status.LOADING -> {
                        binding.progressBar4.visibility = View.VISIBLE
                    }
                    Status.ERROR -> {
                        binding.progressBar4.visibility = View.GONE
                        Snackbar.make(requireView(), "Something went wrong", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

}