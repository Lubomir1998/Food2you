package com.example.food2you.ui

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toolbar
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.food2you.R
import com.example.food2you.adapters.RestaurantAdapter
import com.example.food2you.data.local.entities.Restaurant
import com.example.food2you.databinding.ActivityMainBinding
import com.example.food2you.databinding.RestaurantsFragmentBinding
import com.example.food2you.other.BasicAuthInterceptor
import com.example.food2you.other.Constants
import com.example.food2you.other.Constants.KEY_EMAIL
import com.example.food2you.other.Constants.KEY_PASSWORD
import com.example.food2you.other.Status
import com.example.food2you.viewmodels.RestaurantsViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "RestaurantsFragment"

@AndroidEntryPoint
class RestaurantsFragment: Fragment(R.layout.restaurants_fragment) {

    private var currentList: List<Restaurant>? = null
    private var chipList = mutableListOf<String>()
    private lateinit var binding: RestaurantsFragmentBinding
    private val viewModel: RestaurantsViewModel by viewModels()
    private lateinit var restaurantAdapter: RestaurantAdapter
    private lateinit var listener: RestaurantAdapter.OnRestaurantClickListener
    @Inject
    lateinit var sharedPrefs: SharedPreferences
    @Inject
    lateinit var baseAuthInterceptor: BasicAuthInterceptor


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        binding = RestaurantsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER

        val email = sharedPrefs.getString(KEY_EMAIL, "") ?: ""
        val password = sharedPrefs.getString(KEY_PASSWORD, "") ?: ""

        if(email.isNotEmpty() && password.isNotEmpty()) {
            authenticateApi(email, password)
        }

        listener = object : RestaurantAdapter.OnRestaurantClickListener {
            override fun onRestaurantClicked(restaurant: Restaurant) {
                val action = RestaurantsFragmentDirections.actionRestaurantsFragmentToDetailRestaurantFragment(restaurant.id, restaurant.name)
                findNavController().navigate(action)
            }
        }

        restaurantAdapter = RestaurantAdapter(listOf(), requireContext(), listener)
        setupRecyclerView()

        viewModel.getAllRestaurants()
        subscribeToOtherObserver()

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

        binding.swipeRefresh.setOnRefreshListener {
            subscribeToObservers()
            binding.swipeRefresh.isRefreshing = false
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

                when (result.status) {
                    Status.SUCCESS -> {
                        binding.progressBar.visibility = View.GONE
                        displayData(result.data!!)
                        currentList = result.data

                        binding.orderTextView.text = "Order from ${result.data.size} restaurants"

                        binding.chipGroup.removeAllViews()
                        addChip("All")

                        chipList.clear()

                        for (restaurant in result.data) {
                            val exists = chipList.contains(restaurant.type)
                            if (!exists) {
                                chipList.add(restaurant.type)
                                addChip(restaurant.type)
                            }

                        }

                        val firstChip = binding.chipGroup.getChildAt(0) as Chip?
                        firstChip?.isSelected = true

                        for (view in binding.chipGroup) {
                            val chip = view as Chip?
                            Log.d(TAG, "********************: ${chip?.text} - ${chip?.id} \n")
                        }
                    }
                    Status.ERROR -> {
                        binding.progressBar.visibility = View.GONE
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
                        binding.progressBar.visibility = View.VISIBLE
                        binding.orderTextView.text = "Loading restaurants..."
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

    private fun authenticateApi(email: String, password: String) {
        baseAuthInterceptor.email = email
        baseAuthInterceptor.password = password
    }

    @SuppressLint("SetTextI18n")
    private fun subscribeToOtherObserver() {
        viewModel.allRes.observe(viewLifecycleOwner, {
            it?.let { event ->
                val result = event.peekContent()

                when (result.status) {
                    Status.SUCCESS -> {
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        displayData(result.data!!)
                        currentList = result.data

                        binding.orderTextView.text = "Order from ${result.data.size} restaurants"

                        binding.chipGroup.removeAllViews()
                        addChip("All")

                        chipList.clear()

                        for (restaurant in result.data) {
                            val exists = chipList.contains(restaurant.type)
                            if (!exists) {
                                chipList.add(restaurant.type)
                                addChip(restaurant.type)
                            }

                        }

                        val firstChip = binding.chipGroup.getChildAt(0) as Chip?
                        firstChip?.isSelected = true

                    }
                    Status.ERROR -> {
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
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
                        binding.progressBar.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                        binding.orderTextView.text = "Loading restaurants..."
                    }
                }


            }
        })
    }

}