package tech.nikku.learningapp.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tech.nikku.learningapp.core.designsystem.theme.LearningAppTheme

/**
 * Pure Canvas rendering of KanjiVG stroke order paths.
 *
 * @param strokePaths KanjiVG SVG path `d` strings, each drawn in sequence.
 * @param progress Overall animation progress in range [0f, strokePaths.size.toFloat()].
 *                 If progress == strokePaths.size.toFloat(), all strokes are completely drawn.
 * @param strokeColor Active stroke color.
 * @param ghostColor Background faint guide color for uncompleted strokes.
 * @param gridColor Color of the traditional 2x2 practice guide lines.
 * @param showStrokeNumbers Whether to draw small stroke order index badges at stroke start points.
 * @param strokeWidth Direct stroke width in dp (not scaled by viewBox matrix).
 */
@Composable
fun StrokeOrderCanvas(
  strokePaths: List<String>,
  progress: Float,
  modifier: Modifier = Modifier,
  strokeColor: Color = MaterialTheme.colorScheme.primary,
  ghostColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
  gridColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
  showStrokeNumbers: Boolean = true,
  strokeNumberColor: Color = MaterialTheme.colorScheme.secondary,
  strokeWidth: Dp = 4.dp
) {
  val textMeasurer = rememberTextMeasurer()

  // Parse original paths once when strokePaths changes
  val parsedPaths = remember(strokePaths) {
    strokePaths.mapNotNull { d ->
      runCatching {
        PathParser().parsePathString(d).toPath()
      }.getOrNull()
    }
  }

  Canvas(modifier = modifier) {
    val canvasSize = size
    if (parsedPaths.isEmpty() || canvasSize.width <= 0f || canvasSize.height <= 0f) return@Canvas

    // 1. Draw practice grid lines (outer border + dashed center crosshairs)
    val w = canvasSize.width
    val h = canvasSize.height
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

    // Center vertical dashed line
    drawLine(
      color = gridColor,
      start = Offset(w / 2f, 0f),
      end = Offset(w / 2f, h),
      strokeWidth = 1.dp.toPx(),
      pathEffect = dashEffect
    )
    // Center horizontal dashed line
    drawLine(
      color = gridColor,
      start = Offset(0f, h / 2f),
      end = Offset(w, h / 2f),
      strokeWidth = 1.dp.toPx(),
      pathEffect = dashEffect
    )

    // 2. Compute viewBox (109x109) to Canvas scaling matrix
    val scale = minOf(w / 109f, h / 109f) * 0.88f
    val offsetX = (w - 109f * scale) / 2f
    val offsetY = (h - 109f * scale) / 2f

    val matrix = Matrix().apply {
      translate(offsetX, offsetY)
      scale(scale, scale)
    }

    val strokeWidthPx = strokeWidth.toPx()
    val strokeCap = StrokeCap.Round
    val strokeJoin = StrokeJoin.Round
    val activeStrokeStyle = Stroke(width = strokeWidthPx, cap = strokeCap, join = strokeJoin)
    val ghostStrokeStyle = Stroke(width = strokeWidthPx * 0.8f, cap = strokeCap, join = strokeJoin)

    // 3. Draw strokes
    val pathMeasure = PathMeasure()

    parsedPaths.forEachIndexed { index, path ->
      val scaledPath = Path().apply {
        addPath(path)
        transform(matrix)
      }

      // Draw faint ghost for full character context
      drawPath(
        path = scaledPath,
        color = ghostColor,
        style = ghostStrokeStyle
      )

      // Animate active stroke
      val strokeStartProgress = index.toFloat()
      val strokeEndProgress = (index + 1).toFloat()

      if (progress > strokeStartProgress) {
        if (progress >= strokeEndProgress) {
          // Entire stroke is completed
          drawPath(
            path = scaledPath,
            color = strokeColor,
            style = activeStrokeStyle
          )
        } else {
          // In-progress stroke
          val fraction = (progress - strokeStartProgress).coerceIn(0f, 1f)
          pathMeasure.setPath(scaledPath, false)
          val totalLen = pathMeasure.length
          if (totalLen > 0f) {
            val segment = Path()
            pathMeasure.getSegment(0f, totalLen * fraction, segment, true)
            drawPath(
              path = segment,
              color = strokeColor,
              style = activeStrokeStyle
            )
          }
        }
      }

      // 4. Draw stroke numbers at start positions
      if (showStrokeNumbers) {
        pathMeasure.setPath(scaledPath, false)
        if (pathMeasure.length > 0f) {
          val startPos = pathMeasure.getPosition(0f)
          val numText = "${index + 1}"
          val isCurrentOrDone = progress >= strokeStartProgress
          val numColor = if (isCurrentOrDone) strokeNumberColor else ghostColor

          // Draw small number offset slightly above-left
          drawText(
            textMeasurer = textMeasurer,
            text = numText,
            topLeft = Offset(startPos.x - 14.dp.toPx(), startPos.y - 14.dp.toPx()),
            style = TextStyle(
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = numColor
            )
          )
        }
      }
    }
  }
}

