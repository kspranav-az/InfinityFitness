package com.example.infinityfitness.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Ignore

@Entity(
    indices = [
        Index(value = ["userId"])
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "userId")
    var userId: Int = 0,

    @ColumnInfo(name = "username")
    var username: String = "",

    @ColumnInfo(name = "passwordHash")
    var passwordHash: String = "",

    @ColumnInfo(name = "useFingerprint")
    var useFingerprint: Boolean = false
) {
    @Ignore
    constructor() : this(0, "", "", false)
}
