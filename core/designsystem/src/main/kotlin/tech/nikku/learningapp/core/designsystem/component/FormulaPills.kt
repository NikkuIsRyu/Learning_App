package tech.nikku.learningapp.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.nikku.learningapp.core.designsystem.theme.AppDomain
import tech.nikku.learningapp.core.designsystem.theme.ChineseLocaleList
import tech.nikku.learningapp.core.designsystem.theme.LearningAppTheme
import tech.nikku.learningapp.core.designsystem.theme.RoleConnectorDark
import tech.nikku.learningapp.core.designsystem.theme.RoleConnectorLight
import tech.nikku.learningapp.core.designsystem.theme.RoleMeasureDark
import tech.nikku.learningapp.core.designsystem.theme.RoleMeasureLight
import tech.nikku.learningapp.core.designsystem.theme.RoleModalNegationDark
import tech.nikku.learningapp.core.designsystem.theme.RoleModalNegationLight
import tech.nikku.learningapp.core.designsystem.theme.RoleObjectDark
import tech.nikku.learningapp.core.designsystem.theme.RoleObjectLight
import tech.nikku.learningapp.core.designsystem.theme.RoleParticleDark
import tech.nikku.learningapp.core.designsystem.theme.RoleParticleLight
import tech.nikku.learningapp.core.designsystem.theme.RolePlaceDark
import tech.nikku.learningapp.core.designsystem.theme.RolePlaceLight
import tech.nikku.learningapp.core.designsystem.theme.RoleSubjectDark
import tech.nikku.learningapp.core.designsystem.theme.RoleSubjectLight
import tech.nikku.learningapp.core.designsystem.theme.RoleTimeDark
import tech.nikku.learningapp.core.designsystem.theme.RoleTimeLight
import tech.nikku.learningapp.core.designsystem.theme.RoleTopicDark
import tech.nikku.learningapp.core.designsystem.theme.RoleTopicLight
import tech.nikku.learningapp.core.designsystem.theme.RoleVerbDark
import tech.nikku.learningapp.core.designsystem.theme.RoleVerbLight

enum class SyntaxRole(val label: String) {
  SUBJECT("Subject"),
  TOPIC("Topic"),
  TIME("Time"),
  PLACE("Place"),
  MODAL_NEGATION("Modal/Negation"),
  VERB("Verb"),
  OBJECT("Object"),
  PARTICLE("Particle"),
  MEASURE("Measure"),
  CONNECTOR("Connector")
}

data class PatternToken(
  val text: String,
  val role: SyntaxRole,
  val tooltip: String? = null
)

fun syntaxRoleColor(role: SyntaxRole, isDark: Boolean): Color {
  return when (role) {
    SyntaxRole.SUBJECT -> if (isDark) RoleSubjectDark else RoleSubjectLight
    SyntaxRole.TOPIC -> if (isDark) RoleTopicDark else RoleTopicLight
    SyntaxRole.TIME -> if (isDark) RoleTimeDark else RoleTimeLight
    SyntaxRole.PLACE -> if (isDark) RolePlaceDark else RolePlaceLight
    SyntaxRole.MODAL_NEGATION -> if (isDark) RoleModalNegationDark else RoleModalNegationLight
    SyntaxRole.VERB -> if (isDark) RoleVerbDark else RoleVerbLight
    SyntaxRole.OBJECT -> if (isDark) RoleObjectDark else RoleObjectLight
    SyntaxRole.PARTICLE -> if (isDark) RoleParticleDark else RoleParticleLight
    SyntaxRole.MEASURE -> if (isDark) RoleMeasureDark else RoleMeasureLight
    SyntaxRole.CONNECTOR -> if (isDark) RoleConnectorDark else RoleConnectorLight
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormulaPills(
  tokens: List<PatternToken>,
  modifier: Modifier = Modifier,
  darkTheme: Boolean = isSystemInDarkTheme()
) {
  FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    for (token in tokens) {
      val pillColor = syntaxRoleColor(token.role, darkTheme)
      val description = token.tooltip ?: "${token.role.label}: ${token.text}"

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(pillColor.copy(alpha = if (darkTheme) 0.35f else 0.18f))
          .padding(horizontal = 10.dp, vertical = 6.dp)
          .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = token.text,
          style = MaterialTheme.typography.labelLarge.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            localeList = ChineseLocaleList
          ),
          color = pillColor
        )
      }
    }
  }
}

@Preview(name = "Chinese Grammar Formula Pills", showBackground = true)
@Composable
fun FormulaPillsPreview() {
  val sampleTokens = listOf(
    PatternToken("主语 (S)", SyntaxRole.SUBJECT),
    PatternToken("+", SyntaxRole.CONNECTOR),
    PatternToken("时间 (Time)", SyntaxRole.TIME),
    PatternToken("+", SyntaxRole.CONNECTOR),
    PatternToken("在 + 地点 (Place)", SyntaxRole.PLACE),
    PatternToken("+", SyntaxRole.CONNECTOR),
    PatternToken("动词 (V)", SyntaxRole.VERB),
    PatternToken("+", SyntaxRole.CONNECTOR),
    PatternToken("宾语 (O)", SyntaxRole.OBJECT)
  )

  LearningAppTheme(domain = AppDomain.CHINESE) {
    Surface(modifier = Modifier.padding(16.dp)) {
      FormulaPills(tokens = sampleTokens)
    }
  }
}
