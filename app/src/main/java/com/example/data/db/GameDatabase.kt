package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [LevelProgressEntity::class, UserPreferencesEntity::class],
  version = 1,
  exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
  abstract fun levelProgressDao(): LevelProgressDao

  companion object {
    @Volatile
    private var INSTANCE: GameDatabase? = null

    fun getDatabase(context: Context): GameDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          GameDatabase::class.java,
          "arrow_escape_game.db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
