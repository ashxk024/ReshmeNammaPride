package com.example.reshmenammapride.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.reshmenammapride.database.dao.BatchDao
import com.example.reshmenammapride.database.dao.ClimateEntryDao
import com.example.reshmenammapride.database.entity.BatchEntity
import com.example.reshmenammapride.database.entity.ClimateEntryEntity

/**
 * Single Room database for the entire app.
 *
 * Version history:
 *   1 — initial schema (BatchEntity + ClimateEntryEntity)
 *
 * To add a migration later:
 *   .addMigrations(MIGRATION_1_2)
 */
@Database(
    entities = [BatchEntity::class, ClimateEntryEntity::class],
    version  = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun batchDao(): BatchDao
    abstract fun climateEntryDao(): ClimateEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton database instance.
         * Thread-safe via double-checked locking.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reshme_namma_pride.db"
                )
                    .fallbackToDestructiveMigration()   // safe for dev; replace with migrations in prod
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
