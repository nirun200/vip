package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BaccaratDao {
    @Query("SELECT * FROM baccarat_shoes ORDER BY createdAt DESC")
    fun getAllShoes(): Flow<List<BaccaratShoe>>

    @Query("SELECT * FROM baccarat_shoes WHERE id = :id LIMIT 1")
    suspend fun getShoeById(id: Long): BaccaratShoe?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoe(shoe: BaccaratShoe): Long

    @Query("DELETE FROM baccarat_shoes WHERE id = :id")
    suspend fun deleteShoeById(id: Long)

    @Query("DELETE FROM baccarat_shoes")
    suspend fun deleteAllShoes()
}
