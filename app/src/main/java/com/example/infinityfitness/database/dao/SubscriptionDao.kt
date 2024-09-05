package com.example.infinityfitness.database.dao


import androidx.room.*
import com.example.infinityfitness.database.entity.Subscription

@Dao
interface SubscriptionDao {

    @Query("SELECT * FROM Subscription WHERE subscriptionId = :subscriptionId")
    suspend fun getSubscriptionById(subscriptionId: Int): Subscription?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: Subscription)

    @Update
    suspend fun updateSubscription(subscription: Subscription)

    @Delete
    suspend fun deleteSubscription(subscription: Subscription)

    @Query("SELECT * FROM Subscription WHERE customerId = :customerId")
    suspend fun getSubscriptionsByCustomerId(customerId: Int): List<Subscription>
}