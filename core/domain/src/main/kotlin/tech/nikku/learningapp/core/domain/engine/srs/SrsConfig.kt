package tech.nikku.learningapp.core.domain.engine.srs

import java.time.ZoneId

data class SrsConfig(
  val learningStepsMinutes: List<Int> = listOf(1, 10),
  val graduatingIntervalDays: Double = 1.0,
  val easyGraduatingIntervalDays: Double = 4.0,
  val minimumEaseFactor: Double = 1.3,
  val maximumIntervalDays: Double = 36500.0,
  val easyBonus: Double = 1.3,
  val hardIntervalFactor: Double = 1.2,
  val lapseNewIntervalFactor: Double = 0.5,
  val relearningStepsMinutes: List<Int> = listOf(10),
  val fuzzFactor: Double = 0.05,
  val learningDayCutoffHour: Int = 4,
  val zoneId: ZoneId = ZoneId.of("UTC")
)
