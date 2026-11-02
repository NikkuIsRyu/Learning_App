package tech.nikku.learningapp.feature.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.nikku.learningapp.R
import tech.nikku.learningapp.core.designsystem.theme.AppDomain
import tech.nikku.learningapp.core.designsystem.theme.LearningAppTheme
import tech.nikku.learningapp.core.domain.model.VaultModule

data class VaultUiState(
  val modules: List<VaultModule> = emptyList(),
  val notifiedModuleIds: Set<String> = emptySet(),
  val isLoading: Boolean = false
)

sealed interface VaultEvent {
  data class NotifyInterest(val moduleId: String) : VaultEvent
}

@Composable
fun VaultRoute(
  modifier: Modifier = Modifier,
  viewModel: VaultViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsState()
  VaultScreen(
    state = uiState,
    onEvent = viewModel::onEvent,
    modifier = modifier
  )
}

@Composable
fun VaultScreen(
  state: VaultUiState,
  onEvent: (VaultEvent) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      VaultHeroBanner()
    }

    items(state.modules, key = { it.id }) { module ->
      VaultModuleCard(
        module = module,
        isNotified = state.notifiedModuleIds.contains(module.id),
        onNotifyClick = { onEvent(VaultEvent.NotifyInterest(module.id)) }
      )
    }
  }
}

@Composable
private fun VaultHeroBanner() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer
    )
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = stringResource(R.string.vault_title),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "WIP",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
          )
        }
      }
      Text(
        text = stringResource(R.string.vault_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
      )
    }
  }
}

@Composable
private fun VaultModuleCard(
  module: VaultModule,
  isNotified: Boolean,
  onNotifyClick: () -> Unit
) {
  val badgeBgColor: Color
  val badgeTextColor: Color

  when (module.badge.lowercase()) {
    "next up" -> {
      badgeBgColor = MaterialTheme.colorScheme.primary
      badgeTextColor = MaterialTheme.colorScheme.onPrimary
    }
    "drafting" -> {
      badgeBgColor = Color(0xFFF57F17) // Amber
      badgeTextColor = Color.White
    }
    else -> { // Planned
      badgeBgColor = MaterialTheme.colorScheme.surfaceVariant
      badgeTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    }
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = module.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(1f)
        )
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(badgeBgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = module.badge,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = badgeTextColor
          )
        }
      }

      Text(
        text = module.description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      FilledTonalButton(
        onClick = onNotifyClick,
        enabled = false, // Disabled CTA as specified in spec §10.5
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
          disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
      ) {
        Text(
          text = if (isNotified) "Interest registered ✓" else stringResource(R.string.vault_notify_me),
          style = MaterialTheme.typography.labelMedium
        )
      }
    }
  }
}

private val FAKE_MODULES = listOf(
  VaultModule(
    id = "vault:cs:algo",
    title = "Algorithms & Data Structures",
    badge = "Next up",
    description = "Trees, graphs, dynamic programming, amortization, and asymptotic algorithmic analysis."
  ),
  VaultModule(
    id = "vault:cs:ise",
    title = "Information Systems Engineering (ISE)",
    badge = "Drafting",
    description = "Software architecture, microservices, Clean Architecture, and domain-driven design."
  ),
  VaultModule(
    id = "vault:cs:theo",
    title = "Theoretical Computer Science",
    badge = "Planned",
    description = "Automata, formal languages, computability, complexity theory, and P vs NP."
  )
)

@Preview(name = "Vault Screen Light", showBackground = true)
@Composable
fun VaultScreenPreview() {
  LearningAppTheme(domain = AppDomain.VAULT, darkTheme = false) {
    Surface {
      VaultScreen(
        state = VaultUiState(modules = FAKE_MODULES),
        onEvent = {}
      )
    }
  }
}

@Preview(name = "Vault Screen Dark", showBackground = true)
@Composable
fun VaultScreenDarkPreview() {
  LearningAppTheme(domain = AppDomain.VAULT, darkTheme = true) {
    Surface {
      VaultScreen(
        state = VaultUiState(modules = FAKE_MODULES),
        onEvent = {}
      )
    }
  }
}
