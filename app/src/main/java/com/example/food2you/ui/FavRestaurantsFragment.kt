package com.example.food2you.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.food2you.R
import com.example.food2you.adapters.RestaurantAdapter
import com.example.food2you.data.local.entities.Restaurant
import com.example.food2you.databinding.FavRestaurantsFragmentBinding
import com.example.food2you.other.Constants.KEY_EMAIL
import com.example.food2you.other.Constants.NO_EMAIL
import com.example.food2you.other.Status
import com.example.food2you.viewmodels.RestaurantsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FavRestaurantsFragment: Fragment(R.layout.auth_fragment) {

    private lateinit var binding: FavRestaurantsFragmentBinding
    private val viewModel: RestaurantsViewModel by viewModels()
    private lateinit var restaurantAdapter: RestaurantAdapter
    private lateinit var listener: RestaurantAdapter.OnRestaurantClickListener
    private var favList: List<Restaurant>? = null
    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FavRestaurantsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        listener = object : RestaurantAdapter.OnRestaurantClickListener {
            override fun onRestaurantClicked(restaurant: Restaurant) {
                val action = FavRestaurantsFragmentDirections.actionFavRestaurantsFragmentToDetailRestaurantFragment(restaurant.id, restaurant.name)
                findNavController().navigate(action)
            }
        }

        restaurantAdapter = RestaurantAdapter(requireContext(), listener)

        val email = sharedPrefs.getString(KEY_EMAIL, NO_EMAIL) ?: NO_EMAIL

        if(email != NO_EMAIL) {
            setupRecyclerView()
            viewModel.getFavouriteRestaurants()
            subscribeToObservers()
        }
        else {
            binding.button.visibility = View.VISIBLE
            binding.textView.visibility = View.VISIBLE
            binding.imageView2.visibility = View.VISIBLE
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getFavouriteRestaurants()
            subscribeToObservers()
            binding.swipeRefresh.isRefreshing = false
        }


        binding.button.setOnClickListener {
            findNavController().navigate(R.id.action_launch_main_fragment)
        }


    }


    private fun subscribeToObservers() {
        viewModel.favouriteRestaurants.observe(viewLifecycleOwner, {
            it?.let { event ->
                val result = event.peekContent()

                when (result.status) {
                    Status.SUCCESS -> {

                        val list = result.data!!

                        favList = list

                        binding.progressBar3.visibility = View.GONE


                        if (list.isNotEmpty()) {
                            binding.button.visibility = View.GONE
                            binding.textView.visibility = View.GONE
                            binding.imageView2.visibility = View.GONE

                            binding.recyclerView.visibility = View.VISIBLE

                            restaurantAdapter.displayData(list)
                        } else {
                            binding.button.visibility = View.VISIBLE
                            binding.textView.visibility = View.VISIBLE
                            binding.imageView2.visibility = View.VISIBLE

                            binding.recyclerView.visibility = View.GONE
                        }
                    }
                    Status.ERROR -> {
                        binding.progressBar3.visibility = View.GONE
                        binding.button.visibility = View.VISIBLE
                        binding.textView.visibility = View.VISIBLE
                        binding.imageView2.visibility = View.VISIBLE

                        binding.recyclerView.visibility = View.GONE
                    }
                    Status.LOADING -> {
                        binding.progressBar3.visibility = View.VISIBLE
                        binding.button.visibility = View.GONE
                        binding.textView.visibility = View.GONE
                        binding.imageView2.visibility = View.GONE

                        binding.recyclerView.visibility = View.GONE
                    }
                }

            }

        })

    }

//    private fun displayData(list: List<Restaurant>) {
//        restaurantAdapter.listOfRestaurants = list
//        restaurantAdapter.notifyDataSetChanged()
//    }


    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            adapter = restaurantAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

}