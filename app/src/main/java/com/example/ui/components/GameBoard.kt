package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.example.model.ArrowItem
import com.example.model.FloatingTextPopup
import com.example.model.SparkParticle
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BentoDivider
import com.example.ui.theme.BentoSurface
import kotlin.math.roundToInt

@Composable
fun GameBoard(
  rows: Int,
  cols: Int,
  arrows: List<ArrowItem>,
  isInteractive: Boolean,
  colorblindMode: Boolean,
  particles: List<SparkParticle>,
  floatingPopups: List<FloatingTextPopup> = emptyList(),
  shakeTrigger: Long = 0L,
  onArrowClick: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val shakeAnim = remember { Animatable(0f) }

  LaunchedEffect(shakeTrigger) {
    if (shakeTrigger > 0L) {
      shakeAnim.snapTo(0f)
      shakeAnim.animateTo(
        targetValue = 0f,
        animationSpec = keyframes {
          durationMillis = 320
          -14f at 30
          14f at 70
          -10f at 130
          10f at 190
          -5f at 250
          0f at 320
        }
      )
    }
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .offset { IntOffset(shakeAnim.value.roundToInt(), 0) }
      .padding(horizontal = 20.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    val maxAvailableW = maxWidth
    val maxAvailableH = maxHeight

    val cellWidthFromW = maxAvailableW / cols
    val cellHeightFromH = maxAvailableH / rows
    val cellSize = min(cellWidthFromW, cellHeightFromH)

    val boardWidth = cellSize * cols
    val boardHeight = cellSize * rows

    Surface(
      modifier = Modifier
        .size(width = boardWidth, height = boardHeight)
        .clip(RoundedCornerShape(32.dp))
        .testTag("game_board_surface"),
      shape = RoundedCornerShape(32.dp),
      color = BentoSurface,
      shadowElevation = 4.dp
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(8.dp)
      ) {
        // Bento Grid Inset Cells
        Canvas(modifier = Modifier.fillMaxSize()) {
          val cellW = size.width / cols
          val cellH = size.height / rows
          val pad = 3.dp.toPx()

          for (r in 0 until rows) {
            for (c in 0 until cols) {
              val cellTopLeft = Offset(c * cellW, r * cellH)
              drawRoundRect(
                color = BentoDivider.copy(alpha = 0.35f),
                topLeft = Offset(cellTopLeft.x + pad, cellTopLeft.y + pad),
                size = Size(cellW - pad * 2, cellH - pad * 2),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
              )
            }
          }
        }

        // Render Active & Moving Arrows
        val activeCellW = (boardWidth - 16.dp) / cols
        val activeCellH = (boardHeight - 16.dp) / rows
        val effectiveCellSize = min(activeCellW, activeCellH)

        arrows.forEach { arrow ->
          val offsetX = effectiveCellSize * arrow.col
          val offsetY = effectiveCellSize * arrow.row

          Box(
            modifier = Modifier
              .offset(x = offsetX, y = offsetY)
              .size(effectiveCellSize)
          ) {
            ArrowTile(
              arrow = arrow,
              tileSizeDp = effectiveCellSize,
              isInteractive = isInteractive,
              colorblindMode = colorblindMode,
              onArrowClick = onArrowClick
            )
          }
        }

        // Particle sparks layer
        if (particles.isNotEmpty()) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val cellW = size.width / cols
            val cellH = size.height / rows
            particles.forEach { p ->
              val px = (p.x + 0.5f) * cellW + p.vx * 3f
              val py = (p.y + 0.5f) * cellH + p.vy * 3f
              drawCircle(
                color = Color(p.color).copy(alpha = p.alpha),
                radius = p.size,
                center = Offset(px, py)
              )
            }
          }
        }

        // Floating Animated Popups layer
        floatingPopups.forEach { popup ->
          AnimatedFloatingScoreItem(
            popup = popup,
            cellSize = effectiveCellSize
          )
        }
      }
    }
  }
}

@Composable
private fun AnimatedFloatingScoreItem(
  popup: FloatingTextPopup,
  cellSize: androidx.compose.ui.unit.Dp
) {
  val riseAnim = remember { Animatable(0f) }
  val alphaAnim = remember { Animatable(1f) }
  val scaleAnim = remember { Animatable(0.7f) }

  LaunchedEffect(popup.id) {
    scaleAnim.animateTo(
      targetValue = if (popup.isCombo) 1.25f else 1.05f,
      animationSpec = tween(120, easing = FastOutSlowInEasing)
    )
  }

  LaunchedEffect(popup.id) {
    riseAnim.animateTo(
      targetValue = -54f,
      animationSpec = tween(550, easing = FastOutSlowInEasing)
    )
  }

  LaunchedEffect(popup.id) {
    alphaAnim.animateTo(
      targetValue = 0f,
      animationSpec = keyframes {
        durationMillis = 550
        1f at 280
        0f at 550
      }
    )
  }

  val baseOffsetX = cellSize * popup.x
  val baseOffsetY = cellSize * popup.y

  Box(
    modifier = Modifier
      .offset(
        x = baseOffsetX,
        y = baseOffsetY + riseAnim.value.dp
      )
      .alpha(alphaAnim.value)
      .scale(scaleAnim.value),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = popup.text,
      fontWeight = FontWeight.Black,
      fontSize = if (popup.isCombo) 16.sp else 14.sp,
      color = if (popup.isCombo) AmberGold else Color(popup.color),
      style = TextStyle(
        shadow = Shadow(
          color = Color.Black.copy(alpha = 0.5f),
          offset = Offset(2f, 2f),
          blurRadius = 4f
        )
      )
    )
  }
}
