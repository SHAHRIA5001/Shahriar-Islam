package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
  @PrimaryKey val levelId: Int,
  val isUnlocked: Boolean = false,
  val isCompleted: Boolean = false,
  val starsEarned: Int = 0,
  val bestMoves: Int = 0,
  val bestTimeSeconds: Int = 0,
  val highScore: Int = 0,
  val lastPlayedTimestamp: Long = 0L
)

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
  @PrimaryKey val id: Int = 1,
  val totalStars: Int = 0,
  val currentUnlockedLevel: Int = 1,
  val hintTickets: Int = 5,
  val soundEnabled: Boolean = true,
  val vibrationEnabled: Boolean = true,
  val colorblindMode: Boolean = false
)
