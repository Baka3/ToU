package com.example.tou

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {

    @Insert
    suspend fun insert(topic: CustomTopicEntity)

    @Query("DELETE FROM CustomTopicEntity WHERE name = :name")
    suspend fun deleteByName(name: String)
    @Query("SELECT name FROM CustomTopicEntity ORDER BY `order` DESC")
    fun getAll(): Flow<List<String>>

    @Query("UPDATE CustomTopicEntity SET `order` = :order WHERE name = :name")
    suspend fun updateOrder(name: String, order: Int)
    @Query("UPDATE CustomTopicEntity SET name = :newName WHERE name = :oldName")
    suspend fun rename(oldName: String, newName: String)
    @Query("SELECT COUNT(*) FROM CustomTopicEntity WHERE name = :name")
    suspend fun exists(name: String): Int
    @Query("SELECT MAX(`order`) FROM CustomTopicEntity")
    suspend fun getMaxOrder(): Int?
}