package com.idz.teamup.model

data class Group(
    val groupId: String = "",
    val name: String = "",
    val description: String = "",
    val activityType: String = "",
    val location: String = "",
    val dateTime: String = "",
    val imageUrl: String = "",
    val createdBy: String = "",
    val members: List<String> = emptyList()
)
