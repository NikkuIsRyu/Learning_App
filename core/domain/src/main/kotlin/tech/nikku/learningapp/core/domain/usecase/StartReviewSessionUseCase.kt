package tech.nikku.learningapp.core.domain.usecase

import kotlinx.coroutines.flow.first
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.ReviewItem
import tech.nikku.learningapp.core.domain.model.SrsState
import tech.nikku.learningapp.core.domain.repository.ProgressRepository
import tech.nikku.learningapp.core.domain.repository.SettingsRepository

class StartReviewSessionUseCase(
  private val progressRepository: ProgressRepository,
  private val settingsRepository: SettingsRepository? = null
) {

  suspend operator fun invoke(
    domain: Domain? = null,
    chapters: Set<Int>? = null,
    dailyNewLimit: Int? = null,
    limit: Int = 50,
    nowMs: Long = System.currentTimeMillis()
  ): List<ReviewItem> {
    val activeChapters = chapters
      ?: settingsRepository?.observeActiveChapters()?.first()
      ?: (26..32).toSet()

    val newLimit = dailyNewLimit
      ?: settingsRepository?.observeDailyNewLimit()?.first()
      ?: 15

    val allDue = progressRepository.observeDueReviews(domain, nowMs).first()

    val filteredDue = allDue.filter { item ->
      val ch = item.word?.chapter
        ?: item.kanji?.chapter
        ?: item.grammar?.chapter
        ?: item.exercise?.chapter
        ?: item.confusable?.chapter
      ch == null || ch in activeChapters
    }

    val reviewItems = filteredDue.filter { it.progress.state != SrsState.NEW }
    val newItems = filteredDue.filter { it.progress.state == SrsState.NEW }.take(newLimit)

    return interleave(reviewItems, newItems).take(limit)
  }

  companion object {
    fun interleave(reviews: List<ReviewItem>, newItems: List<ReviewItem>): List<ReviewItem> {
      val result = mutableListOf<ReviewItem>()
      var rIdx = 0
      var nIdx = 0
      while (rIdx < reviews.size || nIdx < newItems.size) {
        val rEnd = minOf(rIdx + 4, reviews.size)
        for (i in rIdx until rEnd) {
          result.add(reviews[i])
        }
        rIdx = rEnd
        if (nIdx < newItems.size) {
          result.add(newItems[nIdx])
          nIdx++
        }
      }
      return result
    }
  }
}
