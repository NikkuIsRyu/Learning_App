package tech.nikku.learningapp.navigation

import kotlinx.serialization.Serializable

@Serializable
object TodayRoute

@Serializable
object JapaneseRoute

@Serializable
object ChineseRoute

@Serializable
object VaultRoute

@Serializable
data class ChapterDetailRoute(val chapter: Int)

@Serializable
data class ConjugationMatrixRoute(val chapter: Int? = null)

@Serializable
data class ConjugationDrillRoute(val wordId: String? = null, val targetForm: String? = null)

@Serializable
data class ParticleClozeRoute(val chapter: Int? = null)

@Serializable
data class SentenceBuilderRoute(val chapter: Int? = null)

@Serializable
data class ListeningPlayerRoute(val chapter: Int? = null)

@Serializable
data class ChapterKanjiRoute(val chapter: Int? = null)

@Serializable
object MasterLexiconRoute

@Serializable
data class WordDetailRoute(val wordId: String)

@Serializable
data class KanjiDetailRoute(val kanjiLiteral: String)

@Serializable
data class GrammarExplorerRoute(val category: String? = null)

@Serializable
data class GrammarDetailRoute(val ruleId: String)

@Serializable
data class GrammarCardsRoute(val category: String? = null)

@Serializable
object ConfusablePairsRoute

@Serializable
object DialogueListRoute

@Serializable
data class DialoguePlayerRoute(val dialogueId: String)

@Serializable
data class ReviewSessionRoute(val domain: String? = null)

@Serializable
object SettingsRoute

@Serializable
data class FeedbackSheetRoute(val itemKey: String? = null)
