package tech.nikku.learningapp.core.data.repository

import kotlinx.coroutines.flow.first
import tech.nikku.learningapp.core.data.content.ContentSeeder
import tech.nikku.learningapp.core.data.content.SeedResult
import tech.nikku.learningapp.core.data.local.dao.WordDao
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.repository.ContentRepository
import tech.nikku.learningapp.core.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
  private val contentSeeder: ContentSeeder,
  private val settingsRepository: SettingsRepository,
  private val wordDao: WordDao
) : ContentRepository {

  override suspend fun seedContentIfEmpty(): Boolean {
    val count = wordDao.count(Domain.JAPANESE)
    val result = if (count == 0) {
      contentSeeder.seed(force = true)
    } else {
      contentSeeder.seed(force = false)
    }
    return result is SeedResult.Success || result is SeedResult.AlreadyUpToDate
  }

  override suspend fun getContentVersion(): Int {
    return settingsRepository.observeContentVersion().first()
  }
}
