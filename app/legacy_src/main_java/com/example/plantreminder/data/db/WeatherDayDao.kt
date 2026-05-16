package com.example.plantreminder.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDayDao {
    @Query("SELECT * FROM weather_days ORDER BY date ASC")
    fun observeAll(): Flow<List<WeatherDayEntity>>

    @Query("SELECT * FROM weather_days ORDER BY date ASC")
    suspend fun getAll(): List<WeatherDayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<WeatherDayEntity>)

    @Query("DELETE FROM weather_days")
    suspend fun deleteAll()
}
