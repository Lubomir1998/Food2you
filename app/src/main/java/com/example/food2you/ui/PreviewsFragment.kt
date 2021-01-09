package com.example.food2you.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.food2you.R
import com.example.food2you.adapters.PreviewAdapter
import com.example.food2you.databinding.PreviewsFragmentBinding

class PreviewsFragment: Fragment(R.layout.previews_fragment) {

    private lateinit var binding: PreviewsFragmentBinding
    private val args: PreviewsFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = PreviewsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = args.restaurant.previews

        binding.previewsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = PreviewAdapter(list)
            setHasFixedSize(true)
        }


    }


}