package com.locationtracker.mm.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

private const val DATABASE_NAME = "my_location_tracker"
@Database(entities = [LocationEntity::class], version = 1, exportSchema = false)
@TypeConverters(LocationTypeConverters::class)
abstract class LocationTrackerDb :RoomDatabase() {
    abstract fun locationTrackerDao(): LocationTrackerDao

    companion object{
        @Volatile private var INSTANCE: LocationTrackerDb? = null

        fun getInstance(context: Context): LocationTrackerDb {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): LocationTrackerDb {
            return Room.databaseBuilder(
                context,
                LocationTrackerDb::class.java,
                DATABASE_NAME
            ).build()
        }
    }

}