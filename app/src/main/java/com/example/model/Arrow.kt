package com.example.model

enum class Direction(val dx: Int, val dy: Int, val symbol: String, val rotationDegrees: Float) {
  UP(0, -1, "↑", 0f),
  RIGHT(1, 0, "→", 90f),
  DOWN(0, 1, "↓", 180f),
  LEFT(-1, 0, "←", 270f);

  fun opposite(): Direction = when (this) {
    UP -> DOWN
    DOWN -> UP
    LEFT -> RIGHT
    RIGHT -> LEFT
  }
}

enum class ArrowColor(val hex: Long, val nameStr: String) {
  NEON_RED(0xFFFF3366, "Red"),
  CYAN_BLUE(0xFF00E5FF, "Cyan"),
  EMERALD_GREEN(0xFF00E676, "Green"),
  AMBER_ORANGE(0xFFFF9100, "Orange"),
  VIVID_PURPLE(0xFFD500F9, "Purple"),
  ELECTRIC_YELLOW(0xFFFFEA00, "Yellow"),
  HOT_PINK(0xFFFF1744, "Pink"),
  LIME(0xFFAEEA00, "Lime"),
  INDIGO(0xFF3D5AFE, "Indigo"),
  CORAL(0xFFFF6E40, "Coral"),
  TEAL(0xFF1DE9B6, "Teal"),
  MAGENTA(0xFFF50057, "Magenta");

  companion object {
    fun fromIndex(index: Int): ArrowColor {
      val values = values()
      return values[Math.floorMod(index, values.size)]
    }
  }
}

data class ArrowItem(
  val id: Int,
  val row: Int,
  val col: Int,
  val direction: Direction,
  val color: ArrowColor,
  val isActive: Boolean = true,
  val isHighlightedForHint: Boolean = false,
  val isBlockedShaking: Boolean = false,
  val shakeOffset: Float = 0f,
  val exitProgress: Float = 0f, // 0f = in place, 1f = moved off screen
  val isMovingOut: Boolean = false
)

enum class Difficulty {
  EASY,
  MEDIUM,
  HARD,
  EXPERT,
  MASTER
}

data class LevelDefinition(
  val levelId: Int,
  val name: String,
  val rows: Int,
  val cols: Int,
  val arrows: List<ArrowConfig>,
  val maxMoves: Int,
  val targetMoves: Int,
  val startingLives: Int = 3,
  val difficulty: Difficulty = Difficulty.EASY,
  val star3Moves: Int = targetMoves,
  val star2Moves: Int = (targetMoves * 1.5).toInt().coerceAtLeast(targetMoves + 1),
  val timerTargetSeconds: Int = 45
)

data class ArrowConfig(
  val id: Int,
  val row: Int,
  val col: Int,
  val direction: Direction,
  val colorIndex: Int
) {
  fun toArrowItem(): ArrowItem = ArrowItem(
    id = id,
    row = row,
    col = col,
    direction = direction,
    color = ArrowColor.fromIndex(colorIndex)
  )
}
