package tech.nikku.learningapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.nikku.learningapp.core.domain.repository.ProgressRepository
import tech.nikku.learningapp.core.domain.usecase.GradeAnswerUseCase
import tech.nikku.learningapp.core.domain.usecase.StartReviewSessionUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

  @Provides
  @Singleton
  fun provideGradeAnswerUseCase(
    progressRepository: ProgressRepository
  ): GradeAnswerUseCase = GradeAnswerUseCase(progressRepository)

  @Provides
  @Singleton
  fun provideStartReviewSessionUseCase(
    progressRepository: ProgressRepository
  ): StartReviewSessionUseCase = StartReviewSessionUseCase(progressRepository)
}
