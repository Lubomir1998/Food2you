package com.example.food2you.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.food2you.R
import com.example.food2you.data.local.entities.Restaurant
import com.example.food2you.databinding.ActivityMainBinding.inflate
import com.example.food2you.databinding.DetailRestaurantFragmentBinding
import com.example.food2you.databinding.RestaurantsFragmentBinding
import com.example.food2you.other.Status
import com.example.food2you.viewmodels.DetailRestaurantViewModel
import com.example.food2you.viewmodels.RestaurantsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailRestaurantFragment: Fragment(R.layout.detail_restaurant_fragment) {

    private lateinit var binding: DetailRestaurantFragmentBinding
    private val viewModel: DetailRestaurantViewModel by viewModels()
    private val args: DetailRestaurantFragmentArgs by navArgs()
    private var currentRestaurant: Restaurant? = null

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


        if(args.restaurantId.isNotEmpty()) {
            viewModel.getRestaurantById(args.restaurantId)
            subscribeToObservers()
        }



    }


    @SuppressLint("SetTextI18n")
    private fun subscribeToObservers() {
        viewModel.restaurant.observe(viewLifecycleOwner, {
            it?.let { event ->
                val result = event.peekContent()

                when (result.status) {
                    Status.SUCCESS -> {
                        currentRestaurant = result.data!!

                        binding.titleTextView.text = currentRestaurant!!.name
                        binding.reviewsTextView.text = "${currentRestaurant!!.previews.size} reviews"

                        Glide
                            .with(requireContext())
                            .load(currentRestaurant!!.imgUrl)
                            .into(binding.restaurantLogoImg)


                    }
                    Status.ERROR -> {

                    }
                    Status.LOADING -> {

                    }

                }

            }
        })
    }

}