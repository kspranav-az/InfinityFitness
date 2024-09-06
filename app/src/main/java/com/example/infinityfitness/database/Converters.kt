package com.example.infinityfitness.database

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import com.example.infinityfitness.enums.PaymentMethod
import com.example.infinityfitness.enums.SEX
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.*

class Converters {

    // Date converters
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // Enum converters for SEX
    @TypeConverter
    fun fromSEX(value: SEX): String {
        return value.name
    }

    @TypeConverter
    fun toSEX(value: String): SEX {
        return SEX.valueOf(value)
    }

    // Enum converters for PaymentMethod
    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String {
        return value.name
    }

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod {
        return PaymentMethod.valueOf(value)
    }

    // Bitmap converters
    @TypeConverter
    fun fromBitmap(bitmap: Bitmap?): ByteArray? {
        if (bitmap == null) return null
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray?): Bitmap? {
        return byteArray?.let {
            BitmapFactory.decodeByteArray(it, 0, it.size)
        }
    }

    // Password hashing
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
