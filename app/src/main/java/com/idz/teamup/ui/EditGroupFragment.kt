package com.idz.teamup.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.idz.teamup.R
import com.idz.teamup.viewmodel.GroupDetailsViewModel
import com.squareup.picasso.Picasso

class EditGroupFragment : Fragment(R.layout.fragment_edit_group) {

    private val args: EditGroupFragmentArgs by navArgs()
    private val viewModel: GroupDetailsViewModel by viewModels()

    private lateinit var editGroupToolbar: Toolbar
    private lateinit var editGroupImageView: ImageView
    private lateinit var editGroupName: TextInputEditText
    private lateinit var editGroupDescription: TextInputEditText
    private lateinit var saveGroupChanges: MaterialButton
    private lateinit var cancelButton: MaterialButton
    private lateinit var pickImageButton: View
    private lateinit var loadingOverlay: FrameLayout

    private var newImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            newImageUri = uri
            Picasso.get()
                .load(uri)
                .fit()
                .centerCrop()
                .into(editGroupImageView)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupUI(view)
        observeViewModel()
        setupListeners()
    }
    private fun setupUI(view: View) {
        editGroupToolbar = view.findViewById(R.id.editGroupToolbar)
        editGroupImageView = view.findViewById(R.id.editGroupImageView)
        editGroupName = view.findViewById(R.id.editGroupName)
        editGroupDescription = view.findViewById(R.id.editGroupDescription)
        saveGroupChanges = view.findViewById(R.id.saveGroupChanges)
        cancelButton = view.findViewById(R.id.cancelButton)
        pickImageButton = view.findViewById(R.id.pickImageButton)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)
    }

    private fun observeViewModel() {
        viewModel.loadGroupDetails(args.groupId)

        viewModel.group.observe(viewLifecycleOwner) { group ->
            if(group == null) return@observe

            editGroupName.setText(group.name)
            editGroupDescription.setText(group.description)

            if (group.imageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(group.imageUrl)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.default_group_image)
                    .into(editGroupImageView)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            saveGroupChanges.isEnabled = !isLoading
            cancelButton.isEnabled = !isLoading
        }
    }

    private fun setupListeners() {
        editGroupToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        pickImageButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        saveGroupChanges.setOnClickListener {
            saveGroupChanges()
        }

        cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    private fun saveGroupChanges() {
        val newName = editGroupName.text.toString().trim()
        val newDesc = editGroupDescription.text.toString().trim()

        if (newName.isEmpty() || newDesc.isEmpty()) {
            Toast.makeText(requireContext(), "Fields cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }



        viewModel.updateGroupDetails(args.groupId, newName, newDesc, newImageUri) { success ->
            requireActivity().runOnUiThread {
                if (success) {
                    Toast.makeText(requireContext(), "Group updated successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Update failed! Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
