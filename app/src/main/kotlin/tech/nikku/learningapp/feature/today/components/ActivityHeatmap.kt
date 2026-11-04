package tech.nikku.learningapp.feature.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

/**
 * 12-week GitHub/Anki-style study activity heatmap.
 *
 * @param activityByEpochDay Map of epochDay -> count of reviews performed on that day.
 * @param streakDays Current consecutive active days.
 * @param modifier Modifier
 */
@Composable
fun ActivityHeatmap(
  activityByEpochDay: Map<Long, Int>,
  streakDays: Int,
  modifier: Modifier = Modifier
) {
  val today = remember { LocalDate.now() }
  val todayEpochDay = remember(today) { today.toEpochDay() }

  // 12 weeks = 84 days. We align the grid so the current week ends on Sunday or today.
  // Day of week: Monday=1 .. Sunday=7
  val currentDayOfWeek = today.dayOfWeek.value // 1..7 (Mon=1, Sun=7)
  val daysFromStartOfWeek = currentDayOfWeek - 1 // 0 for Mon, 6 for Sun

  // 11 full previous weeks (11 * 7 = 77 days) + current week up to today
  val totalDaysInGrid = 12 * 7
  val startEpochDay = todayEpochDay - daysFromStartOfWeek - (11 * 7)

  val totalReviews = remember(activityByEpochDay) {
    activityByEpochDay.values.sum()
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Header: Title and Streak
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Activity Heatmap",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "$totalReviews reviews in last 12 weeks",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(text = "🔥", fontSize = 16.sp)
          Text(
            text = "$streakDays day${if (streakDays == 1) "" else "s"}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Heatmap Grid: 7 rows (Mon..Sun) x 12 columns (weeks)
      val dayLabels = listOf("M", "", "W", "", "F", "", "S")

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Weekday initials column
        Column(
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.padding(end = 6.dp)
        ) {
          dayLabels.forEach { label ->
            Box(
              modifier = Modifier.size(14.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.outline
              )
            }
          }
        }

        // 12 Week Columns
        for (week in 0 until 12) {
          Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 2.dp)
          ) {
            for (day in 0 until 7) {
              val cellEpochDay = startEpochDay + (week * 7) + day
              val isFuture = cellEpochDay > todayEpochDay
              val count = if (isFuture) 0 else activityByEpochDay[cellEpochDay] ?: 0

              val cellColor = when {
                isFuture -> Color.Transparent
                count == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                count in 1..4 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                count in 5..9 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                else -> MaterialTheme.colorScheme.primary
              }

              Box(
                modifier = Modifier
                  .size(14.dp)
                  .clip(RoundedCornerShape(3.dp))
                  .background(cellColor)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Legend Row (Less -> More)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Less",
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.outline,
          modifier = Modifier.padding(end = 6.dp)
        )
        val legendLevels = listOf(
          MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
          MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
          MaterialTheme.colorScheme.primary
        )
        legendLevels.forEach { color ->
          Box(
            modifier = Modifier
              .padding(horizontal = 2.dp)
              .size(10.dp)
              .clip(RoundedCornerShape(2.dp))
              .background(color)
          )
        }
        Text(
          text = "More",
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.outline,
          modifier = Modifier.padding(start = 6.dp)
        )
      }
    }
  }
}
