package com.idz.teamup

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class RegisterFragment : Fragment(R.layout.fragment_register) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigate to Login Fragment when "Login now" clicked
        view.findViewById<TextView>(R.id.loginNow).setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }

        // Registration logic will go here soon
    }
}
