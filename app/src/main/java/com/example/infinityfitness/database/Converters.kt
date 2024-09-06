package com.example.infinityfitness.database

import androidx.room.TypeConverter
import com.example.infinityfitness.enums.PaymentMethod
import com.example.infinityfitness.enums.SEX
import java.security.MessageDigest
import java.util.*

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromSEX(value: SEX): String {
        return value.name
    }

    @TypeConverter
    fun toSEX(value: String): SEX {
        return SEX.valueOf(value)
    }

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String {
        return value.name
    }

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod {
        return PaymentMethod.valueOf(value)
    }

    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") {
            "%02x".format(it)  // Convert each byte to a hex string
        }
    }

    // Function to compare plain password with the hashed password
    fun isPasswordValid(plainPassword: String, hashedPassword: String): Boolean {
        return hashPassword(plainPassword) == hashedPassword
    }
}