package com.example.model

enum class GameStatus {
  PLAYING,
  WON,
  FAILED,
  PAUSED
}

enum class InputState {
  IDLE,
  VALIDATING,
  MOVING,
  CLEANUP,
  FINISHED
}

sealed class MoveValidationResult {
  data class Clear(val arrowId: Int, val pathLength: Int) : MoveValidationResult()
  data class Blocked(val arrowId: Int, val blockerId: Int?) : MoveValidationResult()
}

data class SparkParticle(
  val id: Long,
  val x: Float,
  val y: Float,
  val vx: Float,
  val vy: Float,
  val color: Long,
  val size: Float,
  val alpha: Float = 1f,
  val life: Float = 1f
)

data class FloatingTextPopup(
  val id: Long,
  val text: String,
  val x: Float,
  val y: Float,
  val color: Long,
  val isCombo: Boolean = false
)

data class LevelResult(
  val levelId: Int,
  val moves: Int,
  val maxMoves: Int,
  val stars: Int,
  val timeSeconds: Int,
  val score: Int,
  val isNewBest: Boolean,
  val isPerfect: Boolean
)
