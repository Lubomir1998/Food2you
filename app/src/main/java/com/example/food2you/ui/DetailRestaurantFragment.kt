package com.example.food2you.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.food2you.R
import com.example.food2you.adapters.FoodAdapter
import com.example.food2you.data.local.entities.Food
import com.example.food2you.data.local.entities.Restaurant
import com.example.food2you.databinding.ActivityMainBinding.inflate
import com.example.food2you.databinding.DetailRestaurantFragmentBinding
import com.example.food2you.databinding.RestaurantsFragmentBinding
import com.example.food2you.other.Status
import com.example.food2you.viewmodels.DetailRestaurantViewModel
import com.example.food2you.viewmodels.RestaurantsViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "DetailRestaurantFragmen"

@AndroidEntryPoint
class DetailRestaurantFragment: Fragment(R.layout.detail_restaurant_fragment) {

    private lateinit var binding: DetailRestaurantFragmentBinding
    private val viewModel: DetailRestaurantViewModel by viewModels()
    private val args: DetailRestaurantFragmentArgs by navArgs()
    private var currentRestaurant: Restaurant? = null
    private lateinit var foodAdapter: FoodAdapter
    private var chipList = mutableListOf<String>()
    private var currentList: List<Food>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DetailRestaurantFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        foodAdapter = FoodAdapter(listOf(), requireContext())
        setUpRecyclerView()

        if(args.restaurantId.isNotEmpty()) {
            viewModel.getRestaurantById(args.restaurantId)
            subscribeToObservers()
        }


        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != -1) {

                for (i in 0 until group.childCount) {
                    if (i == checkedId - 1) continue
                    val chip = group.getChildAt(i) as Chip?
                    chip?.isSelected = false
                    chip?.isChecked = false
                }

                val chip = group.getChildAt(checkedId - 1) as Chip?
                chip?.isSelected = true
                chip?.isChecked = false

                if (chip?.text.toString() != "All") {
                    viewModel.filter(chip?.text.toString())
                    subscribeFilterLiveData()
                } else {
                    displayData(currentList!!)
                }

            }
        }

    }


    @SuppressLint("SetTextI18n")
    private fun subscribeToObservers() {
        viewModel.restaurant.observe(viewLifecycleOwner, {
            it?.let { event ->
                val result = event.peekContent()

                when (result.status) {
                    Status.SUCCESS -> {
                        currentRestaurant = result.data

                        binding.titleTextView.text = currentRestaurant!!.name
                        binding.reviewsTextView.text = "${currentRestaurant!!.previews.size} reviews"

                        Glide
                                .with(requireContext())
                                .load(currentRestaurant!!.imgUrl)
                                .into(binding.restaurantLogoImg)

                        viewModel.getFood(currentRestaurant!!.owner)

                        subscribeToFoodList()


                    }
                    Status.ERROR -> {

                    }
                    Status.LOADING -> {

                    }


                }
            }
        })

    }

    private fun subscribeToFoodList() {
        viewModel.foodList.observe(viewLifecycleOwner, {
            it?.let { event ->
                val result = event.peekContent()

                when(result.status) {
                    Status.SUCCESS -> {
                        binding.progressBar2.visibility = View.GONE
                        val list = result.data
                        displayData(list!!)

                        binding.chipGroup.removeAllViews()
                        addChip("All")

                        chipList.clear()

                        for(food in result.data) {
                            val exists = chipList.contains(food.type)
                            chipList.add(food.type)
                            if(!exists) {
                                addChip(food.type)
                            }

                        }

                    }
                    Status.ERROR -> {
                        binding.progressBar2.visibility = View.GONE
                        event.getContentIfNotHandled()?.let { error ->
                            error.message?.let { message ->
                                Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
                            }
                        }
                    }
                    Status.LOADING -> {
                        binding.progressBar2.visibility = View.VISIBLE
                    }
                }
            }
        })
    }


    private fun displayData(list: List<Food>) {
        foodAdapter.listOfFood = list
        foodAdapter.notifyDataSetChanged()
    }

    private fun setUpRecyclerView() {
        binding.recyclerView.apply {
            adapter = foodAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun subscribeFilterLiveData() {
        viewModel.filteredFood.observe(viewLifecycleOwner, {
            displayData(it)
        })
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