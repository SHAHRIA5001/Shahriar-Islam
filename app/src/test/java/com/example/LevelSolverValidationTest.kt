package com.example

import com.example.engine.LevelCatalog
import com.example.engine.PuzzleSolver
import com.example.model.ArrowColor
import com.example.model.ArrowItem
import com.example.model.Direction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelSolverValidationTest {

  @Test
  fun testAll220LevelsAreSolvableAndValid() {
    val allLevels = LevelCatalog.getAllLevels()
    assertEquals("Should contain at least 220 levels", 220, allLevels.size)

    for (level in allLevels) {
      assertTrue("Level ${level.levelId} must have valid rows/cols", level.rows >= 3 && level.cols >= 3)
      assertTrue("Level ${level.levelId} must have arrows", level.arrows.isNotEmpty())
      assertTrue("Level ${level.levelId} must have maxMoves >= arrows.size", level.maxMoves >= level.arrows.size)

      // Test solver
      val solverResult = PuzzleSolver.solve(level)
      assertTrue(
        "Level ${level.levelId} MUST be solvable! Remaining blocked: ${solverResult.remainingBlockedIds}",
        solverResult.isSolvable
      )
      assertEquals(
        "Level ${level.levelId} solution order size must match arrow count",
        level.arrows.size,
        solverResult.solutionOrder.size
      )

      // Test hint finder on initial state
      val activeArrows = level.arrows.map { it.toArrowItem() }
      val hint = PuzzleSolver.findBestHint(activeArrows, level.rows, level.cols)
      assertNotNull("Level ${level.levelId} must have a valid initial hint", hint)
      assertTrue("Hint arrow must be one of the solvable first moves", hint!!.id in solverResult.solutionOrder)
    }
  }

  @Test
  fun testBlockedPathDetection() {
    // Arrow 1 at (0,0) facing RIGHT, Arrow 2 at (0,1) facing UP
    val arrow1 = ArrowItem(1, 0, 0, Direction.RIGHT, ArrowColor.NEON_RED)
    val arrow2 = ArrowItem(2, 0, 1, Direction.UP, ArrowColor.CYAN_BLUE)

    val (clear1, blocker1) = PuzzleSolver.checkPathClear(arrow1, listOf(arrow1, arrow2), 3, 3)
    assertFalse("Arrow 1 should be blocked by Arrow 2", clear1)
    assertEquals("Blocker should be Arrow 2", 2, blocker1?.id)

    val (clear2, blocker2) = PuzzleSolver.checkPathClear(arrow2, listOf(arrow1, arrow2), 3, 3)
    assertTrue("Arrow 2 should have a clear path UP to edge", clear2)
    assertEquals(null, blocker2)
  }
}
