package com.luma.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PlanItemEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class LumaDatabase : RoomDatabase() {
    abstract fun planItemDao(): PlanItemDao

    companion object {
        @Volatile
        private var instance: LumaDatabase? = null

        fun getInstance(context: Context): LumaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LumaDatabase::class.java,
                    "luma-planning.db",
                ).fallbackToDestructiveMigration(false).build().also { instance = it }
            }
    }
}
