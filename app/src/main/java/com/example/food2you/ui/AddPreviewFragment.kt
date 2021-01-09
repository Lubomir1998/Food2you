package com.example.food2you.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.food2you.R
import com.example.food2you.databinding.AddPreviewFragmentBinding
import com.example.food2you.other.Status
import com.example.food2you.viewmodels.AddPreviewViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddPreviewFragment: Fragment(R.layout.add_preview_fragment) {

    private lateinit var binding: AddPreviewFragmentBinding
    private val viewModel: AddPreviewViewModel by viewModels()
    private val args: AddPreviewFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = AddPreviewFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding.submitPreviewBtn.setOnClickListener {
            val preview = binding.previewEt.text.toString()
            val id = args.restaurantId

            if(preview.isNotEmpty()) {
                if (id.isNotEmpty()) {
                    viewModel.addPreview(id, preview)
                    subscribeToObservers()
                } else {
                    Snackbar.make(requireView(), "Something is wrong", Snackbar.LENGTH_LONG).show()
                }
            }
            else {
                Snackbar.make(requireView(), "Preview is empty", Snackbar.LENGTH_LONG).show()
            }
        }

    }

    private fun subscribeToObservers() {
        viewModel.addPreviewStatus.observe(viewLifecycleOwner, {
            it?.let { event ->
                val result = event.peekContent()

                when(result.status) {
                    Status.SUCCESS -> {
                       findNavController().navigate(R.id.action_launch_main_fragment)
                    }
                    Status.ERROR -> {
                        Snackbar.make(requireView(), result.message ?: "An unknown error occurred", Snackbar.LENGTH_LONG).show()
                    }
                    Status.LOADING -> { }
                }

            }
        })
    }

}