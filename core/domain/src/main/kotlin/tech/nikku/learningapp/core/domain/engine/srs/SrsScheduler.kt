package tech.nikku.learningapp.core.domain.engine.srs

import tech.nikku.learningapp.core.domain.model.ReviewGrade
import tech.nikku.learningapp.core.domain.model.SrsState
import java.time.Duration
import java.time.Instant

object SrsScheduler {

  fun deriveSeed(itemKey: String, totalReviews: Int): Long {
    var h = 1125899906842597L
    for (c in itemKey) {
      h = 31L * h + c.code.toLong()
    }
    return h xor (totalReviews.toLong() * 0x5deece66dL)
  }

  fun deterministicFuzz(interval: Double, seed: Long, fuzzFactor: Double): Double {
    if (interval < 2.0 || fuzzFactor <= 0.0) return interval
    var z = seed + -0x61c8864680b583ebL
    z = (z xor (z ushr 30)) * -0x5b350417933183bL
    z = (z xor (z ushr 27)) * -0x6b2fb644c867e2b5L
    val mixed = z xor (z ushr 31)
    val unit = (mixed and 0x7FFFFFFFFFFFFFFFL).toDouble() / Long.MAX_VALUE.toDouble() * 2.0 - 1.0
    val delta = interval * fuzzFactor * unit
    return interval + delta
  }

  fun schedule(
    state: SrsSnapshot,
    grade: ReviewGrade,
    now: Instant,
    cfg: SrsConfig = SrsConfig(),
    seed: Long = deriveSeed(state.itemKey, state.totalReviews)
  ): SrsSnapshot {
    val totalReviews = state.totalReviews + 1
    val isCorrect = grade != ReviewGrade.AGAIN
    val correctReviews = if (isCorrect) state.correctReviews + 1 else state.correctReviews

    return when (state.state) {
      SrsState.NEW, SrsState.LEARNING -> {
        handleLearning(state, grade, now, cfg, totalReviews, correctReviews)
      }
      SrsState.RELEARNING -> {
        handleRelearning(state, grade, now, cfg, totalReviews, correctReviews)
      }
      SrsState.REVIEW -> {
        handleReview(state, grade, now, cfg, seed, totalReviews, correctReviews)
      }
      SrsState.SUSPENDED -> {
        state.copy(totalReviews = totalReviews, lastReviewedAt = now)
      }
    }
  }

  private fun handleLearning(
    state: SrsSnapshot,
    grade: ReviewGrade,
    now: Instant,
    cfg: SrsConfig,
    totalReviews: Int,
    correctReviews: Int
  ): SrsSnapshot {
    val steps = cfg.learningStepsMinutes
    return when (grade) {
      ReviewGrade.AGAIN -> {
        val stepMinutes = steps.getOrElse(0) { 1 }
        state.copy(
          state = SrsState.LEARNING,
          learningStep = 0,
          dueAt = now.plusSeconds(stepMinutes * 60L),
          intervalDays = 0.0,
          totalReviews = totalReviews,
          correctReviews = correctReviews,
          lastReviewedAt = now
        )
      }
      ReviewGrade.HARD -> {
        val stepMinutes = steps.getOrElse(state.learningStep) { 1 }
        state.copy(
          state = SrsState.LEARNING,
          dueAt = now.plusSeconds(stepMinutes * 60L),
          totalReviews = totalReviews,
          correctReviews = correctReviews,
          lastReviewedAt = now
        )
      }
      ReviewGrade.GOOD -> {
        val nextStep = state.learningStep + 1
        if (nextStep < steps.size) {
          val stepMinutes = steps[nextStep]
          state.copy(
            state = SrsState.LEARNING,
            learningStep = nextStep,
            dueAt = now.plusSeconds(stepMinutes * 60L),
            intervalDays = 0.0,
            totalReviews = totalReviews,
            correctReviews = correctReviews,
            lastReviewedAt = now
          )
        } else {
          // Graduate to REVIEW
          val interval = cfg.graduatingIntervalDays
          val dueAt = LearningDay.calculateDueDate(now, interval, cfg.learningDayCutoffHour, cfg.zoneId)
          state.copy(
            state = SrsState.REVIEW,
            learningStep = 0,
            intervalDays = interval,
            repetitions = 1,
            dueAt = dueAt,
            totalReviews = totalReviews,
            correctReviews = correctReviews,
            lastReviewedAt = now
          )
        }
      }
      ReviewGrade.EASY -> {
        val interval = cfg.easyGraduatingIntervalDays
        val dueAt = LearningDay.calculateDueDate(now, interval, cfg.learningDayCutoffHour, cfg.zoneId)
        state.copy(
          state = SrsState.REVIEW,
          learningStep = 0,
          intervalDays = interval,
          repetitions = 1,
          dueAt = dueAt,
          totalReviews = totalReviews,
          correctReviews = correctReviews,
          lastReviewedAt = now
        )
      }
    }
  }

