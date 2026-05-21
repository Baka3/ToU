package com.example.tou

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {

    @Insert
    suspend fun insert(topic: CustomTopicEntity)

    @Query("DELETE FROM CustomTopicEntity WHERE name = :name")
    suspend fun deleteByName(name: String)
    @Query("SELECT name FROM CustomTopicEntity ORDER BY `order` ASC")
    fun getAll(): Flow<List<String>>

    @Query("UPDATE CustomTopicEntity SET `order` = :order WHERE name = :name")
    suspend fun updateOrder(name: String, order: Int)
}