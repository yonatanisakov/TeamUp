package com.idz.teamup.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.idz.teamup.R
import com.idz.teamup.viewmodel.AuthViewModel

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailInput = view.findViewById<EditText>(R.id.emailLogin)
        val passwordInput = view.findViewById<EditText>(R.id.passwordLogin)
        val loginButton = view.findViewById<Button>(R.id.buttonLogin)
        val registerNow = view.findViewById<TextView>(R.id.registerNow)

        registerNow.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        authViewModel.authResult.observe(viewLifecycleOwner) { result ->
            if (result.first) {
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            } else {
                Toast.makeText(requireContext(), "Login Failed: ${result.second}", Toast.LENGTH_SHORT).show()
            }
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
