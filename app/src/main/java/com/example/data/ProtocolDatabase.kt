package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DripProtocolEntity::class, SyringeProtocolEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ProtocolDatabase : RoomDatabase() {
    abstract fun protocolDao(): ProtocolDao

    companion object {
        @Volatile
        private var INSTANCE: ProtocolDatabase? = null

        fun getInstance(context: Context): ProtocolDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ProtocolDatabase::class.java,
                    "medic_protocols.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
