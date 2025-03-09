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
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.idz.teamup.R
import com.idz.teamup.model.Group
import com.idz.teamup.repository.GeoDBRepo
import com.idz.teamup.viewmodel.GroupViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class CreateGroupFragment : Fragment(R.layout.fragment_create_group) {
    private var isSelecting = false
    private val groupViewModel: GroupViewModel by viewModels()

    private lateinit var createGroupToolbar: MaterialToolbar
    private lateinit var dateTimePickerButton: TextInputEditText
    private lateinit var cityInput: AutoCompleteTextView
    private lateinit var activityTypeDropdown: AutoCompleteTextView
    private lateinit var nameInput: TextInputEditText
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var createGroupButton: MaterialButton
    private lateinit var createGroupImageView: ImageView
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var pickImageButton: View

    private var selectedDateTime: String = ""
    private lateinit var cityAdapter: ArrayAdapter<String>
    private val cities = mutableListOf<String>()
    private val validCities = mutableSetOf<String>()
    private var imageUri: Uri? = null
    private var searchJob: Job? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            Picasso.get()
                .load(uri)
                .fit()
                .centerCrop()
                .into(createGroupImageView)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupUI(view)
        setupListeners()
        observeViewModel()
    }


    private fun setupUI(view: View) {
        createGroupToolbar = view.findViewById(R.id.createGroupToolbar)
        nameInput = view.findViewById(R.id.groupNameInput)
        descriptionInput = view.findViewById(R.id.groupDescriptionInput)
        activityTypeDropdown = view.findViewById(R.id.activityTypeDropdown)
        dateTimePickerButton = view.findViewById(R.id.dateTimePickerButton)
        createGroupButton = view.findViewById(R.id.createGroupButton)
        createGroupImageView = view.findViewById(R.id.createGroupImageView)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)
        pickImageButton = view.findViewById(R.id.pickImageButton)
        cityInput = view.findViewById(R.id.groupLocation)
        cityInput.threshold = 2

        val activityTypes = listOf("Soccer", "Basketball", "Yoga", "Running", "Hiking", "Cycling", "Swimming", "Book Club", "Study Group", "Gaming")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, activityTypes)
        activityTypeDropdown.setAdapter(adapter)
    }

    private fun observeViewModel() {
        groupViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            createGroupButton.isEnabled = !isLoading
        }
    }

    private fun setupListeners() {
        createGroupToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        pickImageButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        dateTimePickerButton.setOnClickListener {
            showDateTimePicker()
        }

        createGroupButton.setOnClickListener {
            createGroup()
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
                if (s != null && s.length > 2) {
                    searchJob?.cancel()

                    searchJob = lifecycleScope.launch {
                        delay(300)
                        if (isSelecting) return@launch
                        fetchCities(s.toString())
                    }
                }
            }
        })
    }

    private fun createGroup() {
        val name = nameInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val activityType = activityTypeDropdown.text.toString().trim()
        val location = cityInput.text.toString().trim()

        if (name.isEmpty() || description.isEmpty() || selectedDateTime.isEmpty() || location.isEmpty() || activityType.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!validCities.contains(location)) {
            Toast.makeText(requireContext(), "Please select a valid city from the list", Toast.LENGTH_SHORT).show()
            return
        }


        val group = Group(
            name = name,
            location = location,
            description = description,
            activityType = activityType,
            dateTime = selectedDateTime,
            imageUrl = imageUri?.toString() ?: ""
        )

        groupViewModel.createGroup(group) { success ->
            requireActivity().runOnUiThread {
                if (success) {
                    showSuccessDialog()
                } else {
                    Toast.makeText(requireContext(), "Failed to create group. Try again!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun fetchCities(query: String) {
        if (isSelecting) return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val cityList =
                    GeoDBRepo.getCities(query, "2e6b1dc02dmsh07a36dd56161cbcp16de01jsn2a659fb6544c")

                if (!isAdded) return@launch
                requireActivity().runOnUiThread {
                    validCities.clear()
                    validCities.addAll(cityList)

                    cities.clear()
                    cities.addAll(cityList)

                    cityAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        cities
                    )
                    cityInput.setAdapter(cityAdapter)
                    cityAdapter.notifyDataSetChanged()
                    if (cityInput.hasFocus()) {
                        cityInput.showDropDown()
                    }
                }
            } catch (e: Exception) {
                Log.e("FetchCities", "Error fetching cities: ${e.message}", e)
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "Error fetching cities. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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
                dateTimePickerButton.setText(selectedDateTime)
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
