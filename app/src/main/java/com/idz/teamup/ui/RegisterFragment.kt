package com.idz.teamup.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.idz.teamup.R
import com.idz.teamup.viewmodel.AuthViewModel

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fullNameInput = view.findViewById<EditText>(R.id.fullNameRegister)
        val emailInput = view.findViewById<EditText>(R.id.emailRegister)
        val passwordInput = view.findViewById<EditText>(R.id.passwordRegister)
        val registerButton = view.findViewById<Button>(R.id.buttonRegister)
        val loginNow = view.findViewById<TextView>(R.id.loginNow)

        loginNow.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }

        authViewModel.authResult.observe(viewLifecycleOwner) { result ->
            if (result.first) {
                Toast.makeText(requireContext(), "Registration Successful!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_register_to_login)
            } else {
                Toast.makeText(requireContext(), "Registration Failed: ${result.second}", Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            val fullName = fullNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            if (fullName.isEmpty() ||email.isEmpty() || password.isEmpty() || password.length < 6) {
                Toast.makeText(requireContext(), "Enter valid name, email, and password (6+ chars)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            authViewModel.register(fullName,email,password)
        }
    }
}
