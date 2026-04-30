package com.example.tou
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM NoteEntity")
    fun getAll(): Flow<List<NoteEntity>>

    @Insert
    suspend fun insert(note: NoteEntity)
    @Delete
    suspend fun delete(note: NoteEntity)
    @Update
    suspend fun update(note: NoteEntity)
}