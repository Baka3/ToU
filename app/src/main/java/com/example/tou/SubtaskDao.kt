package com.example.tou

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtaskDao {

    @Query("SELECT * FROM SubtaskEntity WHERE parentNoteId = :noteId")
    fun getByNote(noteId: Int): Flow<List<SubtaskEntity>>
    @Query("SELECT * FROM SubtaskEntity WHERE parentNoteId = :noteId")
    suspend fun getByNoteOnce(noteId: Int): List<SubtaskEntity>
    @Query("DELETE FROM SubtaskEntity WHERE parentNoteId = :noteId")
    suspend fun deleteByNote(noteId: Int)
    @Query("SELECT * FROM SubtaskEntity WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): SubtaskEntity?
    @Insert
    suspend fun insert(subtask: SubtaskEntity)

    @Update
    suspend fun update(subtask: SubtaskEntity)

    @Delete
    suspend fun delete(subtask: SubtaskEntity)
}