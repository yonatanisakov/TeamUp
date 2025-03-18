package com.idz.teamup.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.idz.teamup.R
import com.idz.teamup.ui.adapters.MyGroupsAdapter
import com.idz.teamup.viewmodel.GroupViewModel
import com.idz.teamup.viewmodel.MyGroupsViewModel

class MyGroupsFragment : Fragment(R.layout.fragment_my_groups) {

    private val viewModel: MyGroupsViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var createFirstGroupButton: MaterialButton
    private lateinit var createGroupButton: FloatingActionButton
    private lateinit var loadingOverlay: FrameLayout

    private lateinit var adapter: MyGroupsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI(view)
        observeViewModel()
        setupListeners()
    }
    private fun setupUI(view: View) {
        recyclerView = view.findViewById(R.id.myGroupsRecyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer)
        createFirstGroupButton = view.findViewById(R.id.createFirstGroupButton)
        createGroupButton = view.findViewById(R.id.createGroupButton)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)

        adapter = MyGroupsAdapter(
            emptyList(),
            onItemClick = { group ->
                val action = MyGroupsFragmentDirections.actionMyGroupsFragmentToGroupDetailsFragment(group.groupId)
                findNavController().navigate(action)
            },
            onManageClick = { group ->
                showManageOptionsDialog(group.groupId, group.name)
            }
        )

        recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.myGroups.observe(viewLifecycleOwner) { groups ->
            adapter.updateGroups(groups)

            if (groups.isEmpty()) {
                emptyStateContainer.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyStateContainer.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (!swipeRefreshLayout.isRefreshing) {
                loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            } else if (!isLoading) {
                // If we were doing a pull-to-refresh and loading finished, hide the refresh
                swipeRefreshLayout.isRefreshing = false
            }
            createFirstGroupButton.isEnabled = !isLoading
            createGroupButton.isEnabled = !isLoading
        }
    }

    private fun setupListeners() {
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true

            viewModel.loadMyGroups(forceRefresh = true)
        }

        val createGroupClickListener = View.OnClickListener {
            findNavController().navigate(R.id.action_myGroupsFragment_to_createGroupFragment)
        }

        createFirstGroupButton.setOnClickListener(createGroupClickListener)
        createGroupButton.setOnClickListener(createGroupClickListener)
    }
    private fun showManageOptionsDialog(groupId: String, groupName: String) {
        val options = arrayOf("Edit Group", "Delete Group")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Manage \"$groupName\"")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        val action = MyGroupsFragmentDirections.actionMyGroupsFragmentToEditGroupFragment(groupId)
                        findNavController().navigate(action)
                    }
                    1 -> {
                        showDeleteConfirmationDialog(groupId, groupName)
                    }
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun showDeleteConfirmationDialog(groupId: String, groupName: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_delete, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.confirmDeleteButton).setOnClickListener {
            dialog.dismiss()
            deleteGroup(groupId)
        }

        dialog.show()
    }

    private fun deleteGroup(groupId: String) {

        viewModel.deleteGroup(groupId) { success ->
            requireActivity().runOnUiThread {

                if (success) {
                    viewModel.loadMyGroups(true)
                    Toast.makeText(requireContext(), "Group deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete the group", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadMyGroups(false)

    }

}