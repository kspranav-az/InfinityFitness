package com.example.infinityfitness.database.entity
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Ignore
import com.example.infinityfitness.enums.PaymentMethod
import java.util.Date

@Entity(
    indices = [
        Index(value = ["subscriptionId"]),
        Index(value = ["customerId"]),
        Index(value = ["packId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["billNo"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Pack::class,
            parentColumns = ["packId"],
            childColumns = ["packId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Subscription(

    @ColumnInfo(name = "customerId")
    var customerId: Long = 0,

    @ColumnInfo(name = "packId")
    var packId: Long? = null,

    @ColumnInfo(name = "startDate")
    var startDate: Date = Date(),

    @ColumnInfo(name = "endDate")
    var endDate: Date = Date(),

    @ColumnInfo(name = "finalPrice")
    var finalPrice: Double = 0.0,

    @ColumnInfo(name = "paymentMethod")
    var paymentMethod: PaymentMethod = PaymentMethod.CASH,

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "subscriptionId")
    var subscriptionId: Long = 0
) {
    @Ignore
    constructor() : this( 0, null, Date(), Date(), 0.0, PaymentMethod.CASH, 0)
}

