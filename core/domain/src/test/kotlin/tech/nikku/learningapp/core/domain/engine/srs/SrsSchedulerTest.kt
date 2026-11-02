package tech.nikku.learningapp.core.domain.engine.srs

import org.junit.Assert.*
import org.junit.Test
import tech.nikku.learningapp.core.domain.model.ReviewGrade
import tech.nikku.learningapp.core.domain.model.ReviewLog
import tech.nikku.learningapp.core.domain.model.SrsState
import tech.nikku.learningapp.core.domain.usecase.ReplayProgressUseCase
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

class SrsSchedulerTest {

  private val cfg = SrsConfig(
    learningStepsMinutes = listOf(1, 10),
    graduatingIntervalDays = 1.0,
    easyGraduatingIntervalDays = 4.0,
    minimumEaseFactor = 1.3,
    maximumIntervalDays = 36500.0,
    easyBonus = 1.3,
    hardIntervalFactor = 1.2,
    lapseNewIntervalFactor = 0.5,
    relearningStepsMinutes = listOf(10),
    fuzzFactor = 0.05,
    learningDayCutoffHour = 4,
    zoneId = ZoneId.of("UTC")
  )

  @Test
  fun newCard_goodThenGood_graduatesToOneDay() {
    val initial = SrsSnapshot.initial("card_new")
    val t0 = Instant.parse("2026-11-02T10:00:00Z")

    // Step 0 -> GOOD
    val step1 = SrsScheduler.schedule(initial, ReviewGrade.GOOD, t0, cfg)
    assertEquals(SrsState.LEARNING, step1.state)
    assertEquals(1, step1.learningStep)
    assertEquals(0.0, step1.intervalDays, 0.001)
    assertEquals(t0.plusSeconds(600), step1.dueAt) // 10 minutes later

    // Step 1 -> GOOD graduates to 1 d
    val t1 = t0.plusSeconds(600)
    val graduated = SrsScheduler.schedule(step1, ReviewGrade.GOOD, t1, cfg)
    assertEquals(SrsState.REVIEW, graduated.state)
    assertEquals(0, graduated.learningStep)
    assertEquals(1.0, graduated.intervalDays, 0.001)
    assertEquals(1, graduated.repetitions)

    // Due at 04:00 on the following day (2026-11-03T04:00:00Z)
    val expectedDue = Instant.parse("2026-11-03T04:00:00Z")
    assertEquals(expectedDue, graduated.dueAt)
  }

  @Test
  fun review_again_lapsesAndFloorsEaseAt1_3() {
    val initialReview = SrsSnapshot(
      itemKey = "card_review",
      state = SrsState.REVIEW,
      intervalDays = 10.0,
      easeFactor = 1.4,
      repetitions = 4,
      lapses = 0,
      dueAt = Instant.parse("2026-11-02T04:00:00Z")
    )

    val now = Instant.parse("2026-11-02T08:00:00Z")
    val lapsed = SrsScheduler.schedule(initialReview, ReviewGrade.AGAIN, now, cfg)

    assertEquals(SrsState.RELEARNING, lapsed.state)
    assertEquals(1, lapsed.lapses)
    assertEquals(0, lapsed.repetitions)
    // 1.4 - 0.20 = 1.2, floored at 1.3
    assertEquals(1.3, lapsed.easeFactor, 0.001)
    // 10.0 * 0.5 = 5.0
    assertEquals(5.0, lapsed.intervalDays, 0.001)
    // Due after 10 min relearning step
    assertEquals(now.plusSeconds(600), lapsed.dueAt)

    // Another lapse at 1.3 remains 1.3
    val secondLapse = SrsScheduler.schedule(lapsed.copy(state = SrsState.REVIEW), ReviewGrade.AGAIN, now.plusSeconds(1000), cfg)
    assertEquals(1.3, secondLapse.easeFactor, 0.001)
    assertEquals(2, secondLapse.lapses)
  }

  @Test
  fun review_easyBonus_scalesIntervalAndEase() {
    val reviewCard = SrsSnapshot(
      itemKey = "card_easy",
      state = SrsState.REVIEW,
      intervalDays = 10.0,
      easeFactor = 2.5,
      repetitions = 3,
      dueAt = Instant.parse("2026-11-02T04:00:00Z")
    )

    val now = Instant.parse("2026-11-02T04:00:00Z") // zero days late
    val seed = 12345L

    val goodResult = SrsScheduler.schedule(reviewCard, ReviewGrade.GOOD, now, cfg, seed)
    val easyResult = SrsScheduler.schedule(reviewCard, ReviewGrade.EASY, now, cfg, seed)

    // Ease factor: GOOD unchanged (2.5), EASY + 0.15 (2.65)
    assertEquals(2.5, goodResult.easeFactor, 0.001)
    assertEquals(2.65, easyResult.easeFactor, 0.001)

    // EASY interval must be significantly larger than GOOD interval due to easyBonus (1.3)
    assertTrue(
      "EASY interval (${easyResult.intervalDays}) should be > GOOD interval * 1.25 (${goodResult.intervalDays * 1.25})",
      easyResult.intervalDays > goodResult.intervalDays * 1.25
    )
  }

