package tech.nikku.learningapp.core.domain.usecase

import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.ItemType
import tech.nikku.learningapp.core.domain.model.ReviewGrade
import tech.nikku.learningapp.core.domain.model.UserProgress
import tech.nikku.learningapp.core.domain.repository.ProgressRepository

class GradeAnswerUseCase(
  private val progressRepository: ProgressRepository
) {

  suspend operator fun invoke(
    itemKey: String,
    itemType: ItemType,
    domain: Domain,
    grade: ReviewGrade,
    nowMs: Long = System.currentTimeMillis(),
    reviewDurationMs: Long = 0L
  ): UserProgress {
    return progressRepository.recordReview(
      itemKey = itemKey,
      itemType = itemType,
      domain = domain,
      grade = grade,
      nowMs = nowMs,
      reviewDurationMs = reviewDurationMs
    )
  }
}
