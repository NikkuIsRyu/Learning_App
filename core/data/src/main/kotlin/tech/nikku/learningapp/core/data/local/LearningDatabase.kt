package tech.nikku.learningapp.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tech.nikku.learningapp.core.data.local.converter.Converters
import tech.nikku.learningapp.core.data.local.dao.ConfusableDao
import tech.nikku.learningapp.core.data.local.dao.DialogueDao
import tech.nikku.learningapp.core.data.local.dao.ExerciseDao
import tech.nikku.learningapp.core.data.local.dao.FeedbackDao
import tech.nikku.learningapp.core.data.local.dao.GrammarDao
import tech.nikku.learningapp.core.data.local.dao.KanjiDao
import tech.nikku.learningapp.core.data.local.dao.ProgressDao
import tech.nikku.learningapp.core.data.local.dao.ReviewLogDao
import tech.nikku.learningapp.core.data.local.dao.WordDao
import tech.nikku.learningapp.core.data.local.entity.ConfusablePairEntity
import tech.nikku.learningapp.core.data.local.entity.DialogueEntity
import tech.nikku.learningapp.core.data.local.entity.DialogueTurnEntity
import tech.nikku.learningapp.core.data.local.entity.ExerciseEntity
import tech.nikku.learningapp.core.data.local.entity.FeedbackEntity
import tech.nikku.learningapp.core.data.local.entity.GrammarExampleEntity
import tech.nikku.learningapp.core.data.local.entity.GrammarRuleEntity
import tech.nikku.learningapp.core.data.local.entity.KanjiCompoundCrossRef
import tech.nikku.learningapp.core.data.local.entity.KanjiEntity
import tech.nikku.learningapp.core.data.local.entity.ReviewLogEntity
import tech.nikku.learningapp.core.data.local.entity.UserProgressEntity
import tech.nikku.learningapp.core.data.local.entity.WordEntity
import tech.nikku.learningapp.core.data.local.entity.WordFtsEntity

@Database(
  entities = [
    WordEntity::class,
    WordFtsEntity::class,
    KanjiEntity::class,
    KanjiCompoundCrossRef::class,
    GrammarRuleEntity::class,
    GrammarExampleEntity::class,
    ExerciseEntity::class,
    DialogueEntity::class,
    DialogueTurnEntity::class,
    ConfusablePairEntity::class,
    UserProgressEntity::class,
    ReviewLogEntity::class,
    FeedbackEntity::class
  ],
  version = 1,
  exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LearningDatabase : RoomDatabase() {
  abstract fun wordDao(): WordDao
  abstract fun kanjiDao(): KanjiDao
  abstract fun grammarDao(): GrammarDao
  abstract fun exerciseDao(): ExerciseDao
  abstract fun dialogueDao(): DialogueDao
  abstract fun confusableDao(): ConfusableDao
  abstract fun progressDao(): ProgressDao
  abstract fun reviewLogDao(): ReviewLogDao
  abstract fun feedbackDao(): FeedbackDao

  companion object {
    const val NAME = "learning.db"
  }
}
