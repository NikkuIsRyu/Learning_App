package tech.nikku.learningapp.core.domain.usecase

import tech.nikku.learningapp.core.domain.engine.srs.SrsConfig
import tech.nikku.learningapp.core.domain.engine.srs.SrsScheduler
import tech.nikku.learningapp.core.domain.engine.srs.SrsSnapshot
import tech.nikku.learningapp.core.domain.model.ReviewLog
import java.time.Instant

class ReplayProgressUseCase {

  operator fun invoke(
    itemKey: String,
    logs: List<ReviewLog>,
    cfg: SrsConfig = SrsConfig(),
    resetAtMs: Long = 0L,
    initial: SrsSnapshot = SrsSnapshot.initial(itemKey)
  ): SrsSnapshot {
    val validLogs = logs
      .filter { it.timestampMs >= resetAtMs }
      .sortedWith(compareBy({ it.timestampMs }, { it.id }))

    return validLogs.fold(initial) { current, log ->
      val now = Instant.ofEpochMilli(log.timestampMs)
      val seed = SrsScheduler.deriveSeed(itemKey, current.totalReviews)
      SrsScheduler.schedule(current, log.grade, now, cfg, seed)
    }
  }
}
