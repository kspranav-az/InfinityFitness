package com.example.infinityfitness.database.entity
import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Ignore
import com.example.infinityfitness.enums.SEX

@Entity(
    indices = [
        Index(value = ["billNo"])
    ]
)
data class Customer(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "billNo")
    var billNo: Int = 100,

    @ColumnInfo(name = "name")
    var name: String = "",

    @ColumnInfo(name = "gender")
    var gender: SEX = SEX.MALE,

    @ColumnInfo(name = "address")
    var address: String? = null,

    @ColumnInfo(name = "phoneNumber")
    var phoneNumber: String? = null,

    @ColumnInfo(name = "image", typeAffinity = ColumnInfo.BLOB)
    var image: ByteArray? = null,

    @ColumnInfo(name = "isActive")
    var isActive: Boolean = true
) {
    @Ignore
    constructor() : this(100, "", SEX.MALE, null, null, null, true)

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
            if (!image.contentEquals(other.image)) return false
        } else if (other.image != null) return false
        if (isActive != other.isActive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = billNo
        result = 31 * result + name.hashCode()
        result = 31 * result + gender.hashCode()
        result = 31 * result + (address?.hashCode() ?: 0)
        result = 31 * result + (phoneNumber?.hashCode() ?: 0)
        result = 31 * result + (image?.contentHashCode() ?: 0)
        result = 31 * result + isActive.hashCode()
        return result
    }
}
