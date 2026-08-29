package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelProgressDao {
  @Query("SELECT * FROM level_progress ORDER BY levelId ASC")
  fun getAllProgress(): Flow<List<LevelProgressEntity>>

  @Query("SELECT * FROM level_progress WHERE levelId = :levelId")
  suspend fun getProgressForLevel(levelId: Int): LevelProgressEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateProgress(progress: LevelProgressEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(progressList: List<LevelProgressEntity>)

  @Query("SELECT * FROM user_preferences WHERE id = 1")
  fun getUserPreferences(): Flow<UserPreferencesEntity?>

  @Query("SELECT * FROM user_preferences WHERE id = 1")
  suspend fun getUserPreferencesSync(): UserPreferencesEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveUserPreferences(prefs: UserPreferencesEntity)
}
