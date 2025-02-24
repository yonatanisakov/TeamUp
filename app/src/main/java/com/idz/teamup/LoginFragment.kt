package com.idz.teamup

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigate to Register Fragment when "Register now" clicked
        view.findViewById<TextView>(R.id.registerNow).setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        // Authentication logic will go here soon
    }
}
