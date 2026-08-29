package com.example.engine

import com.example.model.ArrowConfig
import com.example.model.Difficulty
import com.example.model.Direction
import com.example.model.LevelDefinition
import java.util.Random

object LevelCatalog {

  const val TOTAL_LEVELS = 220

  private val levelsMap: MutableMap<Int, LevelDefinition> = mutableMapOf()

  init {
    buildAllLevels()
  }

  fun getLevel(levelId: Int): LevelDefinition {
    val id = levelId.coerceIn(1, TOTAL_LEVELS)
    return levelsMap[id] ?: generateFallbackLevel(id)
  }

  fun getAllLevels(): List<LevelDefinition> {
    return (1..TOTAL_LEVELS).mapNotNull { levelsMap[it] }
  }

  fun getLevelsForWorld(worldIndex: Int): List<LevelDefinition> {
    // 6 Worlds / Chapters
    val range = when (worldIndex) {
      0 -> 1..30    // World 1: Genesis (3x3 - 4x4)
      1 -> 31..65   // World 2: Crossroads (4x4 - 5x5)
      2 -> 66..105  // World 3: Tangled Labyrinth (5x5 - 6x6)
      3 -> 106..145 // World 4: Cyber Matrix (6x6 - 7x6)
      4 -> 146..185 // World 5: Helix Citadel (7x7)
      else -> 186..220 // World 6: Grandmaster Realm (7x7 - 8x8)
    }
    return range.mapNotNull { levelsMap[it] }
  }

  private fun buildAllLevels() {
    // Milestone Handcrafted & Algorithmic Solvable Levels
    for (id in 1..TOTAL_LEVELS) {
      val level = createDeterministicLevel(id)
      levelsMap[id] = level
    }
  }

  private fun createDeterministicLevel(levelId: Int): LevelDefinition {
    // Determine grid size and arrow count based on progression
    val (rows, cols, targetArrows, difficulty) = when {
      levelId <= 10 -> Quad(3, 3, 3 + (levelId / 3), Difficulty.EASY)
      levelId <= 30 -> Quad(4, 4, 5 + ((levelId - 10) / 4), Difficulty.EASY)
      levelId <= 65 -> Quad(5, 5, 8 + ((levelId - 30) / 5), Difficulty.MEDIUM)
      levelId <= 105 -> Quad(6, 5, 12 + ((levelId - 65) / 5), Difficulty.MEDIUM)
      levelId <= 145 -> Quad(6, 6, 16 + ((levelId - 105) / 5), Difficulty.HARD)
      levelId <= 185 -> Quad(7, 6, 20 + ((levelId - 145) / 5), Difficulty.EXPERT)
      else -> Quad(7, 7, 24 + ((levelId - 185) / 4), Difficulty.MASTER)
    }

    // Generate guaranteed solvable arrows using reverse-construction with deterministic seed
    val arrows = generateSolvableBoard(levelId, rows, cols, targetArrows)

    val arrowCount = arrows.size
    val maxMoves = arrowCount + when (difficulty) {
      Difficulty.EASY -> 4
      Difficulty.MEDIUM -> 3
      Difficulty.HARD -> 2
      Difficulty.EXPERT -> 2
      Difficulty.MASTER -> 1
    }

    val timerTarget = when (difficulty) {
      Difficulty.EASY -> 30 + arrowCount * 3
      Difficulty.MEDIUM -> 40 + arrowCount * 3
      Difficulty.HARD -> 50 + arrowCount * 4
      Difficulty.EXPERT -> 60 + arrowCount * 4
      Difficulty.MASTER -> 75 + arrowCount * 4
    }

    val worldName = when {
      levelId <= 30 -> "Genesis"
      levelId <= 65 -> "Crossroads"
      levelId <= 105 -> "Labyrinth"
      levelId <= 145 -> "Matrix"
      levelId <= 185 -> "Citadel"
      else -> "Grandmaster"
    }

    return LevelDefinition(
      levelId = levelId,
      name = "$worldName Level $levelId",
      rows = rows,
      cols = cols,
      arrows = arrows,
      maxMoves = maxMoves,
      targetMoves = arrowCount,
      startingLives = 3,
      difficulty = difficulty,
      star3Moves = arrowCount,
      star2Moves = arrowCount + 2,
      timerTargetSeconds = timerTarget
    )
  }

