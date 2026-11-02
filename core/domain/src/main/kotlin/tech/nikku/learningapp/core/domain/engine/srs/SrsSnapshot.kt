package tech.nikku.learningapp.core.domain.engine.srs

import tech.nikku.learningapp.core.domain.model.SrsState
import java.time.Instant

data class SrsSnapshot(
  val itemKey: String,
  val state: SrsState = SrsState.NEW,
  val intervalDays: Double = 0.0,
  val easeFactor: Double = 2.5,
  val repetitions: Int = 0,
  val lapses: Int = 0,
  val learningStep: Int = 0,
  val dueAt: Instant = Instant.EPOCH,
  val lastReviewedAt: Instant? = null,
  val totalReviews: Int = 0,
  val correctReviews: Int = 0
) {
  companion object {
    fun initial(itemKey: String, dueAt: Instant = Instant.EPOCH): SrsSnapshot {
      return SrsSnapshot(
        itemKey = itemKey,
        state = SrsState.NEW,
        intervalDays = 0.0,
        easeFactor = 2.5,
        repetitions = 0,
        lapses = 0,
        learningStep = 0,
        dueAt = dueAt,
        lastReviewedAt = null,
        totalReviews = 0,
        correctReviews = 0
      )
    }
  }
}
