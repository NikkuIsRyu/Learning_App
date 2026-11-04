package tech.nikku.learningapp.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikku.learningapp.core.designsystem.theme.AppDomain
import tech.nikku.learningapp.core.designsystem.theme.LearningAppTheme
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.feature.today.components.ActivityHeatmap
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TodayRoute(
  modifier: Modifier = Modifier,
  onStartReview: (Domain?) -> Unit = {},
  viewModel: TodayViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  TodayScreen(
    state = uiState,
    onStartReview = onStartReview,
    modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
  state: TodayUiState,
  onStartReview: (Domain?) -> Unit,
  modifier: Modifier = Modifier
) {
  val todayFormatted = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Today",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = todayFormatted,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        actions = {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(end = 16.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(text = "🔥", fontSize = 14.sp)
              Text(
                text = "${state.streakDays}d streak",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }
        }
      )
    },
    modifier = modifier
  ) { innerPadding ->
    if (state.isLoading) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        CircularProgressIndicator()
      }
    } else {
      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        // 1. Big Primary Review Card
        item {
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Text(
                text = "${state.totalDueCount}",
                fontSize = 54.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = "Cards Due for Review Today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )

              Spacer(modifier = Modifier.height(4.dp))

              Button(
                onClick = { onStartReview(null) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.padding(start = 6.dp))
                Text(
                  text = "Start Review Session",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        // 2. Domain Breakdown Cards
        item {
          Text(
            text = "Domain Review Queues",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }

        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Japanese Card
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
              modifier = Modifier.weight(1f)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(
                  text = "🇯🇵 Japanese",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "${state.japaneseDueCount} due",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.primary
                )
                FilledTonalButton(
                  onClick = { onStartReview(Domain.JAPANESE) },
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(text = "Review", style = MaterialTheme.typography.labelMedium)
                }
              }
            }

            // Chinese Card
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
              modifier = Modifier.weight(1f)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(
                  text = "🇨🇳 Chinese",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "${state.chineseDueCount} due",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.secondary
                )
                FilledTonalButton(
                  onClick = { onStartReview(Domain.CHINESE) },
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(text = "Review", style = MaterialTheme.typography.labelMedium)
                }
              }
            }
          }
        }

        // 3. 12-Week Activity Heatmap
        item {
          ActivityHeatmap(
            activityByEpochDay = state.activityMap,
            streakDays = state.streakDays
          )
        }
      }
    }
  }
}

@Preview(name = "Today Screen Light", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun TodayScreenPreview() {
  LearningAppTheme(domain = AppDomain.TODAY) {
    Surface {
      val todayEpochDay = LocalDate.now().toEpochDay()
      val sampleActivity = mapOf(
        todayEpochDay to 14,
        todayEpochDay - 1 to 12,
        todayEpochDay - 2 to 8,
        todayEpochDay - 3 to 15,
        todayEpochDay - 5 to 7,
        todayEpochDay - 7 to 10,
        todayEpochDay - 14 to 20,
        todayEpochDay - 21 to 18
      )

      TodayScreen(
        state = TodayUiState(
          japaneseDueCount = 12,
          chineseDueCount = 5,
          totalDueCount = 17,
          streakDays = 5,
          activityMap = sampleActivity,
          isLoading = false
        ),
        onStartReview = {}
      )
    }
  }
}
