package com.team21.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team21.myapplication.data.local.entity.HousingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HousingDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertHousings(housings: List<HousingEntity>)

    @Query("SELECT * FROM housings WHERE isRecommended = 1")
    fun getRecommendedHousings(): Flow<List<HousingEntity>>

    @Query("SELECT * FROM housings WHERE isRecentlySeen = 1")
    fun getRecentlySeenHousings(): Flow<List<HousingEntity>>

    @Query("DELETE FROM housings WHERE isRecommended = 1")
    suspend fun deleteRecommendedHousings()

    @Query("DELETE FROM housings WHERE isRecentlySeen = 1")
    suspend fun deleteRecentlySeenHousings()
}