package com.example.food2you.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.food2you.R
import com.example.food2you.databinding.MyAccountFragmentBinding
import com.example.food2you.other.Constants.KEY_ADDRESS
import com.example.food2you.other.Constants.KEY_EMAIL
import com.example.food2you.other.Constants.KEY_PASSWORD
import com.example.food2you.other.Constants.KEY_PHONE
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MyAccountFragment: Fragment(R.layout.auth_fragment) {

    private lateinit var binding: MyAccountFragmentBinding

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MyAccountFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val address = sharedPrefs.getString(KEY_ADDRESS, "") ?: ""
        val phone = sharedPrefs.getLong(KEY_PHONE, 0L)


        binding.saveAddressEt.setText(address)
        if(phone != 0L) {
            binding.phoneNumberEt.setText(phone.toString())
        }

        binding.saveInfoBtn.setOnClickListener {
            if(binding.saveAddressEt.text?.isNotEmpty() == true && binding.phoneNumberEt.text?.isNotEmpty() == true) {
                sharedPrefs.edit()
                        .putString(KEY_ADDRESS, binding.saveAddressEt.text.toString())
                        .putLong(KEY_PHONE, binding.phoneNumberEt.text.toString().toLong())
                        .apply()
                Snackbar.make(requireView(), "Info saved", Snackbar.LENGTH_LONG).show()
            }
            else if(binding.saveAddressEt.text?.isNotEmpty() == true && binding.phoneNumberEt.text?.isNotEmpty() == false) {
                sharedPrefs.edit()
                        .putString(KEY_ADDRESS, binding.saveAddressEt.text.toString())
                        .apply()
                Snackbar.make(requireView(), "Address saved", Snackbar.LENGTH_LONG).show()
            }
            else if(binding.saveAddressEt.text?.isNotEmpty() == false && binding.phoneNumberEt.text?.isNotEmpty() == true) {
                sharedPrefs.edit()
                        .putLong(KEY_PHONE, binding.phoneNumberEt.text.toString().toLong())
                        .apply()
                Snackbar.make(requireView(), "Phone saved", Snackbar.LENGTH_LONG).show()
            }
            else {
                Snackbar.make(requireView(), "Fields are empty", Snackbar.LENGTH_LONG).show()
            }
        }

        binding.logOutBtn.setOnClickListener {
            logout()
        }

    }


    private fun logout() {
        sharedPrefs.edit()
            .putString(KEY_EMAIL, "")
            .putString(KEY_PASSWORD, "")
            .putString(KEY_ADDRESS, "")
            .apply()

//        findNavController().navigate(R.id.action_launch_main_fragment)
        startActivity(Intent(requireContext(), MainActivity::class.java))
    }


}