package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ads.TestAdManager
import com.example.ui.components.DifficultyBadge
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TestAdsInspectorDialog
import com.example.ui.components.TestBannerAd
import com.example.ui.components.TestInterstitialDialog
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoContainerDeep
import com.example.ui.theme.BentoDivider
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoPrimaryDark
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.BentoSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.LevelItemUi
import com.example.viewmodel.LevelSelectUiState
import com.example.viewmodel.WorldChapter
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MissionMapScreen(
  uiState: LevelSelectUiState,
  soundEnabled: Boolean,
  vibrationEnabled: Boolean,
  colorblindMode: Boolean,
  onSelectLevel: (Int) -> Unit,
  onSelectWorld: (Int) -> Unit,
  onClaimDailyReward: () -> Unit,
  onToggleSettings: (Boolean, Boolean, Boolean) -> Unit
) {
  var showSettingsDialog by remember { mutableStateOf(false) }
  var showTestAdsInspector by remember { mutableStateOf(false) }
  var showInterstitialTest by remember { mutableStateOf(false) }

  val adManager = remember { TestAdManager.instance }
  val bannerVisible = remember { adManager.bannerVisible }
  val isBannerShown by bannerVisible.collectAsState()
  val activeCreative by adManager.activeBannerCreative.collectAsState()

  val listState = rememberLazyListState()

  // Auto scroll towards current level within chapter
  LaunchedEffect(uiState.selectedWorldIndex) {
    val currentIdx = uiState.levels.indexOfFirst { it.isCurrent }
    if (currentIdx >= 0) {
      val rowIdx = currentIdx / 3
      listState.animateScrollToItem((rowIdx + 1).coerceAtLeast(0))
    }
  }

  Scaffold(
    containerColor = BentoBackground,
    topBar = {
      MissionMapHeader(
        totalStars = uiState.totalStars,
        maxStars = uiState.totalMaxStars,
        hintTickets = uiState.hintTickets,
        onClaimReward = onClaimDailyReward,
        onSettingsClick = { showSettingsDialog = true }
      )
    },
    floatingActionButton = {
      // Quick Jump to Current Level Bento FAB
      FloatingActionButton(
        onClick = { onSelectLevel(uiState.currentUnlockedLevelId) },
        containerColor = BentoPrimary,
        contentColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag("jump_to_current_fab")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Play Current Level",
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "LEVEL ${uiState.currentUnlockedLevelId}",
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            fontSize = 13.sp
          )
        }
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Chapter Tabs Selector
      ChapterTabsRow(
        chapters = uiState.worlds,
        selectedIndex = uiState.selectedWorldIndex,
        onSelect = onSelectWorld
      )

      // Selected Chapter Banner Info
      val selectedChapter = uiState.worlds.getOrNull(uiState.selectedWorldIndex)

      // Serpentine Winding Road Level Map with Animated Connected Lines
      LazyColumn(
        state = listState,
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .testTag("levels_winding_map"),
        contentPadding = PaddingValues(bottom = 96.dp)
      ) {
        if (selectedChapter != null) {
          item(key = "chapter_banner") {
            ChapterBannerCard(
              chapter = selectedChapter,
              levels = uiState.levels
            )
          }
        }

        item(key = "winding_path_board") {
          WindingLevelPathBoard(
            levels = uiState.levels,
            onSelectLevel = onSelectLevel
          )
        }
      }

      // Bottom Test Banner Ad
      if (isBannerShown) {
        TestBannerAd(
          creative = activeCreative,
          onRotateCreative = { adManager.rotateBannerCreative() },
          onDismiss = { adManager.toggleBanner(false) },
          modifier = Modifier.padding(bottom = 6.dp)
        )
      }
    }
  }

  if (showSettingsDialog) {
    SettingsDialog(
      soundEnabled = soundEnabled,
      vibrationEnabled = vibrationEnabled,
      colorblindMode = colorblindMode,
      onToggleSettings = onToggleSettings,
      onOpenTestAdsInspector = {
        showTestAdsInspector = true
      },
      onDismiss = { showSettingsDialog = false }
    )
  }

  if (showTestAdsInspector) {
    TestAdsInspectorDialog(
      onTriggerRewarded = {
        showTestAdsInspector = false
        onClaimDailyReward()
      },
      onTriggerInterstitial = {
        showTestAdsInspector = false
        showInterstitialTest = true
      },
      onDismiss = { showTestAdsInspector = false }
    )
  }

  if (showInterstitialTest) {
    TestInterstitialDialog(
      secondsRemaining = 0,
      sponsorName = "AdMob Interstitial Test Suite",
      onDismiss = { showInterstitialTest = false }
    )
  }
}

