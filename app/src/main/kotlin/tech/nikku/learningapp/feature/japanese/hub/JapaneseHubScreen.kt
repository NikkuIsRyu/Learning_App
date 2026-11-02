package tech.nikku.learningapp.feature.japanese.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikku.learningapp.core.designsystem.component.FuriganaText
import tech.nikku.learningapp.core.designsystem.theme.AppDomain
import tech.nikku.learningapp.core.designsystem.theme.LearningAppTheme

@Composable
fun JapaneseHubRoute(
  modifier: Modifier = Modifier,
  onChapterClick: (Int) -> Unit = {},
  viewModel: JapaneseHubViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  JapaneseHubScreen(
    state = uiState,
    onEvent = { event ->
      when (event) {
        is JapaneseHubEvent.SelectChapter -> onChapterClick(event.chapter)
      }
    },
    modifier = modifier
  )
}

@Composable
fun JapaneseHubScreen(
  state: JapaneseHubUiState,
  onEvent: (JapaneseHubEvent) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 160.dp),
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    contentPadding = PaddingValues(vertical = 16.dp)
  ) {
    item(span = { GridItemSpan(maxLineSpan) }) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Minna no Nihongo Hub",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        FuriganaText(
          text = "{皆|みんな}の{日|に}{本|ほん}{語|ご} 初級 I & II (Chapters 1–50)",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.padding(horizontal = 16.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Verified status",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "Chapters 26–32 Verified & Errata Fixed",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }

    items(
      items = state.chapters,
      key = { it.chapter }
    ) { item ->
      ChapterCard(
        item = item,
        onClick = { onEvent(JapaneseHubEvent.SelectChapter(item.chapter)) }
      )
    }
  }
}

@Composable
fun ChapterCard(
  item: ChapterHubItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick),
    colors = CardDefaults.cardColors(
      containerColor = if (item.isVerified) {
        MaterialTheme.colorScheme.surfaceVariant
      } else {
        MaterialTheme.colorScheme.surface
      }
    ),
    elevation = CardDefaults.cardElevation(
      defaultElevation = if (item.isVerified) 3.dp else 1.dp
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "第${item.chapter}課",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        if (item.isVerified) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.height(24.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(12.dp)
              )
              Text(
                text = "Verified",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      Text(
        text = if (item.isVerified) {
          "${item.wordCount} words • Grammar • Kanji"
        } else {
          "Coming Soon"
        },
        style = MaterialTheme.typography.bodySmall,
        color = if (item.isVerified) {
          MaterialTheme.colorScheme.onSurfaceVariant
        } else {
          MaterialTheme.colorScheme.outline
        }
      )
    }
  }
}

@Preview(name = "Japanese Hub Preview", showBackground = true)
@Composable
fun JapaneseHubScreenPreview() {
  val previewItems = (1..50).map { ch ->
    ChapterHubItem(
      chapter = ch,
      wordCount = when (ch) {
        26 -> 45
        27 -> 41
        28 -> 54
        29 -> 51
        30 -> 48
        31 -> 39
        32 -> 57
        else -> 0
      },
      isVerified = ch in 26..32
    )
  }

  LearningAppTheme(domain = AppDomain.JAPANESE) {
    Surface {
      JapaneseHubScreen(
        state = JapaneseHubUiState(chapters = previewItems, isLoading = false),
        onEvent = {}
      )
    }
  }
}
