package com.example.infinityfitness.database.dao

import androidx.room.*
import com.example.infinityfitness.database.entity.Pack

@Dao
interface PackDao {

    @Query("SELECT * FROM Pack WHERE packId = :packId")
    suspend fun getPackById(packId: Int): Pack?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPack(pack: Pack)

    @Update
    suspend fun updatePack(pack: Pack)

    @Delete
    suspend fun deletePack(pack: Pack)

    @Query("SELECT * FROM Pack")
    suspend fun getAllPacks(): List<Pack>
}