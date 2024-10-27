package com.example.infinityfitness.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.infinityfitness.database.dao.CustomerDao
import com.example.infinityfitness.database.dao.PackDao
import com.example.infinityfitness.database.dao.SubscriptionDao
import com.example.infinityfitness.database.dao.UserDao
import com.example.infinityfitness.database.entity.Customer
import com.example.infinityfitness.database.entity.Pack
import com.example.infinityfitness.database.entity.Subscription
import com.example.infinityfitness.database.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Customer::class, Pack::class, Subscription::class, User::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GymDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun packDao(): PackDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: GymDatabase? = null

        fun getDatabase(context: Context): GymDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymDatabase::class.java,
                    "gym_database"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate the database in a background thread
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    populateDatabase(database.userDao(), database.packDao())
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }


        // Function to populate the database with predefined data
        suspend fun populateDatabase(userDao: UserDao, packDao: PackDao) {
            // Add one User
            val user = User(
                userId = 1,
                username = "admin",
                passwordHash = Converters().hashPassword("admin123"),
                useFingerprint = true
            )
            userDao.insertUser(user)

            // Add six predefined Packs
            val packs = listOf(
                Pack(packId = 1, duration = 30, cost = 1000.0, type = "1 Month"),
                Pack(packId = 2, duration = 90, cost = 2000.0 , type = "3 Month"),
                Pack(packId = 3, duration = 180, cost = 4000.0 , type = "6 Month" ),
                Pack(packId = 4, duration = 365, cost = 8000.0 , type = "1 Year"),
                Pack(packId = 5, duration = 7, cost = 3000.0 , type = "4 Month"),
                Pack(packId = 6, duration = 15, cost = 200.0, type = "1 Day")
            )
            packs.forEach { pack ->
                packDao.insertPack(pack)
            }
        }
    }

    fun destroyInstance() {
        if (INSTANCE!!.isOpen) INSTANCE!!.close()
        INSTANCE = null
    }
}
