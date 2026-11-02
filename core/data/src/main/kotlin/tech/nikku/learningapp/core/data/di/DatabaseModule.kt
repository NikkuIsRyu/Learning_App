package tech.nikku.learningapp.core.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tech.nikku.learningapp.core.data.local.LearningDatabase
import tech.nikku.learningapp.core.data.local.dao.ConfusableDao
import tech.nikku.learningapp.core.data.local.dao.DialogueDao
import tech.nikku.learningapp.core.data.local.dao.ExerciseDao
import tech.nikku.learningapp.core.data.local.dao.FeedbackDao
import tech.nikku.learningapp.core.data.local.dao.GrammarDao
import tech.nikku.learningapp.core.data.local.dao.KanjiDao
import tech.nikku.learningapp.core.data.local.dao.ProgressDao
import tech.nikku.learningapp.core.data.local.dao.ReviewLogDao
import tech.nikku.learningapp.core.data.local.dao.WordDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

  @Provides
  @Singleton
  fun provideDatabase(@ApplicationContext context: Context): LearningDatabase {
    return Room.databaseBuilder(
      context,
      LearningDatabase::class.java,
      LearningDatabase.NAME
    ).build()
  }

  @Provides
  fun provideWordDao(db: LearningDatabase): WordDao = db.wordDao()

  @Provides
  fun provideKanjiDao(db: LearningDatabase): KanjiDao = db.kanjiDao()

  @Provides
  fun provideGrammarDao(db: LearningDatabase): GrammarDao = db.grammarDao()

  @Provides
  fun provideExerciseDao(db: LearningDatabase): ExerciseDao = db.exerciseDao()

  @Provides
  fun provideDialogueDao(db: LearningDatabase): DialogueDao = db.dialogueDao()

  @Provides
  fun provideConfusableDao(db: LearningDatabase): ConfusableDao = db.confusableDao()

  @Provides
  fun provideProgressDao(db: LearningDatabase): ProgressDao = db.progressDao()

  @Provides
  fun provideReviewLogDao(db: LearningDatabase): ReviewLogDao = db.reviewLogDao()

  @Provides
  fun provideFeedbackDao(db: LearningDatabase): FeedbackDao = db.feedbackDao()
}
