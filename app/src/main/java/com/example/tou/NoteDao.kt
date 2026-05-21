package com.example.tou
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM NoteEntity WHERE done = 1 ORDER BY completedAt DESC")
    fun getCompleted(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity")
    fun getAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE date != ''")
    fun getWithDate(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): NoteEntity?

    @Query("SELECT DISTINCT topic FROM NoteEntity WHERE topic != '' AND done = 0")
    fun getTopics(): Flow<List<String>>

    @Query("SELECT * FROM NoteEntity WHERE topic = :topic AND done = 0")
    fun getByTopic(topic: String): Flow<List<NoteEntity>>
    @Query("SELECT name FROM CustomTopicEntity")
    fun getCustomTopics(): Flow<List<String>>
    @Query("SELECT * FROM NoteEntity WHERE done = 0 ORDER BY `order` ASC")
    fun getActive(): Flow<List<NoteEntity>>

    @Query("UPDATE NoteEntity SET `order` = :order WHERE id = :id")
    suspend fun updateOrder(id: Int, order: Int)

    @Insert
    suspend fun insertCustomTopic(topic: CustomTopicEntity)
    @Insert
    suspend fun insert(note: NoteEntity): Long
    @Delete
    suspend fun delete(note: NoteEntity)
    @Update
    suspend fun update(note: NoteEntity)
}