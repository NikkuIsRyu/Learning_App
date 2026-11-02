package tech.nikku.learningapp.feature.shared.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.nikku.learningapp.core.domain.model.Dialogue
import tech.nikku.learningapp.core.domain.model.DialogueTurn
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.SpeakerInfo
import tech.nikku.learningapp.ui.theme.LearningAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialoguePlayerScreen(
  dialogue: Dialogue,
  activeTurnIndex: Int,
  isPlaying: Boolean,
  displayMode: DisplayMode,
  speed: Float,
  autoPause: Boolean,
  onPlayPauseClick: () -> Unit,
  onTurnClick: (DialogueTurn) -> Unit,
  onDisplayModeChange: (DisplayMode) -> Unit,
  onSpeedChange: (Float) -> Unit,
  onAutoPauseToggle: (Boolean) -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val listState = rememberLazyListState()

  // Auto-scroll to active turn when playing
  LaunchedEffect(activeTurnIndex) {
    if (activeTurnIndex in dialogue.turns.indices) {
      listState.animateScrollToItem(activeTurnIndex)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = dialogue.nativeTitle,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = dialogue.titleEn,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    },
    bottomBar = {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Replay active button
            OutlinedButton(
              onClick = {
                val turn = dialogue.turns.getOrNull(activeTurnIndex)
                if (turn != null) onTurnClick(turn)
              },
              enabled = activeTurnIndex in dialogue.turns.indices
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Replay Sentence", fontSize = 12.sp)
            }

            // Play / Pause Toggle
            Button(
              onClick = onPlayPauseClick,
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
              )
            ) {
              Icon(
                if (isPlaying) Icons.Default.Refresh else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isPlaying) "Pause" else "Play All",
                fontWeight = FontWeight.Bold
              )
            }
          }

          // Speed & Auto-Pause controls
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Speed Preset Chips
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              listOf(0.75f, 1.0f, 1.25f).forEach { s ->
                val isSelected = kotlin.math.abs(speed - s) < 0.05f
                FilterChip(
                  selected = isSelected,
                  onClick = { onSpeedChange(s) },
                  label = { Text("${s}×", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                  )
                )
              }
            }

            // Auto-pause chip for shadowing
            FilterChip(
              selected = autoPause,
              onClick = { onAutoPauseToggle(!autoPause) },
              label = { Text(if (autoPause) "Pause: ON" else "Pause: OFF", fontSize = 11.sp) }
            )
          }
        }
      }
    },
    modifier = modifier.fillMaxSize()
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      // Script display mode selector
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        DisplayMode.entries.forEach { mode ->
          val isSelected = displayMode == mode
          FilterChip(
            selected = isSelected,
            onClick = { onDisplayModeChange(mode) },
            label = {
              Text(
                text = mode.label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
          )
        }
      }

      // Turns List
      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        itemsIndexed(dialogue.turns) { index, turn ->
          TurnRow(
            turn = turn,
            isActive = index == activeTurnIndex,
            displayMode = displayMode,
            speakerInfo = dialogue.speakers[turn.speaker],
            onTurnClick = { onTurnClick(turn) },
            domain = dialogue.domain
          )
        }
        item {
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}

// ──────────────────────────────── PREVIEWS ────────────────────────────────

@Preview(showBackground = true)
@Composable
fun DialoguePlayerJapanesePreview() {
  LearningAppTheme {
    val sampleDialogue = Dialogue(
      id = "ja:dia:26:01",
      domain = Domain.JAPANESE,
      chapter = 26,
      titleEn = "A Visit to the Doctor",
      titleDe = "Ein Besuch beim Arzt",
      nativeTitle = "病院での診察",
      topicEn = "Explaining symptoms and asking for advice",
      audioRef = "media/ja/dialogue/l26_01.mp3",
      durationMs = 15000L,
      speakers = mapOf(
        "A" to SpeakerInfo(
          label = "Yamada",
          role = "Patient",
          gender = "M",
          colorHex = "#1E88E5"
        ),
        "B" to SpeakerInfo(
          label = "Doctor",
          role = "Physician",
          gender = "F",
          colorHex = "#43A047"
        )
      ),
      turns = listOf(
        DialogueTurn(
          dialogueId = "ja:dia:26:01",
          idx = 0,
          speaker = "B",
          text = "どうしたんですか。",
          rubyMarkup = "どうしたんですか。",
          romanization = "Dō shitan desu ka.",
          translationEn = "What seems to be the problem?",
          translationDe = "Was fehlt Ihnen denn?",
          startMs = 500L,
          endMs = 2800L
        ),
        DialogueTurn(
          dialogueId = "ja:dia:26:01",
          idx = 1,
          speaker = "A",
          text = "昨日から喉が痛くて、熱もあるんです。",
          rubyMarkup = "{昨日|きのう}から {喉|のど}が {痛|いた}くて、{熱|ねつ}もあるんです。",
          romanization = "Kinō kara nodo ga itakute, netsu mo arun desu.",
          translationEn = "My throat has been hurting since yesterday, and I have a fever too.",
          translationDe = "Seit gestern tut mir der Hals weh und ich habe auch Fieber.",
          startMs = 3200L,
          endMs = 7900L
        ),
        DialogueTurn(
          dialogueId = "ja:dia:26:01",
          idx = 2,
          speaker = "B",
          text = "そうですか。ちょっと口を開けてください。",
          rubyMarkup = "そうですか。ちょっと {口|くち}を {開|あ}けて ください。",
          romanization = "Sō desu ka. Chotto kuchi o akete kudasai.",
          translationEn = "I see. Please open your mouth for a moment.",
          translationDe = "Verstehe. Bitte machen Sie mal den Mund auf.",
          startMs = 8400L,
          endMs = 13200L
        )
      )
    )

    DialoguePlayerScreen(
      dialogue = sampleDialogue,
      activeTurnIndex = 1, // Second turn active (highlighted)
      isPlaying = true,
      displayMode = DisplayMode.FURIGANA,
      speed = 1.0f,
      autoPause = false,
      onPlayPauseClick = {},
      onTurnClick = {},
      onDisplayModeChange = {},
      onSpeedChange = {},
      onAutoPauseToggle = {},
      onBackClick = {}
    )
  }
}
