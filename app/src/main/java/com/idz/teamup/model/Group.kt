package com.idz.teamup.model

import com.idz.teamup.local.entity.GroupEntity

data class Group(
    var groupId: String = "",
    val name: String = "",
    val description: String = "",
    val activityType: String = "",
    val dateTime: String = "",
    val location: String = "",
    var createdBy: String = "",
    var imageUrl: String = "",
    val members: List<String> = listOf(),
    val weather: String = ""
) {
    fun toGroupEntity(): GroupEntity {
        return GroupEntity(
            groupId, name, description, activityType, dateTime, location, createdBy, imageUrl, members, weather
        )
    }
}