/**
 * Interactive Stroke Order Player with controls: Play/Pause, Step Back, Step Forward, Reset, Loop.
 */
@Composable
fun StrokeOrderPlayer(
  strokePaths: List<String>,
  modifier: Modifier = Modifier,
  strokeColor: Color = MaterialTheme.colorScheme.primary,
  autoPlay: Boolean = false,
  showControls: Boolean = true,
  strokeWidth: Dp = 4.dp
) {
  val totalStrokes = strokePaths.size
  var isPlaying by remember { mutableStateOf(autoPlay) }
  var isLooping by remember { mutableStateOf(true) }
  val currentProgress = remember { Animatable(if (autoPlay) 0f else totalStrokes.toFloat()) }
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(isPlaying, totalStrokes) {
    if (isPlaying && totalStrokes > 0) {
      while (isActive && isPlaying) {
        if (currentProgress.value >= totalStrokes.toFloat()) {
          currentProgress.snapTo(0f)
        }
        val remaining = totalStrokes.toFloat() - currentProgress.value
        val durationMs = (remaining * 650).toInt().coerceAtLeast(300)
        currentProgress.animateTo(
          targetValue = totalStrokes.toFloat(),
          animationSpec = tween(durationMillis = durationMs, easing = LinearEasing)
        )
        if (!isLooping) {
          isPlaying = false
          break
        }
        kotlinx.coroutines.delay(1000L) // pause before repeating
      }
    }
  }

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .background(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          shape = RoundedCornerShape(16.dp)
        )
        .border(
          width = 1.dp,
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
          shape = RoundedCornerShape(16.dp)
        )
        .padding(12.dp),
      contentAlignment = Alignment.Center
    ) {
      StrokeOrderCanvas(
        strokePaths = strokePaths,
        progress = currentProgress.value,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
        modifier = Modifier.fillMaxSize()
      )
    }

    if (showControls && totalStrokes > 0) {
      Spacer(modifier = Modifier.height(10.dp))

      val currentStrokeDisplay = (currentProgress.value.toInt() + 1).coerceAtMost(totalStrokes)
      Text(
        text = "Stroke $currentStrokeDisplay of $totalStrokes",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Step back
        IconButton(
          onClick = {
            coroutineScope.launch {
              isPlaying = false
              val target = (currentProgress.value - 1f).coerceAtLeast(0f)
              currentProgress.snapTo(target)
            }
          }
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Previous stroke"
          )
        }

        // Play / Pause toggle
        FilledTonalIconButton(
          onClick = {
            if (isPlaying) {
              isPlaying = false
            } else {
              coroutineScope.launch {
                if (currentProgress.value >= totalStrokes.toFloat()) {
                  currentProgress.snapTo(0f)
                }
                isPlaying = true
              }
            }
          }
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Default.Refresh else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause / Replay" else "Play"
          )
        }

        // Step forward
        IconButton(
          onClick = {
            coroutineScope.launch {
              isPlaying = false
              val target = (currentProgress.value + 1f).coerceAtMost(totalStrokes.toFloat())
              currentProgress.snapTo(target)
            }
          }
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Next stroke"
          )
        }

        // Reset / Replay from start
        IconButton(
          onClick = {
            coroutineScope.launch {
              currentProgress.snapTo(0f)
              isPlaying = true
            }
          }
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Replay from start"
          )
        }
      }
    }
  }
}

/**
 * Static Preview for Gate G3 verification.
 * Previews with progress = 1f (all strokes completed) to avoid blank first-frame render trap.
 */
@Preview(showBackground = true, widthDp = 300, heightDp = 360)
@Composable
fun StrokeOrderStaticPreview() {
  LearningAppTheme {
    Surface(
      modifier = Modifier
        .size(300.dp, 360.dp)
        .padding(16.dp)
    ) {
      // Kanji "語" (talk, language) sample KanjiVG stroke paths (14 strokes)
      val sampleGoStrokes = listOf(
        "M13.25,22.5c0.94,0.23,5.18,0.96,7.74,0.75c17.87-1.5,46.54-4.75,66.38-4.75c2.92,0,6.42,0.75,7.88,1.25",
        "M52.97,23.25c0.93,1.07,1.56,2.75,1.56,5.3c0,8.65-0.2,39.42-0.27,57.2c-0.02,3.86-0.02,5.89-0.02,8.25",
        "M67.83,37.17C72.75,39.5,79.88,47.62,82,52.12"
      )
      StrokeOrderPlayer(
        strokePaths = sampleGoStrokes,
        autoPlay = false,
        showControls = true
      )
    }
  }
}
