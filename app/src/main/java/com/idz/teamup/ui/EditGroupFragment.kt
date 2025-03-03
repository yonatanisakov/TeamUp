package com.idz.teamup.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.idz.teamup.R
import com.idz.teamup.viewmodel.GroupDetailsViewModel
import com.squareup.picasso.Picasso

class EditGroupFragment : Fragment(R.layout.fragment_edit_group) {

    private val args: EditGroupFragmentArgs by navArgs()
    private val viewModel: GroupDetailsViewModel by viewModels()
    private lateinit var editGroupImageView: ImageView
    private lateinit var pickImageButton: Button
    private var newImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            newImageUri = uri
            editGroupImageView.setImageURI(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nameInput = view.findViewById<EditText>(R.id.editGroupName)
        val descInput = view.findViewById<EditText>(R.id.editGroupDescription)
        val saveButton = view.findViewById<Button>(R.id.saveGroupChanges)
        editGroupImageView = view.findViewById(R.id.editGroupImageView)
        pickImageButton = view.findViewById(R.id.pickImageButton)

        viewModel.loadGroupDetails(args.groupId)

        viewModel.group.observe(viewLifecycleOwner) { group ->
            if(group == null) return@observe
            nameInput.setText(group.name)
            descInput.setText(group.description )
            if (group.imageUrl.isNotEmpty()) {
                Picasso.get().load(group.imageUrl).into(editGroupImageView)
            }
        }

        pickImageButton.setOnClickListener {
            pickImage.launch("image/*")
        }
        saveButton.setOnClickListener {
            val newName = nameInput.text.toString().trim()
            val newDesc = descInput.text.toString().trim()

            if (newName.isEmpty() || newDesc.isEmpty()) {
                Toast.makeText(requireContext(), "Fields cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateGroupDetails(args.groupId, newName, newDesc,newImageUri) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Group updated!", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                } else {
                    Toast.makeText(requireContext(), "Update failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
