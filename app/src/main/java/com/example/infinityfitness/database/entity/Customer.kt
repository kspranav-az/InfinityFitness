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
    var image: Bitmap? = null,

    @ColumnInfo(name = "isActive")
    var isActive: Boolean = true
) {
    @Ignore
    constructor() : this(100, "", SEX.MALE, null, null, null, true)
}
