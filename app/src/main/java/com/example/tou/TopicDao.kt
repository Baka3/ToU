package com.example.tou

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {

    @Query("SELECT name FROM CustomTopicEntity")
    fun getAll(): Flow<List<String>>

    @Insert
    suspend fun insert(topic: CustomTopicEntity)

    @Query("DELETE FROM CustomTopicEntity WHERE name = :name")
    suspend fun deleteByName(name: String)
}