package com.example.infinityfitness.database.dao
import androidx.room.*
import com.example.infinityfitness.DueCust
import com.example.infinityfitness.database.entity.Customer
import java.util.Date

@Dao
interface CustomerDao {

    @Query("SELECT * FROM Customer WHERE billNo = :billNo")
    suspend fun getCustomerByBillNo(billNo: Int): Customer?

    @Query("SELECT * FROM Customer WHERE activeTill = :dueDate")
    suspend fun getDueCustomers(dueDate: String):List<Customer>?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer) : Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("SELECT * FROM Customer")
    suspend fun getAllCustomers(): List<Customer>

    @Query("SELECT * FROM Customer WHERE isActive = 1 ORDER BY activeTill ASC")
    suspend fun getActiveCustomers(): List<Customer>

    @Query("SELECT * FROM Customer WHERE isActive = 1 AND ( " +
            "(name LIKE '%' || :string || '%' OR (CASE WHEN :isNumeric = 1" +
            " THEN billNo = :numericValue ELSE 0 END) OR " +
            "phoneNumber LIKE '%' || :string || '%')) " +
            "ORDER BY phoneNumber, name ASC LIMIT :limit OFFSET :offset")
    suspend fun getActiveCustomersPaged(
        string: String, isNumeric: Int, numericValue: Int,
        limit: Int, offset: Int): List<Customer>


    @Query(
        "SELECT * FROM Customer WHERE isActive = 1 AND name LIKE  '%'|| :string || '%' or" +
            " billNo = '%'|| :string || '%' OR phoneNumber LIKE '%'|| :string || '%' ORDER" +
            " BY phoneNumber , name ")
    suspend fun queryCustomers(string: String) : List<Customer>

}