  private fun handleRelearning(
    state: SrsSnapshot,
    grade: ReviewGrade,
    now: Instant,
    cfg: SrsConfig,
    totalReviews: Int,
    correctReviews: Int
  ): SrsSnapshot {
    val steps = cfg.relearningStepsMinutes
    return when (grade) {
      ReviewGrade.AGAIN -> {
        val stepMinutes = steps.getOrElse(0) { 10 }
        state.copy(
          learningStep = 0,
          dueAt = now.plusSeconds(stepMinutes * 60L),
          totalReviews = totalReviews,
          correctReviews = correctReviews,
          lastReviewedAt = now
        )
      }
      ReviewGrade.HARD -> {
        val stepMinutes = steps.getOrElse(state.learningStep) { 10 }
        state.copy(
          dueAt = now.plusSeconds(stepMinutes * 60L),
          totalReviews = totalReviews,
          correctReviews = correctReviews,
          lastReviewedAt = now
        )
      }
      ReviewGrade.GOOD, ReviewGrade.EASY -> {
        val interval = Math.max(1.0, state.intervalDays)
        val dueAt = LearningDay.calculateDueDate(now, interval, cfg.learningDayCutoffHour, cfg.zoneId)
        state.copy(
          state = SrsState.REVIEW,
          learningStep = 0,
          intervalDays = interval,
          repetitions = 1,
          dueAt = dueAt,
          totalReviews = totalReviews,
          correctReviews = correctReviews,
          lastReviewedAt = now
        )
      }
    }
  }

  private fun handleReview(
    state: SrsSnapshot,
    grade: ReviewGrade,
    now: Instant,
    cfg: SrsConfig,
    seed: Long,
    totalReviews: Int,
    correctReviews: Int
  ): SrsSnapshot {
    val diffSeconds = Duration.between(state.dueAt, now).seconds
    val daysLate = Math.max(0.0, diffSeconds.toDouble() / 86400.0)

    val currentInterval = state.intervalDays
    val currentEase = state.easeFactor

    return when (grade) {
      ReviewGrade.AGAIN -> {
        val newEase = Math.max(cfg.minimumEaseFactor, currentEase - 0.20)
        val newInterval = Math.max(1.0, currentInterval * cfg.lapseNewIntervalFactor)
        val relearnStepMinutes = cfg.relearningStepsMinutes.getOrElse(0) { 10 }
        state.copy(
          state = SrsState.RELEARNING,
          intervalDays = newInterval,
          easeFactor = newEase,
          repetitions = 0,
          lapses = state.lapses + 1,
          learningStep = 0,
          dueAt = now.plusSeconds(relearnStepMinutes * 60L),
          totalReviews = totalReviews,
          correctReviews = correctReviews,
          lastReviewedAt = now
        )
      }
      ReviewGrade.HARD -> {
        val newEase = Math.max(cfg.minimumEaseFactor, currentEase - 0.15)
        var newInterval = Math.max(currentInterval + 1.0, currentInterval * cfg.hardIntervalFactor)
        newInterval = Math.min(cfg.maximumIntervalDays, deterministicFuzz(newInterval, seed, cfg.fuzzFactor))
        val dueAt = LearningDay.calculateDueDate(now, newInterval, cfg.learningDayCutoffHour, cfg.zoneId)
        state.copy(
          state = SrsState.REVIEW,
          intervalDays = newInterval,
          easeFactor = newEase,
          repetitions = state.repetitions + 1,
          dueAt = dueAt,
          totalReviews = totalReviews,
          correctReviews = correctReviews,
          lastReviewedAt = now
        )
      }
      ReviewGrade.GOOD -> {
        var newInterval = Math.max(currentInterval + 1.0, (currentInterval + daysLate / 2.0) * currentEase)
        newInterval = Math.min(cfg.maximumIntervalDays, deterministicFuzz(newInterval, seed, cfg.fuzzFactor))
        val dueAt = LearningDay.calculateDueDate(now, newInterval, cfg.learningDayCutoffHour, cfg.zoneId)
        state.copy(
          state = SrsState.REVIEW,
          intervalDays = newInterval,
          easeFactor = currentEase,
          repetitions = state.repetitions + 1,
          dueAt = dueAt,
          totalReviews = totalReviews,
          correctReviews = correctReviews,
          lastReviewedAt = now
        )
      }
      ReviewGrade.EASY -> {
        val newEase = currentEase + 0.15
        var newInterval = (currentInterval + daysLate) * currentEase * cfg.easyBonus
        newInterval = Math.min(cfg.maximumIntervalDays, deterministicFuzz(newInterval, seed, cfg.fuzzFactor))
        val dueAt = LearningDay.calculateDueDate(now, newInterval, cfg.learningDayCutoffHour, cfg.zoneId)
        state.copy(
          state = SrsState.REVIEW,
          intervalDays = newInterval,
          easeFactor = newEase,
          repetitions = state.repetitions + 1,
          dueAt = dueAt,
          totalReviews = totalReviews,
          correctReviews = correctReviews,
          lastReviewedAt = now
        )
      }
    }
  }
}
