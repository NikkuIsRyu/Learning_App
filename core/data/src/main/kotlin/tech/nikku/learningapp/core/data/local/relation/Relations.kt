package tech.nikku.learningapp.core.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import tech.nikku.learningapp.core.data.local.entity.DialogueEntity
import tech.nikku.learningapp.core.data.local.entity.DialogueTurnEntity
import tech.nikku.learningapp.core.data.local.entity.GrammarExampleEntity
import tech.nikku.learningapp.core.data.local.entity.GrammarRuleEntity
import tech.nikku.learningapp.core.data.local.entity.KanjiCompoundCrossRef
import tech.nikku.learningapp.core.data.local.entity.KanjiEntity
import tech.nikku.learningapp.core.data.local.entity.ReviewLogEntity
import tech.nikku.learningapp.core.data.local.entity.UserProgressEntity
import tech.nikku.learningapp.core.data.local.entity.WordEntity
import tech.nikku.learningapp.core.domain.model.SrsState

data class KanjiWithCompounds(
  @Embedded val kanji: KanjiEntity,
  @Relation(
    parentColumn = "literal",
    entityColumn = "id",
    associateBy = Junction(value = KanjiCompoundCrossRef::class, parentColumn = "kanji", entityColumn = "wordId")
  )
  val compounds: List<WordEntity>
)

data class GrammarRuleWithExamples(
  @Embedded val rule: GrammarRuleEntity,
  @Relation(parentColumn = "id", entityColumn = "ruleId")
  val examples: List<GrammarExampleEntity>
)

data class DialogueWithTurns(
  @Embedded val dialogue: DialogueEntity,
  @Relation(parentColumn = "id", entityColumn = "dialogueId")
  val turns: List<DialogueTurnEntity>
)

data class ProgressWithHistory(
  @Embedded val progress: UserProgressEntity,
  @Relation(parentColumn = "itemKey", entityColumn = "itemKey")
  val logs: List<ReviewLogEntity>
)

/** Word + its 自他 partner, for transitivity pair cards. */
data class WordWithPair(
  @Embedded val word: WordEntity,
  @Relation(parentColumn = "pairWordId", entityColumn = "id")
  val pair: WordEntity?
)

data class StateCount(val state: SrsState, val count: Int)
data class DailyActivity(val epochDay: Long, val reviews: Int, val correct: Int)
data class ChapterCount(val chapter: Int, val count: Int)
