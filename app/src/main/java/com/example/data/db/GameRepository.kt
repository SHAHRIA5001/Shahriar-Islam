package com.example.data.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val dao: LevelProgressDao) {
  val allProgress: Flow<List<LevelProgressEntity>> = dao.getAllProgress()
  val userPreferences: Flow<UserPreferencesEntity> = dao.getUserPreferences().map { it ?: UserPreferencesEntity() }

  suspend fun getProgressForLevel(levelId: Int): LevelProgressEntity? {
    return dao.getProgressForLevel(levelId)
  }

  suspend fun recordLevelResult(
    levelId: Int,
    starsEarned: Int,
    moves: Int,
    timeSeconds: Int,
    score: Int,
    totalLevelsCount: Int
  ) {
    val existing = dao.getProgressForLevel(levelId)
    val bestMoves = if (existing != null && existing.bestMoves > 0) minOf(existing.bestMoves, moves) else moves
    val bestTime = if (existing != null && existing.bestTimeSeconds > 0) minOf(existing.bestTimeSeconds, timeSeconds) else timeSeconds
    val bestStars = if (existing != null) maxOf(existing.starsEarned, starsEarned) else starsEarned
    val bestScore = if (existing != null) maxOf(existing.highScore, score) else score

    dao.insertOrUpdateProgress(
      LevelProgressEntity(
        levelId = levelId,
        isUnlocked = true,
        isCompleted = true,
        starsEarned = bestStars,
        bestMoves = bestMoves,
        bestTimeSeconds = bestTime,
        highScore = bestScore,
        lastPlayedTimestamp = System.currentTimeMillis()
      )
    )

    // Unlock next level if exists
    if (levelId < totalLevelsCount) {
      val nextLevel = levelId + 1
      val nextExisting = dao.getProgressForLevel(nextLevel)
      if (nextExisting == null || !nextExisting.isUnlocked) {
        dao.insertOrUpdateProgress(
          nextExisting?.copy(isUnlocked = true) ?: LevelProgressEntity(
            levelId = nextLevel,
            isUnlocked = true,
            isCompleted = false
          )
        )
      }
    }

    // Update user preferences & total stars
    val currentPrefs = dao.getUserPreferencesSync() ?: UserPreferencesEntity()
    val nextUnlocked = if (levelId < totalLevelsCount) maxOf(currentPrefs.currentUnlockedLevel, levelId + 1) else currentPrefs.currentUnlockedLevel
    dao.saveUserPreferences(
      currentPrefs.copy(
        currentUnlockedLevel = nextUnlocked
      )
    )
  }

  suspend fun unlockLevel(levelId: Int) {
    val existing = dao.getProgressForLevel(levelId)
    if (existing == null || !existing.isUnlocked) {
      dao.insertOrUpdateProgress(
        existing?.copy(isUnlocked = true) ?: LevelProgressEntity(levelId = levelId, isUnlocked = true)
      )
    }
  }

  suspend fun useHintTicket(): Boolean {
    val prefs = dao.getUserPreferencesSync() ?: UserPreferencesEntity()
    if (prefs.hintTickets > 0) {
      dao.saveUserPreferences(prefs.copy(hintTickets = prefs.hintTickets - 1))
      return true
    }
    return false
  }

  suspend fun addHintTicket(count: Int = 1) {
    val prefs = dao.getUserPreferencesSync() ?: UserPreferencesEntity()
    dao.saveUserPreferences(prefs.copy(hintTickets = prefs.hintTickets + count))
  }

  suspend fun updateSettings(sound: Boolean, vibration: Boolean, colorblind: Boolean) {
    val prefs = dao.getUserPreferencesSync() ?: UserPreferencesEntity()
    dao.saveUserPreferences(
      prefs.copy(
        soundEnabled = sound,
        vibrationEnabled = vibration,
        colorblindMode = colorblind
      )
    )
  }
}
