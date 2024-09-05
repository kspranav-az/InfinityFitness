package com.example.infinityfitness.database.dao
import androidx.room.*
import com.example.infinityfitness.database.entity.Customer

@Dao
interface CustomerDao {

    @Query("SELECT * FROM Customer WHERE billNo = :billNo")
    suspend fun getCustomerByBillNo(billNo: Int): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("SELECT * FROM Customer")
    suspend fun getAllCustomers(): List<Customer>

    @Query("SELECT * FROM Customer WHERE isActive = 1")
    suspend fun getActiveCustomers(): List<Customer>
}