@Composable
private fun WindingLevelPathBoard(
  levels: List<LevelItemUi>,
  onSelectLevel: (Int) -> Unit
) {
  if (levels.isEmpty()) return

  val columns = 3
  val rowCount = (levels.size + columns - 1) / columns
  val rowHeight = 110.dp

  val infiniteTransition = rememberInfiniteTransition(label = "path_anim")
  val energyPulseProgress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "energy_flow"
  )

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    val totalWidth = maxWidth
    val boardHeight = rowHeight * rowCount

    // Canvas that draws the corner-connecting animated serpentine glowing road lines
    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(boardHeight)
    ) {
      val widthPx = size.width
      val nodeSpacingX = widthPx / columns
      val rowHeightPx = rowHeight.toPx()

      fun getNodeCenter(index: Int): Offset {
        val row = index / columns
        val colInRow = index % columns
        // Zigzag: even rows go left->right (0, 1, 2), odd rows go right->left (2, 1, 0)
        val col = if (row % 2 == 0) colInRow else (columns - 1 - colInRow)
        val centerX = nodeSpacingX * col + nodeSpacingX / 2f
        val centerY = row * rowHeightPx + rowHeightPx / 2f
        return Offset(centerX, centerY)
      }

      // Draw path segments between level i and level i + 1
      for (i in 0 until levels.size - 1) {
        val fromLevel = levels[i]
        val toLevel = levels[i + 1]

        val p1 = getNodeCenter(i)
        val p2 = getNodeCenter(i + 1)

        val fromRow = i / columns
        val toRow = (i + 1) / columns

        val isPathCompleted = fromLevel.isCompleted
        val isPathActive = fromLevel.isCompleted || fromLevel.isCurrent

        val path = Path()
        path.moveTo(p1.x, p1.y)

        if (fromRow == toRow) {
          // Straight horizontal path within the same row
          path.lineTo(p2.x, p2.y)
        } else {
          // Corner curve connecting to next row!
          val isTurningRight = (fromRow % 2 == 0) // Turned on right edge
          val cornerCurveOffset = if (isTurningRight) nodeSpacingX * 0.45f else -nodeSpacingX * 0.45f

          // S-Curve Corner transition with smooth cubic Bézier
          val c1X = p1.x + cornerCurveOffset
          val c1Y = p1.y + (p2.y - p1.y) * 0.25f
          val c2X = p2.x + cornerCurveOffset
          val c2Y = p1.y + (p2.y - p1.y) * 0.75f

          path.cubicTo(c1X, c1Y, c2X, c2Y, p2.x, p2.y)
        }

        // Draw background base trail track
        drawPath(
          path = path,
          color = if (isPathCompleted) BentoPrimary.copy(alpha = 0.25f) else BentoDivider.copy(alpha = 0.4f),
          style = Stroke(
            width = 12.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
          )
        )

        // Draw glowing inner road line
        if (isPathCompleted) {
          drawPath(
            path = path,
            color = BentoPrimary,
            style = Stroke(
              width = 5.dp.toPx(),
              cap = StrokeCap.Round,
              join = StrokeJoin.Round
            )
          )

          // Animated Traveling Laser Pulse along unlocked path line!
          val phaseOffset = (energyPulseProgress * 40.dp.toPx())
          drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.85f),
            style = Stroke(
              width = 3.dp.toPx(),
              cap = StrokeCap.Round,
              pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(20f, 30f),
                phase = -phaseOffset
              )
            )
          )
        } else if (isPathActive) {
          // Currently unlocking route to next level: animated dashed gradient
          val phaseOffset = (energyPulseProgress * 30.dp.toPx())
          drawPath(
            path = path,
            color = BentoPrimary.copy(alpha = 0.7f),
            style = Stroke(
              width = 4.dp.toPx(),
              cap = StrokeCap.Round,
              pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(16f, 16f),
                phase = -phaseOffset
              )
            )
          )
        } else {
          // Locked road path: subtle dotted gray line
          drawPath(
            path = path,
            color = TextMuted.copy(alpha = 0.35f),
            style = Stroke(
              width = 3.dp.toPx(),
              cap = StrokeCap.Round,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 12f), 0f)
            )
          )
        }
      }
    }

    // Overlay Interactive Level Nodes on the calculated coordinates
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(boardHeight)
    ) {
      val nodeSpacingXDp = totalWidth / columns
      val nodeSize = 72.dp

      levels.forEachIndexed { index, level ->
        val row = index / columns
        val colInRow = index % columns
        val col = if (row % 2 == 0) colInRow else (columns - 1 - colInRow)

        val xOffset = nodeSpacingXDp * col + (nodeSpacingXDp - nodeSize) / 2
        val yOffset = rowHeight * row + (rowHeight - nodeSize) / 2

        Box(
          modifier = Modifier
            .offset(x = xOffset, y = yOffset)
            .size(nodeSize)
        ) {
          WindingLevelNode(
            level = level,
            onClick = {
              if (level.isUnlocked) {
                onSelectLevel(level.levelId)
              }
            }
          )
        }
      }
    }
  }
}

