package com.idz.teamup.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.idz.teamup.R
import com.idz.teamup.model.Group
import com.idz.teamup.repository.GeoDBRepo
import com.idz.teamup.viewmodel.GroupViewModel
import kotlinx.coroutines.launch
import java.util.*


class CreateGroupFragment : Fragment(R.layout.fragment_create_group) {
    private var isSelecting = false
    private val groupViewModel: GroupViewModel by viewModels()
    private lateinit var dateTimePickerButton: Button
    private var selectedDateTime: String = ""
    private lateinit var cityInput: AutoCompleteTextView
    private lateinit var cityAdapter: ArrayAdapter<String>
    private val cities = mutableListOf<String>()
    private val validCities = mutableSetOf<String>()
    private lateinit var createGroupImageView: ImageView
    private lateinit var pickImageButton: Button
    private var imageUri: Uri? = null
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            createGroupImageView.setImageURI(uri)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val nameInput = view.findViewById<EditText>(R.id.groupNameInput)
        val descriptionInput = view.findViewById<EditText>(R.id.groupDescriptionInput)
        val activityTypeSpinner = view.findViewById<Spinner>(R.id.activityTypeSpinner)
        dateTimePickerButton = view.findViewById(R.id.dateTimePickerButton)
        val createGroupButton = view.findViewById<Button>(R.id.createGroupButton)
        createGroupImageView = view.findViewById(R.id.createGroupImageView)
        pickImageButton = view.findViewById(R.id.pickImageButton)
        cityInput = view.findViewById(R.id.groupLocation)
        cityInput.threshold = 2

        val activityTypes = listOf("Soccer", "Basketball", "Yoga", "Running")
        activityTypeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, activityTypes)

        pickImageButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        dateTimePickerButton.setOnClickListener {
            showDateTimePicker()
        }

        createGroupButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()
            val activityType = activityTypeSpinner.selectedItem.toString()
            val location = cityInput.text.toString().trim()

            if (name.isEmpty() || description.isEmpty() || selectedDateTime.isEmpty()|| location.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!validCities.contains(location)) {
                Toast.makeText(requireContext(), "Please select a valid city from the list", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val group = Group(name = name,location = location,  description = description, activityType = activityType, dateTime = selectedDateTime, imageUrl = imageUri.toString())

            groupViewModel.createGroup(group) { success ->

                if (success) {
                    requireActivity().runOnUiThread {
                        showSuccessDialog()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to create group. Try again!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        cityInput.setOnItemClickListener { parent, _, position, _ ->
            isSelecting = true

            val selectedCity = parent.getItemAtPosition(position) as String
            cityInput.setText(selectedCity, false)
            cityInput.dismissDropDown()
            cityInput.clearFocus()

            cityInput.post { isSelecting = false }
        }





        cityInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("CityAPI", "User typing: $s")

                if (s != null && s.length > 2) {
                    fetchCities(s.toString())
                }
            }
        })
    }
    private fun fetchCities(query: String) {
        if(isSelecting) return
        viewLifecycleOwner.lifecycleScope.launch {
            Log.d("FetchCities", "Fetching cities for query: $query")
            val cityList = GeoDBRepo.getCities(query, "2e6b1dc02dmsh07a36dd56161cbcp16de01jsn2a659fb6544c")

            Log.d("FetchCities", "Updating UI with: $cityList")

            requireActivity().runOnUiThread {
                validCities.clear()
                validCities.addAll(cityList)

                cities.clear()
                cities.addAll(cityList)

                cityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cities)
                cityInput.setAdapter(cityAdapter)
                cityAdapter.notifyDataSetChanged()

                Log.d("FetchCities", "Adapter Count after update: ${cityAdapter.count}")


                        if (cityInput.hasFocus()) {
                            cityInput.showDropDown()
                }
            }
        }
    }




    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"

            val timePicker = TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                val selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                selectedDateTime = "$selectedDate $selectedTime"
                dateTimePickerButton.text = selectedDateTime
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
            findNavController().navigateUp()
        }

        dialog.show()
    }

}
