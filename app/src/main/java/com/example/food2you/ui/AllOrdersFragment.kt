package com.example.food2you.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.food2you.R
import com.example.food2you.adapters.UserOrderAdapter
import com.example.food2you.data.remote.models.Order
import com.example.food2you.databinding.AllOrdersFragmentBinding
import com.example.food2you.other.Status
import com.example.food2you.viewmodels.OrderViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllOrdersFragment: Fragment(R.layout.all_orders_fragment) {

    private lateinit var binding: AllOrdersFragmentBinding
    private val viewModel: OrderViewModel by viewModels()
    private var currentWaitingOrders: List<Order>? = null

    private lateinit var mAdapter: UserOrderAdapter
    private lateinit var listener: UserOrderAdapter.OnOrderClickListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AllOrdersFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listener = object : UserOrderAdapter.OnOrderClickListener {
            override fun onOrderClicked(order: Order) {
                val action = AllOrdersFragmentDirections.actionAllOrdersFragmentToPostOrderFragment(order, order.restaurantName, order.resImgUrl)
                findNavController().navigate(action)
            }

            override fun onUpdateButtonClicked(order: Order) {
                val action = AllOrdersFragmentDirections.actionAllOrdersFragmentToDetailRestaurantFragment(order.resId, order.restaurantName, order)
                findNavController().navigate(action)
            }

            override fun onTrackButtonClicked(order: Order) {
                findNavController().navigate(AllOrdersFragmentDirections.actionAllOrdersFragmentToTrackOrderFragment(order.id))
            }
        }

        mAdapter = UserOrderAdapter((requireContext()), listener)
        setUpRecyclerView()

        subscribeToObservers()


    }


    private fun subscribeToObservers() {
        viewModel.allOrders.observe(viewLifecycleOwner, {
            it?.let { event ->
                val result = event.peekContent()

                when (result.status) {
                    Status.SUCCESS -> {
                        currentWaitingOrders = result.data

                        mAdapter.orders = currentWaitingOrders!!

                    }
                    Status.LOADING -> {

                    }
                    Status.ERROR -> {
                        event.getContentIfNotHandled()?.let { error ->
                            Snackbar.make(requireView(), error.message ?: "Something went wrong", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        })
    }

    private fun setUpRecyclerView() {
        binding.recyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
            setHasFixedSize(true)
        }
    }
}