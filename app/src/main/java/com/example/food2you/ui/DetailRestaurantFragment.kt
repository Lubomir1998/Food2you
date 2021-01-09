package com.example.food2you.ui

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.food2you.R
import com.example.food2you.adapters.FoodAdapter
import com.example.food2you.data.local.entities.Food
import com.example.food2you.data.local.entities.Restaurant
import com.example.food2you.data.remote.models.FoodItem
import com.example.food2you.data.remote.models.FoodItems
import com.example.food2you.databinding.DetailRestaurantFragmentBinding
import com.example.food2you.other.Constants.KEY_EMAIL
import com.example.food2you.other.Constants.KEY_RESTAURANT
import com.example.food2you.other.Status
import com.example.food2you.other.hasInternetConnection
import com.example.food2you.viewmodels.DetailRestaurantViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.math.RoundingMode
import javax.inject.Inject

private const val TAG = "DetailRestaurantFragmen"

@AndroidEntryPoint
class DetailRestaurantFragment: Fragment(R.layout.detail_restaurant_fragment) {

    private lateinit var binding: DetailRestaurantFragmentBinding
    private val viewModel: DetailRestaurantViewModel by viewModels()
    private val args: DetailRestaurantFragmentArgs by navArgs()
    private var currentRestaurant: Restaurant? = null
    private lateinit var foodAdapter: FoodAdapter
    private lateinit var listener: FoodAdapter.OnFoodClickListener
    private var chipList = mutableListOf<String>()
    private var currentList: List<Food>? = null
    private var orderList: ArrayList<FoodItem> = arrayListOf()
    private var orderPrice = 0f

    @Inject lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DetailRestaurantFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER

        val email = sharedPrefs.getString(KEY_EMAIL, "") ?: ""

        listener = object : FoodAdapter.OnFoodClickListener {
            override fun onFoodClicked(food: Food) {
                binding.orderBar.visibility = View.VISIBLE

                val price = food.price
                val foodName = food.name

                viewModel.increasePrice(price)
                viewModel.addToList(FoodItem(foodName, price))

            }
        }

        viewModel.orderPrice.observe(viewLifecycleOwner, {
            val floatPrice = it.toBigDecimal().setScale(2, RoundingMode.FLOOR).toFloat()
            binding.orderPriceTextView.text = foodAdapter.formattedStringPrice(floatPrice.toString()) + " EUR"
            orderPrice = it
        })


        viewModel.orderList.observe(viewLifecycleOwner, {
            if(it.isNotEmpty()) {
                binding.orderBar.visibility = View.VISIBLE
            }
            binding.foodQuantity.text = it.size.toString()
            orderList = it
        })


        binding.orderBar.setOnClickListener {
            val list = FoodItems().also {
                it.addAll(orderList)
            }
            val action = DetailRestaurantFragmentDirections.actionDetailRestaurantFragmentToOrderFragment(
                    orderPrice.toBigDecimal().setScale(2, RoundingMode.FLOOR).toFloat(),
                    list,
                    currentRestaurant!!.owner,
                    currentRestaurant!!.deliveryPrice,
                    currentRestaurant!!.minimalPrice,
                    currentRestaurant!!.name,
                    args.restaurantId,
                    currentRestaurant!!.imgUrl
                )
            findNavController().navigate(action)
        }

        foodAdapter = FoodAdapter(listOf(), requireContext(), listener)
        setUpRecyclerView()

        if(args.restaurantId.isNotEmpty()) {
            viewModel.getRestaurantById(args.restaurantId)
            subscribeToObservers()
        }
        else {
            val sharedPrefId = sharedPrefs.getString(KEY_RESTAURANT, "") ?: ""
            if(sharedPrefId.isNotEmpty()) {
                viewModel.getRestaurantById(sharedPrefId)
                subscribeToObservers()
            }
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
            else {
                binding.swipeRefresh.isRefreshing = false
            }
        }

        binding.reviewsTextView.setOnClickListener {
            currentRestaurant?.let { restaurant ->
                if(restaurant.previews.isNotEmpty()) {
                    val action = DetailRestaurantFragmentDirections.actionDetailRestaurantFragmentToPreviewsFragment(restaurant)
                    findNavController().navigate(action)
                }
            }
        }

        binding.favButton.setOnClickListener {

            if(hasInternetConnection(requireContext())) {
                if (email.isNotEmpty()) {
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
                } else {
                    Snackbar.make(requireView(), "Sign in first", Snackbar.LENGTH_LONG).show()
                }
            }
            else {
                Snackbar.make(requireView(), "Check your connection", Snackbar.LENGTH_LONG).show()
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

                        sharedPrefs.edit().putString(KEY_RESTAURANT, currentRestaurant!!.id).apply()

                        val email = sharedPrefs.getString(KEY_EMAIL, "") ?: ""

                        if(currentRestaurant!!.users.contains(email)) {
                            binding.favButton.setImageResource(R.drawable.fav_img)
                        }
                        else {
                            binding.favButton.setImageResource(R.drawable.not_fav_img)
                        }


                        binding.titleTextView.text = currentRestaurant!!.name
                        binding.reviewsTextView.text = if(currentRestaurant!!.previews.size == 1) {
                            "1 review"
                        }
                        else if(currentRestaurant!!.previews.isEmpty()) {
                            "No reviews"
                        }
                        else {
                            "${currentRestaurant!!.previews.size} reviews"
                        }

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
                        currentList = result.data
                        displayData(currentList!!)

                        binding.chipGroup.removeAllViews()
                        binding.chipGroup.clearCheck()
                        addChip("All")

                        chipList.clear()

                        for(food in currentList!!) {
                            val exists = chipList.contains(food.type)
                            if(!exists) {
                                chipList.add(food.type)
                                addChip(food.type)
                            }

                        }

                        val firstChip = binding.chipGroup.getChildAt(0) as Chip?
                        firstChip?.isSelected = true


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

        chip.apply {
            text = chipText
            setChipBackgroundColorResource(R.drawable.chip_color)
            isCheckable = true
            id = if(chipText == "All") {
                1
            }
            else {
                chipList.size + 1
            }
        }

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