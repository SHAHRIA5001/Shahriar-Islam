package com.example.engine

import com.example.model.ArrowConfig
import com.example.model.ArrowItem
import com.example.model.Direction
import com.example.model.LevelDefinition

data class SolverResult(
  val isSolvable: Boolean,
  val solutionOrder: List<Int>, // List of arrow IDs in order of escape
  val remainingBlockedIds: List<Int>,
  val movesRequired: Int
)

object PuzzleSolver {

  /**
   * Checks if an arrow has an unobstructed path to the boundary of the board.
   * Returns Pair(isClear, blockerArrowItemOrNull)
   */
  fun checkPathClear(
    arrow: ArrowItem,
    allActiveArrows: List<ArrowItem>,
    rows: Int,
    cols: Int
  ): Pair<Boolean, ArrowItem?> {
    if (!arrow.isActive) return Pair(false, null)

    var currRow = arrow.row + arrow.direction.dy
    var currCol = arrow.col + arrow.direction.dx

    // Fast lookup grid or set
    val activeMap = allActiveArrows.filter { it.isActive && it.id != arrow.id }
      .associateBy { it.row to it.col }

    while (currRow in 0 until rows && currCol in 0 until cols) {
      val blocker = activeMap[currRow to currCol]
      if (blocker != null) {
        return Pair(false, blocker)
      }
      currRow += arrow.direction.dy
      currCol += arrow.direction.dx
    }

    return Pair(true, null)
  }

  /**
   * Returns all active arrows that currently have an unobstructed path to the edge.
   */
  fun getAvailableEscapes(
    activeArrows: List<ArrowItem>,
    rows: Int,
    cols: Int
  ): List<ArrowItem> {
    return activeArrows.filter { arrow ->
      arrow.isActive && checkPathClear(arrow, activeArrows, rows, cols).first
    }
  }

  /**
   * Finds the best next move (hint) from current active state.
   */
  fun findBestHint(
    activeArrows: List<ArrowItem>,
    rows: Int,
    cols: Int
  ): ArrowItem? {
    val escapes = getAvailableEscapes(activeArrows, rows, cols)
    return escapes.firstOrNull()
  }

  /**
   * Solves the full level from scratch.
   */
  fun solve(level: LevelDefinition): SolverResult {
    val active = level.arrows.map { it.toArrowItem() }.toMutableList()
    val solutionOrder = mutableListOf<Int>()

    while (active.isNotEmpty()) {
      val clearArrows = getAvailableEscapes(active, level.rows, level.cols)
      if (clearArrows.isEmpty()) {
        // Deadlocked state
        return SolverResult(
          isSolvable = false,
          solutionOrder = solutionOrder,
          remainingBlockedIds = active.map { it.id },
          movesRequired = solutionOrder.size
        )
      }

      // Pick the first available escape
      val nextEscape = clearArrows.first()
      solutionOrder.add(nextEscape.id)
      active.removeIf { it.id == nextEscape.id }
    }

    return SolverResult(
      isSolvable = true,
      solutionOrder = solutionOrder,
      remainingBlockedIds = emptyList(),
      movesRequired = solutionOrder.size
    )
  }

  /**
   * Validates a level configuration for data integrity and solvability.
   */
  fun validateLevel(level: LevelDefinition): Boolean {
    if (level.rows <= 0 || level.cols <= 0) return false
    if (level.arrows.isEmpty()) return false
    if (level.maxMoves < level.arrows.size) return false

    // Check bounds & duplicates
    val seenPositions = mutableSetOf<Pair<Int, Int>>()
    val seenIds = mutableSetOf<Int>()
    for (arrow in level.arrows) {
      if (arrow.row !in 0 until level.rows || arrow.col !in 0 until level.cols) return false
      if (!seenPositions.add(arrow.row to arrow.col)) return false
      if (!seenIds.add(arrow.id)) return false
    }

    val result = solve(level)
    return result.isSolvable && result.solutionOrder.size == level.arrows.size
  }
}
