package com.example.food2you.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.food2you.R
import com.example.food2you.adapters.RestaurantAdapter
import com.example.food2you.data.local.entities.Restaurant
import com.example.food2you.databinding.RestaurantsFragmentBinding
import com.example.food2you.other.Status
import com.example.food2you.viewmodels.RestaurantsViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "RestaurantsFragment"

@AndroidEntryPoint
class RestaurantsFragment: Fragment(R.layout.restaurants_fragment) {

    private var currentList: List<Restaurant>? = null
    private var chipList = mutableListOf<String>()
    private lateinit var binding: RestaurantsFragmentBinding
    private val viewModel: RestaurantsViewModel by viewModels()
    private lateinit var restaurantAdapter: RestaurantAdapter
    private lateinit var listener: RestaurantAdapter.OnRestaurantClickListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RestaurantsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listener = object : RestaurantAdapter.OnRestaurantClickListener {
            override fun onRestaurantClicked(restaurant: Restaurant) {
                val action = RestaurantsFragmentDirections.actionRestaurantsFragmentToDetailRestaurantFragment(restaurant.id)
                findNavController().navigate(action)
            }
        }

        restaurantAdapter = RestaurantAdapter(listOf(), requireContext(), listener)
        setupRecyclerView()

        subscribeToObservers()

        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
            if(checkedId != -1) {

                for (i in 0 until group.childCount) {
                    if (i == checkedId - 1) continue
                    val chip = group.getChildAt(i) as Chip?
                    chip?.isSelected = false
                    chip?.isChecked = false
                }

                val chip = group.getChildAt(checkedId - 1) as Chip?
                chip?.isSelected = true
                chip?.isChecked = false

                if(chip?.text.toString() != "All") {
                    viewModel.filter(chip?.text.toString())
                    subscribeFilterLiveData()
                }
                else {
                    displayData(currentList!!)
                }

            }


        }




    }


    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            adapter = restaurantAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }


    private fun subscribeFilterLiveData() {
        viewModel.filteredRestaurants.observe(viewLifecycleOwner, {
            displayData(it)
        })
    }

    @SuppressLint("SetTextI18n")
    private fun subscribeToObservers() {
        viewModel.allRestaurants.observe(viewLifecycleOwner, {
            it?.let { event ->
                val result = event.peekContent()

                when(result.status) {
                    Status.SUCCESS -> {
                        binding.progressBar.visibility = View.GONE
                        displayData(result.data!!)
                        currentList = result.data

                        binding.orderTextView.text = "Order from ${result.data.size} restaurants"

                        binding.chipGroup.removeAllViews()
                        addChip("All")

                        chipList.clear()

                        for(restaurant in result.data) {
                            val exists = chipList.contains(restaurant.type)
                            chipList.add(restaurant.type)
                            if(!exists) {
                                addChip(restaurant.type)
                            }

                        }

                        val firstChip = binding.chipGroup.getChildAt(0) as Chip?
                        firstChip?.isSelected = true

                    }
                    Status.ERROR -> {
                        binding.progressBar.visibility = View.GONE
                        event.getContentIfNotHandled()?.let { error ->
                            error.message?.let { message ->
                                Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
                            }
                        }
                    }
                    Status.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }

            }

        })
    }

    private fun displayData(list: List<Restaurant>) {
        restaurantAdapter.listOfRestaurants = list
        restaurantAdapter.notifyDataSetChanged()
    }

    @SuppressLint("ResourceType")
    private fun addChip(chipText: String) {
        val chip = Chip(requireContext())

        chip.text = chipText
        chip.setChipBackgroundColorResource(R.drawable.chip_color)
        chip.isCheckable = true
        binding.chipGroup.addView(chip)
    }



}