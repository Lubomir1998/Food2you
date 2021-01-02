package com.example.food2you.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.food2you.R
import com.example.food2you.databinding.AuthFragmentBinding
import com.example.food2you.other.BasicAuthInterceptor
import com.example.food2you.other.Constants.KEY_EMAIL
import com.example.food2you.other.Constants.KEY_PASSWORD
import com.example.food2you.other.Status
import com.example.food2you.viewmodels.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthFragment: Fragment(R.layout.auth_fragment) {

    private lateinit var binding: AuthFragmentBinding
    private val viewmodel: AuthViewModel by viewModels()

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    @Inject
    lateinit var baseAuthInterceptor: BasicAuthInterceptor

    private var currentEmail: String? = null
    private var currentPassword: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AuthFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        subscribeToObservers()

        binding.btnRegister.setOnClickListener {
            val email = binding.etRegisterEmail.text.toString()
            val password = binding.etRegisterPassword.text.toString()
            val confirmedPassword = binding.etRegisterPasswordConfirm.text.toString()

            viewmodel.register(email, password, confirmedPassword)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etLoginEmail.text.toString()
            val password = binding.etLoginPassword.text.toString()

            currentEmail = email
            currentPassword = password

            viewmodel.login(email, password)

        }



    }


    private fun subscribeToObservers() {
        viewmodel.registerStatus.observe(viewLifecycleOwner, { result ->
            result?.let {
                when(result.status) {
                    Status.LOADING -> {
                        binding.registerProgressBar.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.registerProgressBar.visibility = View.GONE
                        sharedPrefs.edit()
                            .putString(KEY_EMAIL, currentEmail)
                            .putString(KEY_PASSWORD, currentPassword)
                            .apply()

                        authenticateApi(currentEmail ?: "", currentPassword ?: "")

                        binding.etRegisterEmail.text?.clear()
                        binding.etRegisterPassword.text?.clear()
                        binding.etRegisterPasswordConfirm.text?.clear()

                        showSnackBar(result.data ?: "Successfully created an account")
                    }
                    Status.ERROR -> {
                        binding.registerProgressBar.visibility = View.GONE
                        showSnackBar(result.message ?: "An unknown error occurred")
                    }
                }
            }
        })

        viewmodel.loginStatus.observe(viewLifecycleOwner, { result ->
            result?.let {
                when(result.status) {
                    Status.LOADING -> {
                        binding.loginProgressBar.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.loginProgressBar.visibility = View.GONE
                        showSnackBar(result.data ?: "Successfully logged in")

                        sharedPrefs.edit()
                            .putString(KEY_EMAIL, currentEmail)
                            .putString(KEY_PASSWORD, currentPassword)
                            .apply()

                        authenticateApi(currentEmail ?: "", currentPassword ?: "")
                        showSnackBar(result.data ?: "Successfully logged in")
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                    }
                    Status.ERROR -> {
                        binding.loginProgressBar.visibility = View.GONE
                        showSnackBar(result.message ?: "An unknown error occurred")
                    }
                }
            }
        })
    }


    private fun authenticateApi(email: String, password: String) {
        baseAuthInterceptor.email = email
        baseAuthInterceptor.password = password
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

}