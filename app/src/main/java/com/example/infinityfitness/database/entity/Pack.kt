package com.example.infinityfitness.database.entity


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Ignore

@Entity(
    indices = [
        Index(value = ["packId"])
    ]
)
data class Pack(
    @ColumnInfo(name = "type")
    var type: String = "",

    @ColumnInfo(name = "duration")
    var duration: Int = 0,  // Duration in days

    @ColumnInfo(name = "cost")
    var cost: Double = 0.0,

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "packId")
    var packId: Int = 0,
) {
    @Ignore
    constructor() : this( "", 0, 0.0,0)
}
