package com.idz.teamup.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.idz.teamup.R
import com.idz.teamup.viewmodel.ProfileViewModel
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val profileViewModel: ProfileViewModel by viewModels()

    private lateinit var profileImageView: ImageView
    private lateinit var editTextName: EditText
    private lateinit var saveProfileButton: Button
    private lateinit var changeProfilePicButton: Button
    private lateinit var deleteProfilePicButton: Button
    private lateinit var logoutButton: Button

    private var newImageUri: Uri? = null
    private var isImageDeleted: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileImageView = view.findViewById(R.id.profileImageView)
        editTextName = view.findViewById(R.id.editTextName)
        saveProfileButton = view.findViewById(R.id.saveProfileButton)
        changeProfilePicButton = view.findViewById(R.id.changeProfilePicButton)
        deleteProfilePicButton = view.findViewById(R.id.deleteProfilePicButton)
        logoutButton = view.findViewById(R.id.logoutButton)

        profileViewModel.user.observe(viewLifecycleOwner) { user ->
            editTextName.setText(user?.fullName)
            if (!isImageDeleted && newImageUri == null && user?.profileImageUrl?.isNotEmpty() == true) {
                Picasso.get().load(user.profileImageUrl).into(profileImageView)
            }
        }

        profileViewModel.loadUserProfile()

        changeProfilePicButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        deleteProfilePicButton.setOnClickListener {
            profileImageView.setImageResource(R.drawable.ic_launcher_foreground) // Default image
            newImageUri = null
            isImageDeleted = true
        }

        saveProfileButton.setOnClickListener {
            val currentUser = profileViewModel.user.value
            if (currentUser == null) {
                showToast("User not loaded yet")
                return@setOnClickListener
            }

            val updatedUser = currentUser.copy(fullName = editTextName.text.toString().trim())

            when {
                newImageUri != null -> {
                    profileViewModel.uploadProfilePicture(newImageUri!!) { imageUrl ->
                        if (imageUrl != null) {
                            profileViewModel.updateUserProfile(updatedUser.copy(profileImageUrl = imageUrl)) { success ->
                                showToast(if (success) "Profile updated!" else "Update failed!")
                            }
                        } else {
                            showToast("Image upload failed!")
                        }
                    }
                }
                isImageDeleted -> {
                    profileViewModel.deleteProfilePicture { success ->
                        if (success) {
                            profileViewModel.updateUserProfile(updatedUser.copy(profileImageUrl = "")) { profileUpdated ->
                                showToast(if (profileUpdated) "Profile updated!" else "Update failed!")
                            }
                        } else {
                            showToast("Image delete failed!")
                        }
                    }
                }
                else -> {
                    profileViewModel.updateUserProfile(updatedUser) { profileUpdated ->
                        showToast(if (profileUpdated) "Profile updated!" else "Update failed!")
                    }
                }
            }
        }

        // Logout
        logoutButton.setOnClickListener {
            profileViewModel.logout()
            profileViewModel.user.removeObservers(viewLifecycleOwner) // Clear observer
            showToast("Logged out")
            requireActivity().finish()
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            newImageUri = uri // Store selected image for later upload
            isImageDeleted = false // Reset delete flag
            profileImageView.setImageURI(uri) // Show preview
        }
    }

    private fun showToast(message: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
