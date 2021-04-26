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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.food2you.R
import com.example.food2you.adapters.OrderAdapter
import com.example.food2you.adapters.formattedStringPrice
import com.example.food2you.data.remote.NotificationData
import com.example.food2you.data.remote.PushNotification
import com.example.food2you.data.remote.models.FoodItem
import com.example.food2you.data.remote.models.Order
import com.example.food2you.databinding.OrderFragmentBinding
import com.example.food2you.notify.AlertReceiver
import com.example.food2you.other.Constants.KEY_ADDRESS
import com.example.food2you.other.Constants.KEY_EMAIL
import com.example.food2you.other.Constants.KEY_PHONE
import com.example.food2you.other.Constants.KEY_RESTAURANT_ID
import com.example.food2you.other.Constants.KEY_TIMESTAMP
import com.example.food2you.other.Constants.KEY_TOKEN
import com.example.food2you.other.Constants.NO_EMAIL
import com.example.food2you.other.Status
import com.example.food2you.viewmodels.OrderViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.consumesAll
import java.math.RoundingMode
import java.util.*
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
    private var currentFoodList: MutableList<FoodItem>? = null

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

                currentFoodList = fillFoodList(_list)

            }

            override fun plusClicked(foodItem: FoodItem) {

                if(args.currentOrder != null) {
                    Snackbar.make(requireView(), "You can't add meals from this page", Snackbar.LENGTH_LONG).show()
                    return
                }

                val price = foodItem.price / foodItem.quantity
                val item = FoodItem(foodItem.name, price)

                _list.add(item)
                val currentList = fillFoodList(_list)
                displayData(currentList)

                orderPrice = calculateOrderPrice(_list)
                showWarningMessageIfPriceNotEnough(orderPrice.toFloat())
                showPrices(orderPrice, args.deliveryPrice.toBigDecimal().setScale(2, RoundingMode.FLOOR).toFloat())

                currentFoodList = fillFoodList(_list)
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


        val address = if(args.orderAddress.isEmpty()) {
            sharedPrefs.getString(KEY_ADDRESS, "") ?: ""
        }
        else {
            args.orderAddress
        }
        binding.addressEt.setText(address)

        val phone = if(args.orderPhone.isNotEmpty()) {
            args.orderPhone
        }
        else {
            sharedPrefs.getString(KEY_PHONE, "") ?: ""
        }
        if(phone.isNotEmpty()) {
            binding.phoneEditText.setText(phone.toString())
        }

        showWarningMessageIfPriceNotEnough(orderPrice.toFloat())



        binding.orderBtn.setOnClickListener {
            if(binding.addressEt.text.toString().isNotEmpty() && binding.phoneEditText.text.toString().isNotEmpty()) {
                if(args.currentOrder != null  && args.currentOrder!!.food == fillFoodList(_list)) {
                    Snackbar.make(requireView(), "Order is not updated", Snackbar.LENGTH_LONG).show()
                }
                else {
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
            }
            else {
                Snackbar.make(requireView(), "Empty fields", Snackbar.LENGTH_LONG).show()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.orderFragment, true)
                        .setPopUpTo(R.id.detailRestaurantFragment, true)
                        .build()

                val phone1 = if(binding.phoneEditText.text.toString().isNotEmpty()) {
                    binding.phoneEditText.text.toString()
                }
                else {
                    ""
                }

                val id = if(args.orderId.isNotEmpty()) {
                    args.orderId
                } else {
                    UUID.randomUUID().toString()
                }

                val action = OrderFragmentDirections.actionOrderFragmentToDetailRestaurantFragment(args.restaurantId, args.restaurantName, Order(
                        args.restaurantOwner,
                        binding.addressEt.text.toString(),
                        sharedPrefs.getString(KEY_TOKEN, "empty") ?: "empty",
                        sharedPrefs.getString(KEY_EMAIL, NO_EMAIL) ?: NO_EMAIL,
                        phone1,
                        fillFoodList(_list),
                        orderPrice.toFloat() + args.deliveryPrice,
                        System.currentTimeMillis(),
                        "Waiting",
                        args.restaurantImgUrl,
                        args.restaurantName,
                        resId = args.restaurantId,
                        id = id
                )
                )

                findNavController().navigate(action, navOptions)
            }
        })


        binding.closeImg.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.orderFragment, true)
                .setPopUpTo(R.id.detailRestaurantFragment, true)
                .build()

            val phone1 = if(binding.phoneEditText.text.toString().isNotEmpty()) {
                binding.phoneEditText.text.toString()
            }
            else {
                ""
            }
            val id = if(args.orderId.isNotEmpty()) {
                args.orderId
            } else {
                UUID.randomUUID().toString()
            }

            val action = OrderFragmentDirections.actionOrderFragmentToDetailRestaurantFragment(args.restaurantId, args.restaurantName, Order(
                    args.restaurantOwner,
                    binding.addressEt.text.toString(),
                    sharedPrefs.getString(KEY_TOKEN, "empty") ?: "empty",
                    sharedPrefs.getString(KEY_EMAIL, NO_EMAIL) ?: NO_EMAIL,
                    phone1,
                    fillFoodList(_list),
                    orderPrice.toFloat() + args.deliveryPrice,
                    System.currentTimeMillis(),
                    "Waiting",
                    args.restaurantImgUrl,
                    args.restaurantName,
                    resId = args.restaurantId,
                    id = id
            )
            )

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
                    "You can't order. ${args.restaurantName} delivers food for a minimum of ${formattedStringPrice(args.minimumPrice.toString())}€ without the price of the delivery."

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
        binding.totalPriceTv.text = formattedStringPrice(orderPrice) + " €"
        binding.deliveryPriceTv.text = if(deliveryPrice > 0) {
            formattedStringPrice(deliveryPrice.toString()) + " €"
        }
        else {
            "FREE"
        }
        val total = orderPrice.toFloat() + args.deliveryPrice
        binding.totalTv.text = formattedStringPrice(total.toString()) + " €"
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
        val id = if(args.orderId.isNotEmpty()) {
            args.orderId
        } else {
            UUID.randomUUID().toString()
        }
        val order = Order(
                args.restaurantOwner,
                binding.addressEt.text.toString(),
                token,
                sharedPrefs.getString(KEY_EMAIL, NO_EMAIL) ?: NO_EMAIL,
                binding.phoneEditText.text.toString(),
                fillFoodList(_list),
                orderPrice.toFloat() + args.deliveryPrice,
                System.currentTimeMillis(),
                "Waiting",
                args.restaurantImgUrl,
                args.restaurantName,
                resId = args.restaurantId,
                id = id
        )
        viewModel.order(order)
        subscribeToObservers(order)
    }

    private fun sendPushNotificationToRestaurant() {
        val title = if(args.orderId.isNotEmpty()) {
            "Order Update"
        }
        else {
            "New order"
        }

        val message = if(args.orderId.isNotEmpty()) {
            "An order has been updated"
        }
        else {
            "You have received a new order"
        }

        val topic = args.restaurantOwner.replace("[^A-Za-z0-9.]".toRegex(), "-")
        viewModel.sendPushNotification(PushNotification(NotificationData(title, message), "/topics/$topic"))
    }

    private fun subscribeToObservers(order: Order) {
        viewModel.orderStatus.observe(viewLifecycleOwner, {
            it?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        binding.progressBar4.visibility = View.GONE


                        sendPushNotificationToRestaurant()

                        val alarmManager: AlarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

                        val intent = Intent(requireContext(), AlertReceiver::class.java).also {
                            it.putExtra(KEY_TIMESTAMP, order.timestamp)
                            it.putExtra(KEY_RESTAURANT_ID, args.restaurantId)
                            it.putExtra("n", args.restaurantName)
                        }

                        val pendingIntent = PendingIntent.getBroadcast(requireContext(), order.timestamp.toInt(), intent, 0)
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, order.timestamp + TimeUnit.MINUTES.toMillis(1), pendingIntent)

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