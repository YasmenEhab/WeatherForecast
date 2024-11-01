package com.example.weatherforecastapplication.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.weatherforecastapplication.model.FavoriteCity

@Database(entities = [FavoriteCity::class], version = 1, exportSchema = false)

abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteCityDao(): FavoriteCityDao

    companion object {
        @Volatile
       private var INSTANCE: AppDatabase? = null

       fun getDatabase(context: Context): AppDatabase {
           return INSTANCE ?: synchronized(this) {
               val instance = Room.databaseBuilder(
                   context.applicationContext,
                   AppDatabase::class.java,
                    "weather_database"
                ).build()
                INSTANCE = instance
               Log.d("AppDatabase", "Database created: ${instance.openHelper.writableDatabase}")
               instance
            }
        }
    }
}