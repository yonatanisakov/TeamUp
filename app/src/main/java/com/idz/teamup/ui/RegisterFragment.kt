package com.idz.teamup.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.idz.teamup.R
import com.idz.teamup.viewmodel.AuthViewModel
import com.squareup.picasso.Picasso

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var fullNameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var registerButton: MaterialButton
    private lateinit var loginNow: TextView
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var profileImageView: ShapeableImageView
    private lateinit var selectImageButton: MaterialButton

    private var profileImageUri: Uri? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fullNameInput = view.findViewById(R.id.fullNameRegister)
        emailInput = view.findViewById(R.id.emailRegister)
        passwordInput = view.findViewById(R.id.passwordRegister)
        registerButton = view.findViewById(R.id.buttonRegister)
        loginNow = view.findViewById(R.id.loginNow)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)
        profileImageView = view.findViewById(R.id.profileImageView)
        selectImageButton = view.findViewById(R.id.selectImageButton)

        selectImageButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        loginNow.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }

        authViewModel.authResult.observe(viewLifecycleOwner) { result ->
            loadingOverlay.visibility = View.GONE
            registerButton.isEnabled = true

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

            loadingOverlay.visibility = View.VISIBLE
            registerButton.isEnabled = false
            authViewModel.register(fullName, email, password, profileImageUri)

        }
    }
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            profileImageUri = uri

            Picasso.get()
                .load(uri)
                .fit()
                .centerCrop()
                .into(profileImageView)
        }
    }
}

