package com.idz.teamup

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val fullNameInput = view.findViewById<EditText>(R.id.fullNameRegister)
        val emailInput = view.findViewById<EditText>(R.id.emailRegister)
        val passwordInput = view.findViewById<EditText>(R.id.passwordRegister)
        val registerButton = view.findViewById<Button>(R.id.buttonRegister)
        val loginNow = view.findViewById<TextView>(R.id.loginNow)

        // Navigate to Login Fragment
        loginNow.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }

        registerButton.setOnClickListener {
            val fullName = fullNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || password.length < 6) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser!!.uid
                        val user = hashMapOf(
                            "fullName" to fullName,
                            "email" to email,
                            "userId" to userId
                        )

                        db.collection("users").document(userId)
                            .set(user)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Registration Successful", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_register_to_login)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Error saving user: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(requireContext(), "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