  @Test
  fun interval_cappedAtMaximum() {
    val hugeCard = SrsSnapshot(
      itemKey = "card_huge",
      state = SrsState.REVIEW,
      intervalDays = 30000.0,
      easeFactor = 2.5,
      repetitions = 10,
      dueAt = Instant.parse("2026-11-02T04:00:00Z")
    )

    val now = Instant.parse("2026-11-02T04:00:00Z")
    val result = SrsScheduler.schedule(hugeCard, ReviewGrade.EASY, now, cfg)

    assertTrue(result.intervalDays <= cfg.maximumIntervalDays)
    assertEquals(cfg.maximumIntervalDays, result.intervalDays, 0.001)
  }

  @Test
  fun determinism_sameInputsProduceIdenticalOutputs1000x() {
    val snapshot = SrsSnapshot(
      itemKey = "card_deterministic",
      state = SrsState.REVIEW,
      intervalDays = 14.0,
      easeFactor = 2.4,
      repetitions = 3,
      lapses = 1,
      dueAt = Instant.parse("2026-11-02T04:00:00Z"),
      totalReviews = 5,
      correctReviews = 4
    )

    val now = Instant.parse("2026-11-02T12:30:00Z")
    val seed = 987654321L

    val baseline = SrsScheduler.schedule(snapshot, ReviewGrade.GOOD, now, cfg, seed)

    for (i in 1..1000) {
      val output = SrsScheduler.schedule(snapshot, ReviewGrade.GOOD, now, cfg, seed)
      assertEquals("Run $i must match baseline identically", baseline, output)
    }
  }

  @Test
  fun replayEquivalence_sequentialScheduleEqualsReplayProgress() {
    val itemKey = "card_replay_test"
    val t0 = Instant.parse("2026-11-01T09:00:00Z")

    // Sequence of 7 reviews over 14 days
    val grades = listOf(
      ReviewGrade.GOOD to t0,
      ReviewGrade.GOOD to t0.plusSeconds(600), // graduates to 1 d
      ReviewGrade.GOOD to t0.plusSeconds(86400 * 1), // Day 1
      ReviewGrade.AGAIN to t0.plusSeconds(86400 * 3), // Day 3 lapse
      ReviewGrade.GOOD to t0.plusSeconds(86400 * 3 + 600), // Day 3 graduate back
      ReviewGrade.HARD to t0.plusSeconds(86400 * 6), // Day 6
      ReviewGrade.EASY to t0.plusSeconds(86400 * 12) // Day 12
    )

    var sequentialState = SrsSnapshot.initial(itemKey)
    val logs = mutableListOf<ReviewLog>()

    for ((idx, pair) in grades.withIndex()) {
      val (grade, instant) = pair
      val nextState = SrsScheduler.schedule(
        sequentialState,
        grade,
        instant,
        cfg,
        SrsScheduler.deriveSeed(itemKey, sequentialState.totalReviews)
      )

      logs.add(
        ReviewLog(
          id = "log_$idx",
          itemKey = itemKey,
          grade = grade,
          stateBefore = sequentialState.state,
          stateAfter = nextState.state,
          intervalDays = nextState.intervalDays,
          timestampMs = instant.toEpochMilli()
        )
      )

      sequentialState = nextState
    }

    // Replay from raw logs
    val replayUseCase = ReplayProgressUseCase()
    val replayedState = replayUseCase(itemKey, logs, cfg)

    assertEquals(sequentialState.state, replayedState.state)
    assertEquals(sequentialState.intervalDays, replayedState.intervalDays, 0.001)
    assertEquals(sequentialState.easeFactor, replayedState.easeFactor, 0.001)
    assertEquals(sequentialState.repetitions, replayedState.repetitions)
    assertEquals(sequentialState.lapses, replayedState.lapses)
    assertEquals(sequentialState.totalReviews, replayedState.totalReviews)
    assertEquals(sequentialState.correctReviews, replayedState.correctReviews)
    assertEquals(sequentialState.dueAt, replayedState.dueAt)
  }

  @Test
  fun learningDayCutoff_0400LocalTime() {
    val cutoffHour = 4
    val zoneUtc = ZoneId.of("UTC")

    // Review at 02:30 UTC (before 04:00 cutoff)
    // Counts as previous calendar day, so +1 day interval is due TODAY at 04:00 UTC (1.5 h later)
    val earlyMorning = Instant.parse("2026-11-02T02:30:00Z")
    val dueEarly = LearningDay.calculateDueDate(earlyMorning, 1.0, cutoffHour, zoneUtc)
    assertEquals(Instant.parse("2026-11-02T04:00:00Z"), dueEarly)

    // Review at 05:00 UTC (after 04:00 cutoff)
    // Counts as today, so +1 day interval is due TOMORROW at 04:00 UTC
    val morningAfterCutoff = Instant.parse("2026-11-02T05:00:00Z")
    val dueLater = LearningDay.calculateDueDate(morningAfterCutoff, 1.0, cutoffHour, zoneUtc)
    assertEquals(Instant.parse("2026-11-03T04:00:00Z"), dueLater)
  }
}
