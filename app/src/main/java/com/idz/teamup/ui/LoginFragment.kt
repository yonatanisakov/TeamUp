package com.idz.teamup.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.idz.teamup.R
import com.idz.teamup.viewmodel.AuthViewModel

class LoginFragment : Fragment(R.layout.fragment_login) {
    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var registerNow: TextView
    private lateinit var loadingOverlay: FrameLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI(view)
        observeViewModel()
        setupListeners()
    }

    private fun setupUI(view: View) {
        emailInput = view.findViewById(R.id.emailLogin)
        passwordInput = view.findViewById(R.id.passwordLogin)
        loginButton = view.findViewById(R.id.buttonLogin)
        registerNow = view.findViewById(R.id.registerNow)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)
    }

    private fun observeViewModel() {
        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            loginButton.isEnabled = !isLoading
        }

        authViewModel.authResult.observe(viewLifecycleOwner) { result ->
            if (result.first) {
                findNavController().navigate(
                    R.id.action_loginFragment_to_homeFragment,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, true)
                        .build()
                )
            } else {
                Toast.makeText(requireContext(), "Login Failed: ${result.second}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        registerNow.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.login(email, password)
        }
    }
}
