package com.example.infinityfitness.database.dao
import androidx.room.*
import com.example.infinityfitness.DueCust
import com.example.infinityfitness.database.entity.Customer
import java.util.Date

@Dao
interface CustomerDao {

    @Query("SELECT * FROM Customer WHERE billNo = :billNo")
    suspend fun getCustomerByBillNo(billNo: Long): Customer?

    @Query("SELECT * FROM Customer WHERE activeTill <= :dueDate")
    suspend fun getDueCustomers(dueDate: Date):List<Customer>?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer) : Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("SELECT * FROM Customer")
    suspend fun getAllCustomers(): List<Customer>

    @Query("SELECT * FROM Customer WHERE isActive = 1 AND ( " +
            "(name LIKE '%' || :string || '%' OR (CASE WHEN :isNumeric = 1" +
            " THEN billNo = :numericValue ELSE 0 END ))) " +
            "ORDER BY billNo , name ASC ")
    suspend fun getActiveCustomers(
        string: String, isNumeric: Int, numericValue: Int ): List<Customer>

    @Query("SELECT * FROM Customer WHERE isActive = 1 AND ( " +
            "(name LIKE '%' || :string || '%' OR (CASE WHEN :isNumeric = 1" +
            " THEN billNo = :numericValue ELSE 0 END ))) " +
            "ORDER BY billNo , name ASC LIMIT :limit OFFSET :offset")
    suspend fun getActiveCustomersPaged(
        string: String, isNumeric: Int, numericValue: Int,
        limit: Int, offset: Int): List<Customer>


    @Query(
        "SELECT * FROM Customer WHERE isActive = 1 AND name LIKE  '%'|| :string || '%' or" +
            " billNo = '%'|| :string || '%' OR phoneNumber LIKE '%'|| :string || '%' ORDER" +
            " BY phoneNumber , name ")
    suspend fun queryCustomers(string: String) : List<Customer>

    @Query("SELECT MAX(billNo) FROM Customer")
    fun getLastPrimaryKey(): Long?

}