package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BaccaratRepository(private val baccaratDao: BaccaratDao) {
    val allShoes: Flow<List<BaccaratShoe>> = baccaratDao.getAllShoes()

    suspend fun getShoe(id: Long): BaccaratShoe? = withContext(Dispatchers.IO) {
        baccaratDao.getShoeById(id)
    }

    suspend fun insertShoe(shoe: BaccaratShoe): Long = withContext(Dispatchers.IO) {
        baccaratDao.insertShoe(shoe)
    }

    suspend fun deleteShoe(id: Long) = withContext(Dispatchers.IO) {
        baccaratDao.deleteShoeById(id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        baccaratDao.deleteAllShoes()
    }
}
