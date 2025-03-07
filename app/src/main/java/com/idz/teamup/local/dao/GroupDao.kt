package com.idz.teamup.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*

import com.idz.teamup.local.entity.GroupEntity

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups")
    suspend fun getAllGroupsSync(): List<GroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<GroupEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Query("SELECT * FROM groups ORDER BY dateTime ASC")
    fun getAllGroupsLive(): LiveData<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE groupId = :groupId")
    fun getGroupById(groupId: String): LiveData<GroupEntity?>
    @Query("UPDATE groups SET weather = :weather WHERE groupId = :groupId")
    suspend fun updateWeather(groupId: String, weather: String)
    @Query("DELETE FROM groups")
    suspend fun clearGroups()

}
