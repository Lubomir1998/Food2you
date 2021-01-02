package com.example.food2you.ui

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
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
import com.example.food2you.other.Constants.KEY_EMAIL
import com.example.food2you.other.Status
import com.example.food2you.viewmodels.DetailRestaurantViewModel
import com.example.food2you.viewmodels.RestaurantsViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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

    @Inject lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = DetailRestaurantFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = sharedPrefs.getString(KEY_EMAIL, "") ?: ""



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

        binding.swipeRefresh.setOnRefreshListener {
            if(args.restaurantId.isNotEmpty()) {
                viewModel.getRestaurantById(args.restaurantId)
                subscribeToObservers()
                binding.swipeRefresh.isRefreshing = false
            }
        }

        binding.favButton.setOnClickListener {

            if(email.isNotEmpty()) {
                if (currentRestaurant?.users?.contains(email) == true) {
                    viewModel.dislikeRestaurant(
                        args.restaurantId,
                        sharedPrefs.getString(KEY_EMAIL, "") ?: ""
                    )
                    subscribeToLikeObservers()
                } else {
                    viewModel.likeRestaurant(
                        args.restaurantId,
                        sharedPrefs.getString(KEY_EMAIL, "") ?: ""
                    )
                    subscribeToLikeObservers()
                }
            }
            else {
                Snackbar.make(requireView(), "Sign in first", Snackbar.LENGTH_LONG).show()
            }

        }

    }


    @SuppressLint("SetTextI18n")
    private fun subscribeToObservers() {
        viewModel.allRestaurants.observe(viewLifecycleOwner, {
            it?.let { event ->
                val result = event.peekContent()

                when(result.status) {
                    Status.SUCCESS -> {
                        val list = result.data!!

                        for(restaurant in list) {
                            if(restaurant.id == args.restaurantId) {
                                currentRestaurant = restaurant
                            }
                        }

                    }
                    Status.ERROR -> {
                        Snackbar.make(requireView(), "Something went wrong", Snackbar.LENGTH_LONG).show()
                    }
                    Status.LOADING -> {}
                }

            }
        })

        viewModel.restaurant.observe(viewLifecycleOwner, {
            it?.let { event ->
                val result = event.peekContent()

                when (result.status) {
                    Status.SUCCESS -> {
                        currentRestaurant = result.data

                        val email = sharedPrefs.getString(KEY_EMAIL, "") ?: ""

                        if(currentRestaurant!!.users.contains(email)) {
                            binding.favButton.setImageResource(R.drawable.fav_img)
                        }
                        else {
                            binding.favButton.setImageResource(R.drawable.not_fav_img)
                        }


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
                        result.data?.let {
                            displayData(it)
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

    private fun subscribeToLikeObservers() {
        viewModel.likeStatus.observe(viewLifecycleOwner, { event ->
            event?.getContentIfNotHandled()?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        Snackbar.make(requireView(), "Added to favourites", Snackbar.LENGTH_LONG).show()
                        binding.favButton.setImageResource(R.drawable.fav_img)
                    }
                    Status.ERROR -> {
                        Snackbar.make(requireView(), "Something went wrong", Snackbar.LENGTH_LONG).show()
                    }
                    Status.LOADING -> {}
                }
            }
        })

        viewModel.dislikeStatus.observe(viewLifecycleOwner, { event ->
            event?.getContentIfNotHandled()?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        Snackbar.make(requireView(), "Removed from favourites", Snackbar.LENGTH_LONG).show()
                        binding.favButton.setImageResource(R.drawable.not_fav_img)
                    }
                    Status.ERROR -> {
                        Snackbar.make(requireView(), "Something went wrong", Snackbar.LENGTH_LONG).show()
                    }
                    Status.LOADING -> {}
                }
            }
        })

    }

}