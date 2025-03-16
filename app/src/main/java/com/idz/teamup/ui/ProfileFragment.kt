package com.idz.teamup.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.idz.teamup.R
import com.idz.teamup.viewmodel.ProfileViewModel
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val profileViewModel: ProfileViewModel by viewModels()

    private lateinit var profileImageView: ShapeableImageView
    private lateinit var profileNameText: TextView
    private lateinit var profileEmailText: TextView
    private lateinit var editTextName: TextInputEditText
    private lateinit var saveProfileButton: MaterialButton
    private lateinit var changeProfilePicButton: MaterialButton
    private lateinit var deleteProfilePicButton: MaterialButton
    private lateinit var logoutButton: MaterialButton
    private lateinit var loadingOverlay: FrameLayout

    private var newImageUri: Uri? = null
    private var isImageDeleted: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI(view)
        observeViewModel()
        setupListeners()

    }
    private fun setupUI(view: View) {
        profileImageView = view.findViewById(R.id.profileImageView)
        profileNameText = view.findViewById(R.id.profileNameText)
        profileEmailText = view.findViewById(R.id.profileEmailText)
        editTextName = view.findViewById(R.id.editTextName)
        saveProfileButton = view.findViewById(R.id.saveProfileButton)
        changeProfilePicButton = view.findViewById(R.id.changeProfilePicButton)
        deleteProfilePicButton = view.findViewById(R.id.deleteProfilePicButton)
        logoutButton = view.findViewById(R.id.logoutButton)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)
    }
    private fun observeViewModel() {
        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            saveProfileButton.isEnabled = !isLoading
            changeProfilePicButton.isEnabled = !isLoading
            deleteProfilePicButton.isEnabled = !isLoading
            logoutButton.isEnabled = !isLoading
        }


        profileViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                profileNameText.text = it.fullName
                profileEmailText.text = it.email
                editTextName.setText(it.fullName)

                if (!isImageDeleted && newImageUri == null && it.profileImageUrl.isNotEmpty()) {
                    Picasso.get()
                        .load(it.profileImageUrl)
                        .fit()
                        .centerCrop()
                        .into(profileImageView)
                }
            }
        }

        profileViewModel.loadUserProfile()
    }
    private fun setupListeners() {
        changeProfilePicButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        deleteProfilePicButton.setOnClickListener {
            profileImageView.setImageResource(R.drawable.ic_person)
            newImageUri = null
            isImageDeleted = true
        }

        saveProfileButton.setOnClickListener {
            saveProfile()
        }

        logoutButton.setOnClickListener {
            profileViewModel.logout()
            profileViewModel.user.removeObservers(viewLifecycleOwner)
            showToast("Logged out")

            findNavController().navigate(
                R.id.loginFragment,
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
            )
        }
    }
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            newImageUri = uri
            isImageDeleted = false
            Picasso.get()
                .load(uri)
                .fit()
                .centerCrop()
                .into(profileImageView)
        }
    }

    private fun showToast(message: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }


    private fun saveProfile() {
        val currentUser = profileViewModel.user.value
        if (currentUser == null) {
            showToast("User not loaded yet")
            return
        }

        val updatedUser = currentUser.copy(fullName = editTextName.text.toString().trim())

        when {
            newImageUri != null -> {
                val imageUri = newImageUri ?: return
                profileViewModel.updateUserWithImage(updatedUser, imageUri) { success ->
                    showToast(if (success) "Profile updated!" else "Update failed!")
                }
            }

            isImageDeleted -> {
                profileViewModel.deleteProfilePictureAndUpdate(updatedUser) { success ->
                    showToast(if (success) "Profile updated!" else "Update failed!")
                }
            }

            else -> {
                profileViewModel.updateUserWithoutImage(updatedUser) { success ->
                    showToast(if (success) "Profile updated!" else "Update failed!")
                }
            }
        }
    }
}