@Composable
private fun WindingLevelNode(
  level: LevelItemUi,
  onClick: () -> Unit
) {
  val isUnlocked = level.isUnlocked
  val isCompleted = level.isCompleted
  val isCurrent = level.isCurrent

  val infiniteTransition = rememberInfiniteTransition(label = "beacon")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.12f,
    animationSpec = infiniteRepeatable(
      animation = tween(650, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "node_pulse"
  )

  val haloAlpha by infiniteTransition.animateFloat(
    initialValue = 0.7f,
    targetValue = 0.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(650, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "halo_pulse"
  )

  val rotationDeg by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(4000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "ring_rotate"
  )

  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    // Current Active Level Beacon Glowing Halo & Ring
    if (isCurrent) {
      Box(
        modifier = Modifier
          .size(76.dp)
          .scale(pulseScale)
          .clip(CircleShape)
          .background(BentoPrimary.copy(alpha = haloAlpha))
      )

      // Rotating neon dashed ring
      Canvas(modifier = Modifier.size(68.dp).rotate(rotationDeg)) {
        drawCircle(
          brush = Brush.sweepGradient(
            listOf(
              BentoPrimary,
              Color.White,
              AmberGold,
              BentoPrimary
            )
          ),
          style = Stroke(
            width = 3.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
          )
        )
      }
    }

    val nodeBg = when {
      isCurrent -> BentoPrimary
      isCompleted -> BentoPrimaryContainer
      isUnlocked -> BentoSurface
      else -> BentoContainerDeep
    }

    val nodeBorder = when {
      isCurrent -> Color.White
      isCompleted -> BentoPrimary
      isUnlocked -> BentoDivider
      else -> Color.Transparent
    }

    Surface(
      modifier = Modifier
        .size(56.dp)
        .scale(if (isCurrent) pulseScale else 1f)
        .clip(CircleShape)
        .border(
          width = if (isCurrent) 3.dp else 2.dp,
          color = nodeBorder,
          shape = CircleShape
        )
        .clickable(enabled = isUnlocked) { onClick() }
        .testTag("level_node_${level.levelId}"),
      shape = CircleShape,
      color = nodeBg,
      shadowElevation = if (isCurrent) 6.dp else if (isCompleted) 3.dp else 0.dp
    ) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        if (!isUnlocked) {
          Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = "Locked",
            tint = TextMuted.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
          )
        } else {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Text(
              text = "${level.levelId}",
              fontSize = if (level.levelId >= 100) 14.sp else 16.sp,
              fontWeight = FontWeight.Black,
              color = if (isCurrent) Color.White else if (isCompleted) BentoPrimaryDark else TextPrimary
            )

            // Star Rating Indicator
            if (isCompleted) {
              Row(
                modifier = Modifier.padding(top = 1.dp),
                horizontalArrangement = Arrangement.Center
              ) {
                for (s in 1..3) {
                  Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = if (s <= level.stars) AmberGold else BentoDivider,
                    modifier = Modifier.size(9.dp)
                  )
                }
              }
            } else if (isCurrent) {
              Text(
                text = "PLAY",
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.95f),
                letterSpacing = 0.5.sp
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MissionMapHeader(
  totalStars: Int,
  maxStars: Int,
  hintTickets: Int,
  onClaimReward: () -> Unit,
  onSettingsClick: () -> Unit
) {
  Surface(
    color = BentoSurface,
    shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
    shadowElevation = 0.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // App Title Bento
      Column {
        Text(
          text = "ARROW ESCAPE",
          fontSize = 18.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 1.2.sp,
          color = BentoPrimary
        )
        Text(
          text = "220 Master Puzzles",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        // Total Stars Pill
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = BentoContainerDeep,
          modifier = Modifier.padding(end = 8.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Filled.Star,
              contentDescription = "Stars",
              tint = AmberGold,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "$totalStars",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              color = TextPrimary
            )
          }
        }

        // Hints Pill
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = BentoContainerDeep,
          modifier = Modifier
            .padding(end = 6.dp)
            .clickable { onClaimReward() }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Filled.AutoAwesome,
              contentDescription = "Hints",
              tint = BentoPrimary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "$hintTickets",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              color = TextPrimary
            )
          }
        }

        // Settings Button
        IconButton(
          onClick = onSettingsClick,
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BentoContainerDeep)
        ) {
          Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = "Settings",
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun ChapterTabsRow(
  chapters: List<WorldChapter>,
  selectedIndex: Int,
  onSelect: (Int) -> Unit
) {
  ScrollableTabRow(
    selectedTabIndex = selectedIndex,
    containerColor = BentoBackground,
    contentColor = TextPrimary,
    edgePadding = 20.dp,
    indicator = { tabPositions ->
      if (selectedIndex < tabPositions.size) {
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
          color = BentoPrimary,
          height = 3.dp
        )
      }
    },
    divider = {}
  ) {
    chapters.forEachIndexed { index, chapter ->
      val isSelected = selectedIndex == index
      Tab(
        selected = isSelected,
        onClick = { onSelect(index) },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = chapter.iconEmoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "CH ${chapter.id + 1}",
              fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
              fontSize = 13.sp,
              color = if (isSelected) BentoPrimary else TextMuted
            )
          }
        }
      )
    }
  }
}

