package com.idz.teamup.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.idz.teamup.model.Group

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val groupId: String,
    val name: String,
    val description: String,
    val activityType: String,
    val dateTime: String,
    val location: String,
    val createdBy: String,
    val imageUrl: String,
    val members: List<String>,
    val weather: String=""

) {
    fun toGroup(): Group {
        return Group(
            groupId, name, description, activityType, dateTime, location, createdBy, imageUrl, members,weather
        )
    }
}
