package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ArrowItem
import com.example.model.Direction
import kotlin.math.roundToInt

@Composable
fun ArrowTile(
  arrow: ArrowItem,
  tileSizeDp: Dp,
  isInteractive: Boolean,
  colorblindMode: Boolean,
  onArrowClick: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  if (!arrow.isActive && !arrow.isMovingOut) {
    return
  }

  // Animation values
  val shakeOffsetAnim = remember { Animatable(0f) }
  val hintPulseAnim = remember { Animatable(1f) }
  val exitProgressAnim = remember { Animatable(0f) }
  val exitAlphaAnim = remember { Animatable(1f) }
  val exitScaleAnim = remember { Animatable(1f) }

  // Handle Shake Animation on blocked move
  LaunchedEffect(arrow.isBlockedShaking) {
    if (arrow.isBlockedShaking) {
      shakeOffsetAnim.snapTo(0f)
      shakeOffsetAnim.animateTo(
        targetValue = 0f,
        animationSpec = keyframes {
          durationMillis = 360
          -16f at 40
          16f at 80
          -12f at 140
          12f at 200
          -6f at 260
          6f at 310
          0f at 360
        }
      )
    }
  }

  // Handle Hint Pulsing Glow Animation
  LaunchedEffect(arrow.isHighlightedForHint) {
    if (arrow.isHighlightedForHint) {
      hintPulseAnim.animateTo(
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
          animation = tween(450, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse
        )
      )
    } else {
      hintPulseAnim.snapTo(1f)
    }
  }

  // Handle Exit Animation when arrow is cleared
  LaunchedEffect(arrow.isMovingOut) {
    if (arrow.isMovingOut) {
      exitProgressAnim.animateTo(
        targetValue = 1f,
        animationSpec = tween(280, easing = FastOutSlowInEasing)
      )
    } else {
      exitProgressAnim.snapTo(0f)
    }
  }

  LaunchedEffect(arrow.isMovingOut) {
    if (arrow.isMovingOut) {
      exitAlphaAnim.animateTo(
        targetValue = 0f,
        animationSpec = tween(280, easing = LinearEasing)
      )
    } else {
      exitAlphaAnim.snapTo(1f)
    }
  }

  LaunchedEffect(arrow.isMovingOut) {
    if (arrow.isMovingOut) {
      exitScaleAnim.animateTo(
        targetValue = 0.6f,
        animationSpec = tween(280, easing = FastOutSlowInEasing)
      )
    } else {
      exitScaleAnim.snapTo(1f)
    }
  }

  // Calculate motion offset based on exit progress and direction
  val travelDistancePx = tileSizeDp.value * 6f
  val exitOffsetX = when (arrow.direction) {
    Direction.LEFT -> -exitProgressAnim.value * travelDistancePx
    Direction.RIGHT -> exitProgressAnim.value * travelDistancePx
    else -> 0f
  }
  val exitOffsetY = when (arrow.direction) {
    Direction.UP -> -exitProgressAnim.value * travelDistancePx
    Direction.DOWN -> exitProgressAnim.value * travelDistancePx
    else -> 0f
  }

  val totalOffsetX = (exitOffsetX + shakeOffsetAnim.value).roundToInt()
  val totalOffsetY = exitOffsetY.roundToInt()

  val baseColor = Color(arrow.color.hex)
  val isShaking = arrow.isBlockedShaking
  val isHinted = arrow.isHighlightedForHint

  Box(
    modifier = modifier
      .size(tileSizeDp)
      .offset { IntOffset(totalOffsetX, totalOffsetY) }
      .alpha(exitAlphaAnim.value)
      .scale(if (isHinted) hintPulseAnim.value else exitScaleAnim.value)
      .padding(3.dp)
      .testTag("arrow_tile_${arrow.id}"),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .clip(RoundedCornerShape(14.dp))
        .clickable(
          enabled = isInteractive && arrow.isActive && !arrow.isMovingOut,
          interactionSource = remember { MutableInteractionSource() },
          indication = androidx.compose.material3.ripple(bounded = true, color = Color.White)
        ) {
          onArrowClick(arrow.id)
        },
      shape = RoundedCornerShape(14.dp),
      color = if (isShaking) Color(0xFFF43F5E) else baseColor,
      shadowElevation = if (isHinted) 8.dp else 2.dp
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        // Bento White Crisp Arrow with inner glow and neon laser depth
        Canvas(modifier = Modifier.fillMaxSize()) {
          drawBentoArrow(
            isHinted = isHinted,
            isMovingOut = arrow.isMovingOut,
            direction = arrow.direction,
            baseColor = baseColor
          )
        }

        // Colorblind accessibility label
        if (colorblindMode) {
          Text(
            text = arrow.direction.symbol,
            color = Color.White.copy(alpha = 0.95f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(4.dp)
          )
        }
      }
    }
  }
}

private fun DrawScope.drawBentoArrow(
  isHinted: Boolean,
  isMovingOut: Boolean,
  direction: Direction,
  baseColor: Color
) {
  val width = size.width
  val height = size.height
  val center = Offset(width / 2f, height / 2f)

  // If hinted, draw a pulsing white/gold border
  if (isHinted) {
    val cornerRadius = 14.dp.toPx()
    drawRoundRect(
      color = Color.White,
      size = size,
      cornerRadius = CornerRadius(cornerRadius, cornerRadius),
      style = Stroke(width = 3.5.dp.toPx())
    )
  }

  // Draw crisp white arrow pointing in the given direction
  val arrowWidth = width * 0.52f
  val arrowHeight = height * 0.52f
  val halfW = arrowWidth / 2f
  val halfH = arrowHeight / 2f

  drawContext.canvas.save()
  drawContext.transform.rotate(
    degrees = direction.rotationDegrees,
    pivot = center
  )

  // Glowing laser tail when moving out
  if (isMovingOut) {
    drawLine(
      color = Color.White.copy(alpha = 0.85f),
      start = Offset(center.x, center.y + halfH * 0.8f),
      end = Offset(center.x, center.y + halfH * 2.2f),
      strokeWidth = 6.dp.toPx(),
      cap = StrokeCap.Round
    )
  }

  val path = Path().apply {
    val topY = center.y - halfH * 0.92f
    val tipX = center.x
    val stemBottomY = center.y + halfH * 0.85f
    val stemHalfWidth = halfW * 0.42f
    val barbY = center.y + halfH * 0.08f

    moveTo(tipX, topY)
    lineTo(center.x + halfW, barbY)
    lineTo(center.x + stemHalfWidth, barbY)
    lineTo(center.x + stemHalfWidth, stemBottomY)
    lineTo(center.x - stemHalfWidth, stemBottomY)
    lineTo(center.x - stemHalfWidth, barbY)
    lineTo(center.x - halfW, barbY)
    close()
  }

  // Soft glow backdrop
  drawPath(
    path = path,
    color = Color.White.copy(alpha = 0.4f),
    style = Stroke(
      width = 4.dp.toPx(),
      cap = StrokeCap.Round,
      join = StrokeJoin.Round
    )
  )

  // Pure clean white arrow fill
  drawPath(
    path = path,
    color = Color.White,
    style = Fill
  )

  // Crisp outline
  drawPath(
    path = path,
    color = Color.White.copy(alpha = 0.95f),
    style = Stroke(
      width = 1.dp.toPx(),
      cap = StrokeCap.Round,
      join = StrokeJoin.Round
    )
  )

  drawContext.canvas.restore()
}

