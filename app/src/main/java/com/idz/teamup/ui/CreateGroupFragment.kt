package com.idz.teamup.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.idz.teamup.R
import com.idz.teamup.model.Group
import com.idz.teamup.viewmodel.GroupViewModel
import java.util.*


class CreateGroupFragment : Fragment(R.layout.fragment_create_group) {

    private val groupViewModel: GroupViewModel by viewModels()
    private lateinit var dateTimePickerButton: Button
    private var selectedDateTime: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nameInput = view.findViewById<EditText>(R.id.groupNameInput)
        val descriptionInput = view.findViewById<EditText>(R.id.groupDescriptionInput)
        val activityTypeSpinner = view.findViewById<Spinner>(R.id.activityTypeSpinner)
        dateTimePickerButton = view.findViewById<Button>(R.id.dateTimePickerButton)
        val createGroupButton = view.findViewById<Button>(R.id.createGroupButton)

        val activityTypes = listOf("Soccer", "Basketball", "Yoga", "Running")
        activityTypeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, activityTypes)

        dateTimePickerButton.setOnClickListener {
            showDateTimePicker()
        }

        createGroupButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()
            val activityType = activityTypeSpinner.selectedItem.toString()

            if (name.isEmpty() || description.isEmpty() || selectedDateTime.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val group = Group(name = name, description = description, activityType = activityType, dateTime = selectedDateTime)
            groupViewModel.createGroup(group)

            showSuccessDialog()
           // findNavController().navigateUp()
        }
    }
    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        // Show Date Picker
        val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"

            // Show Time Picker after selecting date
            val timePicker = TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                val selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                selectedDateTime = "$selectedDate $selectedTime"
                dateTimePickerButton.text = selectedDateTime // Update button text
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

            timePicker.show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        datePicker.show()
    }
    private fun showSuccessDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_group_created, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.okButton).setOnClickListener {
            dialog.dismiss()
            findNavController().navigateUp() // Go back to home after confirming
        }

        dialog.show()
    }

}
