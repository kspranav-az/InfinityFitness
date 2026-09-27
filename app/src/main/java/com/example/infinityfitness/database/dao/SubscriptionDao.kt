package com.example.infinityfitness.database.dao


import android.graphics.Bitmap
import androidx.room.*
import com.example.infinityfitness.database.entity.Subscription
import com.example.infinityfitness.enums.PaymentMethod
import java.util.Date

data class RenewalDetail(
    val subscriptionId: Long,
    val customerId: Long,
    val customerName: String,
    val billNo: Long,
    val displayBillNo: String,
    val phoneNumber: String?,
    val customerImage: Bitmap?,
    val startDate: Date,
    val endDate: Date,
    val finalPrice: Double,
    val paymentMethod: PaymentMethod,
    val packType: String?
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
        "SELECT s.subscriptionId, s.customerId, c.name as customerName, c.billNo, c.displayBillNo, c.phoneNumber, " +
                "c.image as customerImage, s.startDate, s.endDate, s.finalPrice, s.paymentMethod, p.type as packType " +
                "FROM Subscription s " +
                "LEFT JOIN Customer c ON s.customerId = c.billNo " +
                "LEFT JOIN Pack p ON s.packId = p.packId " +
                "WHERE ( :searchQuery = '' OR " +
                "c.name LIKE '%' || :searchQuery || '%' OR " +
                "c.displayBillNo LIKE '%' || :searchQuery || '%' OR " +
                "c.phoneNumber LIKE '%' || :searchQuery || '%' OR " +
                "( :numericSearch >= 0 AND c.billNo = :numericSearch ) ) " +
                "ORDER BY s.startDate DESC " +
                "LIMIT :limit OFFSET :offset"
    )
    suspend fun getAllRenewalDetailsPaged(
        searchQuery: String,
        numericSearch: Long,
        limit: Int,
        offset: Int
    ): List<RenewalDetail>

    @Query(
        "SELECT s.subscriptionId, s.customerId, c.name as customerName, c.billNo, c.displayBillNo, c.phoneNumber, " +
                "c.image as customerImage, s.startDate, s.endDate, s.finalPrice, s.paymentMethod, p.type as packType " +
                "FROM Subscription s " +
                "LEFT JOIN Customer c ON s.customerId = c.billNo " +
                "LEFT JOIN Pack p ON s.packId = p.packId " +
                "WHERE date(s.startDate / 1000, 'unixepoch', 'localtime') BETWEEN :startDate AND :endDate " +
                "AND ( :searchQuery = '' OR " +
                "c.name LIKE '%' || :searchQuery || '%' OR " +
                "c.displayBillNo LIKE '%' || :searchQuery || '%' OR " +
                "c.phoneNumber LIKE '%' || :searchQuery || '%' OR " +
                "( :numericSearch >= 0 AND c.billNo = :numericSearch ) ) " +
                "ORDER BY s.startDate DESC " +
                "LIMIT :limit OFFSET :offset"
    )
    suspend fun getRenewalDetailsByDateRangePaged(
        startDate: String,
        endDate: String,
        searchQuery: String,
        numericSearch: Long,
        limit: Int,
        offset: Int
    ): List<RenewalDetail>
}
