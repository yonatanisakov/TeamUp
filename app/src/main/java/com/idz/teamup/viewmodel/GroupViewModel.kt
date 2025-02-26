package com.idz.teamup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idz.teamup.model.Group
import com.idz.teamup.repository.GroupRepo
import kotlinx.coroutines.launch

class GroupViewModel : ViewModel() {
    private val groupRepo = GroupRepo()
    private val _groups = MutableLiveData<List<Group>>()
    val groups: LiveData<List<Group>> get() = _groups
    fun createGroup(group: Group) {
        viewModelScope.launch {
            groupRepo.createGroup(group)
        }
    }
    fun loadGroups() {
        viewModelScope.launch {
            val groupList = groupRepo.getGroups()
            _groups.postValue(groupList)
        }
    }
}
