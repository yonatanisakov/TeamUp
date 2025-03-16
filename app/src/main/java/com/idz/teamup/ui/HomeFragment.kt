package com.idz.teamup.ui
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.idz.teamup.R
import com.idz.teamup.ui.adapters.GroupAdapter
import com.idz.teamup.viewmodel.GroupViewModel

class HomeFragment : Fragment(R.layout.fragment_home) {
    private val groupViewModel: GroupViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: GroupAdapter
    private lateinit var searchEditText: TextInputEditText
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var createFirstGroupButton: MaterialButton
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var filterChipGroup: ChipGroup
    private lateinit var upcomingChip: Chip
    private lateinit var pastChip: Chip
    private lateinit var allChip: Chip

    private var scrollPosition = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI(view)
        observeViewModel()
        setupListeners()

        if (savedInstanceState != null) {
            scrollPosition = savedInstanceState.getInt("SCROLL_POSITION", 0)
        }

        groupViewModel.loadGroups()
    }
    private fun setupUI(view: View) {
        recyclerView = view.findViewById(R.id.groupsRecyclerView)
        searchEditText = view.findViewById(R.id.searchEditText)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer)
        createFirstGroupButton = view.findViewById(R.id.createFirstGroupButton)
        loadingOverlay = view.findViewById(R.id.loadingOverlay)
        filterChipGroup = view.findViewById(R.id.filterChipGroup)
        upcomingChip = view.findViewById(R.id.upcomingChip)
        pastChip = view.findViewById(R.id.pastChip)
        allChip = view.findViewById(R.id.allChip)

        upcomingChip.isChecked = true

        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        groupAdapter = GroupAdapter(emptyList()) { group ->
            if (findNavController().currentDestination?.id == R.id.homeFragment) {
                val action = HomeFragmentDirections.actionHomeFragmentToGroupDetailsFragment(group.groupId)
                findNavController().navigate(action)
            }
        }
        recyclerView.adapter = groupAdapter
    }

    private fun observeViewModel() {
        groupViewModel.filteredGroups.observe(viewLifecycleOwner) { groups ->
            groups?.let {
                groupAdapter.updateGroups(it)

                if (it.isEmpty()) {
                    emptyStateContainer.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE

                    val emptyStateText = view?.findViewById<TextView>(R.id.emptyStateText)
                    emptyStateText?.text = when (groupViewModel.filterMode.value) {
                        "upcoming" ->  "No upcoming groups found"
                        "past" ->  "No past groups found"
                        else ->  "No groups found"
                    }

                } else {
                    emptyStateContainer.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    if (scrollPosition > 0 && !swipeRefreshLayout.isRefreshing && groups.isNotEmpty()) {
                        recyclerView.post {
                            val targetPosition = minOf(scrollPosition, groups.size - 1)
                            if (targetPosition >= 0) {
                                recyclerView.scrollToPosition(targetPosition)
                            }
                        }
                    }
                }

            }
        }

        groupViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (!swipeRefreshLayout.isRefreshing) {
                    loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
                } else if (!isLoading) {
                    swipeRefreshLayout.isRefreshing = false
                }

                createFirstGroupButton.isEnabled = !isLoading
            }
    }

    private fun setupListeners() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                val allGroups = groupViewModel.groups.value ?: emptyList()

                if (query.isEmpty()) {
                    groupAdapter.updateGroups(allGroups)
                } else {
                    val filteredGroups = allGroups.filter {
                        it.name.contains(query, ignoreCase = true) ||
                                it.activityType.contains(query, ignoreCase = true) ||
                                it.location.contains(query, ignoreCase = true)
                    }
                    groupAdapter.updateGroups(filteredGroups)
                }

                updateEmptyStateVisibility()
            }
        })

        upcomingChip.setOnClickListener {
            groupViewModel.setFilterMode("upcoming")
        }

        pastChip.setOnClickListener {
            groupViewModel.setFilterMode("past")
        }

        allChip.setOnClickListener {
            groupViewModel.setFilterMode("all")
        }

        swipeRefreshLayout.setOnRefreshListener {
            scrollPosition = 0
            swipeRefreshLayout.isRefreshing = true
            groupViewModel.loadGroups(forceRefresh = true)
        }
        view?.findViewById<FloatingActionButton>(R.id.createGroupButton)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_createGroupFragment)
        }

        createFirstGroupButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_createGroupFragment)
        }
    }
    private fun updateEmptyStateVisibility() {
        val isEmpty = groupAdapter.itemCount == 0
        emptyStateContainer.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    override fun onPause() {
        super.onPause()
        scrollPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        scrollPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        outState.putInt("SCROLL_POSITION", scrollPosition)
    }

    override fun onResume() {
        super.onResume()
        if (GroupViewModel.refreshGroups || GroupViewModel.updatedGroupId != null)
            groupViewModel.loadGroups(forceRefresh = true)
    }

}