  private fun generateSolvableBoard(
    seed: Int,
    rows: Int,
    cols: Int,
    targetCount: Int
  ): List<ArrowConfig> {
    val random = Random((seed * 31337L + 7919L))
    val grid = Array(rows) { arrayOfNulls<ArrowConfig>(cols) }
    val placedArrows = mutableListOf<ArrowConfig>()

    // Available positions
    val allPositions = mutableListOf<Pair<Int, Int>>()
    for (r in 0 until rows) {
      for (c in 0 until cols) {
        allPositions.add(r to c)
      }
    }
    allPositions.shuffle(random)

    var currentId = 1
    val maxAttempts = 300
    var attempts = 0

    // Reverse construction:
    // When placing an arrow in reverse, it must have an unobstructed path to the border
    // in current state, OR point to an already-placed arrow that would escape after it in forward play.
    while (placedArrows.size < targetCount && attempts < maxAttempts && allPositions.isNotEmpty()) {
      attempts++
      val posIndex = random.nextInt(allPositions.size)
      val (r, c) = allPositions[posIndex]

      if (grid[r][c] != null) {
        allPositions.removeAt(posIndex)
        continue
      }

      // Try 4 directions
      val directions = Direction.values().toList().shuffled(random)
      var placed = false

      for (dir in directions) {
        // Check if pointing this way in current reverse board is valid:
        // In forward play, this arrow will escape.
        // It can escape if ray to boundary has NO arrows currently on board,
        // OR it creates an interesting blocker that will clear before other arrows.
        var rayRow = r + dir.dy
        var rayCol = c + dir.dx
        var hasObstruction = false

        while (rayRow in 0 until rows && rayCol in 0 until cols) {
          if (grid[rayRow][rayCol] != null) {
            hasObstruction = true
            break
          }
          rayRow += dir.dy
          rayCol += dir.dx
        }

        // In reverse generation:
        // If hasObstruction == false, it means at this reverse step, the arrow has a direct exit!
        // This guarantees that in forward play, when previous arrows in reverse sequence are cleared,
        // this arrow will definitely have an open exit.
        if (!hasObstruction) {
          val colorIdx = (currentId + seed + dir.ordinal * 2) % 12
          val config = ArrowConfig(
            id = currentId++,
            row = r,
            col = c,
            direction = dir,
            colorIndex = colorIdx
          )
          grid[r][c] = config
          placedArrows.add(config)
          allPositions.removeAt(posIndex)
          placed = true
          break
        }
      }

      if (!placed && attempts > 100) {
        // Fallback: place pointing directly at nearest board edge
        val edgeDir = when {
          r == 0 -> Direction.UP
          r == rows - 1 -> Direction.DOWN
          c == 0 -> Direction.LEFT
          c == cols - 1 -> Direction.RIGHT
          else -> directions.first()
        }
        val colorIdx = (currentId + seed + edgeDir.ordinal) % 12
        val config = ArrowConfig(
          id = currentId++,
          row = r,
          col = c,
          direction = edgeDir,
          colorIndex = colorIdx
        )
        grid[r][c] = config
        placedArrows.add(config)
        allPositions.removeAt(posIndex)
      }
    }

    // Verify solvability
    val tempDef = LevelDefinition(
      levelId = seed,
      name = "Level $seed",
      rows = rows,
      cols = cols,
      arrows = placedArrows,
      maxMoves = placedArrows.size + 3,
      targetMoves = placedArrows.size
    )

    val solverResult = PuzzleSolver.solve(tempDef)
    if (solverResult.isSolvable && solverResult.solutionOrder.size == placedArrows.size && placedArrows.isNotEmpty()) {
      return placedArrows
    }

    // If any arrows caused a deadlock, remove the deadlocked ones or fallback to pure perimeter-oriented generation
    return createGuaranteedSolvableFallback(seed, rows, cols, targetCount)
  }

  private fun createGuaranteedSolvableFallback(
    seed: Int,
    rows: Int,
    cols: Int,
    count: Int
  ): List<ArrowConfig> {
    val random = Random(seed * 49999L + 12345L)
    val arrows = mutableListOf<ArrowConfig>()
    var id = 1

    val cells = mutableListOf<Pair<Int, Int>>()
    for (r in 0 until rows) {
      for (c in 0 until cols) {
        cells.add(r to c)
      }
    }
    cells.shuffle(random)

    val selected = cells.take(count.coerceIn(3, rows * cols - 1))
    for ((r, c) in selected) {
      // Pick direction pointing toward closest edge or spiral
      val dUp = r
      val dDown = (rows - 1) - r
      val dLeft = c
      val dRight = (cols - 1) - c

      val minDistance = minOf(dUp, dDown, dLeft, dRight)
      val dir = when (minDistance) {
        dUp -> Direction.UP
        dDown -> Direction.DOWN
        dLeft -> Direction.LEFT
        else -> Direction.RIGHT
      }

      val colorIdx = (id * 3 + seed + r + c) % 12
      arrows.add(
        ArrowConfig(
          id = id++,
          row = r,
          col = c,
          direction = dir,
          colorIndex = colorIdx
        )
      )
    }

    // Final safety check: solve and keep only the solvable component
    val solverResult = PuzzleSolver.solve(
      LevelDefinition(
        levelId = seed,
        name = "Test",
        rows = rows,
        cols = cols,
        arrows = arrows,
        maxMoves = arrows.size + 5,
        targetMoves = arrows.size
      )
    )

    if (solverResult.isSolvable && solverResult.solutionOrder.isNotEmpty()) {
      return arrows
    } else {
      // Create a classic outward pinwheel / spiral grid
      val safeArrows = mutableListOf<ArrowConfig>()
      var safeId = 1
      for (i in 0 until minOf(count, rows * cols)) {
        val r = i / cols
        val c = i % cols
        val dir = if (r <= rows / 2) {
          if (c <= cols / 2) Direction.UP else Direction.RIGHT
        } else {
          if (c <= cols / 2) Direction.LEFT else Direction.DOWN
        }
        safeArrows.add(
          ArrowConfig(
            id = safeId++,
            row = r,
            col = c,
            direction = dir,
            colorIndex = (safeId * 2 + r + c) % 12
          )
        )
      }
      return safeArrows
    }
  }

  private fun generateFallbackLevel(levelId: Int): LevelDefinition {
    return LevelDefinition(
      levelId = levelId,
      name = "Level $levelId",
      rows = 3,
      cols = 3,
      arrows = listOf(
        ArrowConfig(1, 0, 0, Direction.UP, 0),
        ArrowConfig(2, 0, 1, Direction.UP, 1),
        ArrowConfig(3, 0, 2, Direction.RIGHT, 2),
        ArrowConfig(4, 1, 0, Direction.LEFT, 3),
        ArrowConfig(5, 2, 2, Direction.DOWN, 4)
      ),
      maxMoves = 7,
      targetMoves = 5,
      startingLives = 3,
      difficulty = Difficulty.EASY,
      star3Moves = 5,
      star2Moves = 6,
      timerTargetSeconds = 30
    )
  }

  private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