@Composable
private fun ChapterBannerCard(
  chapter: WorldChapter,
  levels: List<LevelItemUi>
) {
  val completedInChapter = levels.count { it.isCompleted }
  val totalInChapter = levels.size
  val starsInChapter = levels.sumOf { it.stars }
  val maxStarsInChapter = totalInChapter * 3
  val progress = if (totalInChapter > 0) completedInChapter.toFloat() / totalInChapter else 0f

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 10.dp),
    shape = RoundedCornerShape(24.dp),
    color = BentoContainerDeep,
    shadowElevation = 3.dp
  ) {
    Box(modifier = Modifier.fillMaxWidth()) {
      // High-res Background Art with subtle dark gradient overlay
      Image(
        painter = painterResource(id = chapter.bannerDrawableRes),
        contentDescription = chapter.name,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
          .alpha(0.35f)
      )

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
          .background(
            Brush.verticalGradient(
              colors = listOf(
                BentoSurface.copy(alpha = 0.75f),
                BentoContainerDeep.copy(alpha = 0.95f)
              )
            )
          )
      )

      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "${chapter.iconEmoji} ${chapter.name}",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
              )
              Spacer(modifier = Modifier.width(8.dp))
              DifficultyBadge(difficulty = chapter.difficulty)
            }
            Text(
              text = chapter.subtitle,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = TextSecondary
            )
          }

          // Stars earned in Bento Pill
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = BentoSurface.copy(alpha = 0.9f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = AmberGold,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "$starsInChapter / $maxStarsInChapter",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chapter completion progress bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
              .weight(1f)
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = BentoPrimary,
            trackColor = BentoDivider.copy(alpha = 0.6f)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "$completedInChapter/$totalInChapter Done",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        }
      }
    }
  }
}

