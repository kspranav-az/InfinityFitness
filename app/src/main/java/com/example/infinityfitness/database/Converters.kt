package com.example.infinityfitness.database

import androidx.room.TypeConverter
import com.example.infinityfitness.enums.PaymentMethod
import com.example.infinityfitness.enums.SEX
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
    fun fromGender(value: SEX): String {
        return value.name
    }

    @TypeConverter
    fun toGender(value: String): SEX {
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
}