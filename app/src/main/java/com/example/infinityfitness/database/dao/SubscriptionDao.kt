package com.example.infinityfitness.database.dao


import androidx.room.*
import com.example.infinityfitness.database.entity.Subscription

data class RenewalSummary(
    val date: String,
    val count: Int,
    val totalAmount: Double
)

@Dao
interface SubscriptionDao {

    @Query("SELECT * FROM Subscription WHERE customerId = :customerId ORDER BY startDate DESC LIMIT 1")
    suspend fun getSubscriptionById(customerId: Long): Subscription?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: Subscription)

    @Update
    suspend fun updateSubscription(subscription: Subscription)

    @Delete
    suspend fun deleteSubscription(subscription: Subscription)

    @Query("SELECT * FROM Subscription WHERE customerId = :customerId")
    suspend fun getSubscriptionsByCustomerId(customerId: Int): List<Subscription>

    @Query(
        "SELECT date(startDate / 1000, 'unixepoch', 'localtime') as date, " +
                "COUNT(*) as count, SUM(finalPrice) as totalAmount " +
                "FROM Subscription GROUP BY date ORDER BY date DESC"
    )
    suspend fun getRenewalSummary(): List<RenewalSummary>
}