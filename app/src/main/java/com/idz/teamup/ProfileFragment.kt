package com.idz.teamup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var profileImageView: ImageView
    private lateinit var editTextName: EditText
    private lateinit var changeProfilePicButton: Button
    private lateinit var saveProfileButton: Button
    private lateinit var logoutButton: Button
    private lateinit var deleteProfilePicButton: Button

    private var imageUri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        profileImageView = view.findViewById(R.id.profileImageView)
        editTextName = view.findViewById(R.id.editTextName)
        changeProfilePicButton = view.findViewById(R.id.changeProfilePicButton)
        saveProfileButton = view.findViewById(R.id.saveProfileButton)
        logoutButton = view.findViewById(R.id.logoutButton)
        deleteProfilePicButton = view.findViewById(R.id.deleteProfilePicButton)
        deleteProfilePicButton.setOnClickListener {
            deleteProfilePicture()
        }

        loadUserProfile()

        // Change profile picture
        changeProfilePicButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Save changes
        saveProfileButton.setOnClickListener {
            saveUserProfile()
        }

        // Logout
        logoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }
    }

    private fun deleteProfilePicture() {
        val userId = auth.currentUser?.uid ?: return
        val imageRef = storage.reference.child("profile_images/$userId.jpg")
        imageRef.delete()
            .addOnSuccessListener {
                db.collection("users").document(userId).update("profileImageUrl", "")
                    .addOnSuccessListener { Toast.makeText(requireContext(), "Profile picture deleted", Toast.LENGTH_SHORT).show()
                    profileImageView.setImageResource(R.drawable.ic_launcher_foreground)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to update FireStore", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete profile picture", Toast.LENGTH_SHORT).show()
            }

    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("fullName") ?: ""
                    val profileImageUrl = document.getString("profileImageUrl") ?: ""

                    editTextName.setText(name)
                    if (profileImageUrl.isNotEmpty()) {
                        Picasso.get().load(profileImageUrl).into(profileImageView)
                    }
                }
            }
    }

    private fun saveUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        val name = editTextName.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        val userUpdates = hashMapOf("fullName" to name)

        if (imageUri != null) {
            val imageRef = storage.reference.child("profile_images/$userId.jpg")
            imageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        userUpdates["profileImageUrl"] = uri.toString()
                        updateFirestore(userId, userUpdates)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        } else {
            updateFirestore(userId, userUpdates)
        }
    }

    private fun updateFirestore(userId: String, userUpdates: HashMap<String, String>) {
        db.collection("users").document(userId).update(userUpdates as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            profileImageView.setImageURI(uri)
        }
    }
}
