package com.example.tou
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM NoteEntity WHERE done = 0")
    fun getActive(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE done = 1 ORDER BY completedAt DESC")
    fun getCompleted(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity")
    fun getAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): NoteEntity?

    @Insert
    suspend fun insert(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)
}