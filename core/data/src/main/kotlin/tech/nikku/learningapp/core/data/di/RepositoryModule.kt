package tech.nikku.learningapp.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.nikku.learningapp.core.data.repository.ContentRepositoryImpl
import tech.nikku.learningapp.core.data.repository.SettingsRepositoryImpl
import tech.nikku.learningapp.core.domain.repository.ContentRepository
import tech.nikku.learningapp.core.domain.repository.SettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

  @Binds
  @Singleton
  abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

  @Binds
  @Singleton
  abstract fun bindContentRepository(impl: ContentRepositoryImpl): ContentRepository
}
