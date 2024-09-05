package com.example.infinityfitness.database.dao


import androidx.room.*
import com.example.infinityfitness.database.entity.User

@Dao
interface UserDao {

    @Query("SELECT * FROM User WHERE userId = :userId")
    suspend fun getUserById(userId: Int): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM User WHERE username = :username AND passwordHash = :passwordHash")
    suspend fun authenticate(username: String, passwordHash: String): User?
}