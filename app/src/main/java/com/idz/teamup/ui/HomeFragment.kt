package com.idz.teamup.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.idz.teamup.R
import com.idz.teamup.ui.adapters.GroupAdapter
import com.idz.teamup.viewmodel.GroupViewModel

class HomeFragment : Fragment(R.layout.fragment_home) {
    private val groupViewModel: GroupViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: GroupAdapter
    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.groupsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchView = view.findViewById(R.id.searchView)
        groupAdapter = GroupAdapter(emptyList()) { group ->
            val action = HomeFragmentDirections.actionHomeFragmentToGroupDetailsFragment(group.groupId)
            findNavController().navigate(action)
        }
        recyclerView.adapter = groupAdapter

        groupViewModel.groups.observe(viewLifecycleOwner) { groups ->
            groupAdapter.updateGroups(groups)
        }

        groupViewModel.loadGroups()

        view.findViewById<FloatingActionButton>(R.id.createGroupButton).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_createGroupFragment)
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    groupViewModel.searchGroups(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    groupViewModel.searchGroups(newText)
                }
                return true
            }
        })
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_profile -> {
                        findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}

