package tech.nikku.learningapp.core.domain.engine.srs

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object LearningDay {

  /**
   * Returns the learning-day date for a given instant.
   * If local time in [zoneId] is before [cutoffHour], the learning day is the previous calendar day.
   */
  fun getLearningDate(now: Instant, cutoffHour: Int, zoneId: ZoneId): LocalDate {
    val zdt = now.atZone(zoneId)
    return if (zdt.hour < cutoffHour) {
      zdt.toLocalDate().minusDays(1)
    } else {
      zdt.toLocalDate()
    }
  }

  /**
   * Calculates the due instant for an interval in days, anchored at [cutoffHour]:00 in [zoneId].
   */
  fun calculateDueDate(
    now: Instant,
    intervalDays: Double,
    cutoffHour: Int,
    zoneId: ZoneId
  ): Instant {
    val learningDate = getLearningDate(now, cutoffHour, zoneId)
    val daysToAdd = Math.max(1L, Math.round(intervalDays))
    val targetDate = learningDate.plusDays(daysToAdd)
    return targetDate.atTime(cutoffHour, 0).atZone(zoneId).toInstant()
  }
}
