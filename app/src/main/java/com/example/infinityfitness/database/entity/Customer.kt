package com.example.infinityfitness.database.entity
import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Ignore
import com.example.infinityfitness.enums.PaymentMethod
import com.example.infinityfitness.enums.SEX
import java.util.Date

@Entity(
    indices = [
        Index(value = ["billNo"])
    ]
)
data class Customer(

    @ColumnInfo(name = "name")
    var name: String = "",

    @ColumnInfo(name = "gender")
    var gender: SEX = SEX.MALE,

    @ColumnInfo(name = "age")
    var age: Int = 0,

    @ColumnInfo(name = "address")
    var address: String? = null,

    @ColumnInfo(name = "phoneNumber")
    var phoneNumber: String? = null,

    @ColumnInfo(name = "image", typeAffinity = ColumnInfo.BLOB)
    var image: Bitmap? = null,

    @ColumnInfo(name = "JoiningDate")
    var joiningDate: Date = Date(),

    @ColumnInfo(name = "lastPack")
    var lastPack: String? = null,

    @ColumnInfo(name = "activeTill")
    var activeTill: Date = Date(),

    @ColumnInfo(name = "isActive")
    var isActive: Boolean = true,

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "billNo")
    var billNo: Long = 0
) {
    @Ignore
    constructor() : this( "", SEX.MALE, 0,null, null, null,Date(),"" , Date() ,true ,100)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Customer

        if (billNo != other.billNo) return false
        if (name != other.name) return false
        if (gender != other.gender) return false
        if (address != other.address) return false
        if (phoneNumber != other.phoneNumber) return false
        if (image != null) {
            if (other.image == null) return false
            if (image != other.image) return false
        } else if (other.image != null) return false
        if (isActive != other.isActive) return false

        return true
    }

    override fun hashCode(): Int {
        var result =  billNo.toInt()
        result = 31 * result + name.hashCode()
        result = 31 * result + gender.hashCode()
        result = 31 * result + (address?.hashCode() ?: 0)
        result = 31 * result + (phoneNumber?.hashCode() ?: 0)
        result = 31 * result + (image?.hashCode() ?: 0)
        result = 31 * result + isActive.hashCode()
        return result
    }
}
