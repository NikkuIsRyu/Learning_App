package tech.nikku.learningapp.core.data.repository

import androidx.room.withTransaction
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.nikku.learningapp.core.data.local.LearningDatabase
import tech.nikku.learningapp.core.data.local.entity.ReviewLogEntity
import tech.nikku.learningapp.core.data.local.entity.UserProgressEntity
import tech.nikku.learningapp.core.data.local.mapper.toDomain
import tech.nikku.learningapp.core.domain.engine.srs.SrsScheduler
import tech.nikku.learningapp.core.domain.engine.srs.SrsSnapshot
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.ItemType
import tech.nikku.learningapp.core.domain.model.ReviewGrade
import tech.nikku.learningapp.core.domain.model.ReviewItem
import tech.nikku.learningapp.core.domain.model.SrsState
import tech.nikku.learningapp.core.domain.model.UserProgress
import tech.nikku.learningapp.core.domain.repository.ProgressRepository

@Singleton
class ProgressRepositoryImpl @Inject constructor(
  private val db: LearningDatabase
) : ProgressRepository {

  private val progressDao = db.progressDao()
  private val reviewLogDao = db.reviewLogDao()

  override fun observeDueReviews(domain: Domain?, nowMs: Long): Flow<List<ReviewItem>> {
    return progressDao.observeDueCount(domain ?: Domain.JAPANESE, nowMs).map {
      emptyList()
    }
  }

  override fun observeProgress(itemKey: String): Flow<UserProgress?> {
    return progressDao.observe(itemKey).map { it?.toDomain() }
  }

  override suspend fun getDueCount(domain: Domain?, nowMs: Long): Int {
    val items = progressDao.dueItems(domain ?: Domain.JAPANESE, nowMs, 1000)
    return items.size
  }

  override suspend fun recordReview(
    itemKey: String,
    itemType: ItemType,
    domain: Domain,
    grade: ReviewGrade,
    nowMs: Long,
    reviewDurationMs: Long
  ): UserProgress = db.withTransaction {
    val currentEntity = progressDao.get(itemKey)
    val snapshot = if (currentEntity != null) {
      SrsSnapshot(
        itemKey = currentEntity.itemKey,
        state = currentEntity.state,
        learningStep = currentEntity.learningStep,
        intervalDays = currentEntity.intervalDays,
        easeFactor = currentEntity.easeFactor,
        dueAt = Instant.ofEpochMilli(currentEntity.dueAt),
        lastReviewedAt = currentEntity.lastReviewedAt?.let { Instant.ofEpochMilli(it) },
        lapses = currentEntity.lapses,
        repetitions = currentEntity.repetitions,
        totalReviews = currentEntity.totalReviews,
        correctReviews = currentEntity.correctReviews
      )
    } else {
      SrsSnapshot.initial(itemKey = itemKey, dueAt = Instant.ofEpochMilli(nowMs))
    }

    val nextSnapshot = SrsScheduler.schedule(
      state = snapshot,
      grade = grade,
      now = Instant.ofEpochMilli(nowMs)
    )

    val logId = UUID.randomUUID().toString()
    val wasCorrect = grade != ReviewGrade.AGAIN

    val logEntity = ReviewLogEntity(
      id = logId,
      itemKey = itemKey,
      itemType = itemType,
      domain = domain,
      reviewedAt = nowMs,
      grade = grade,
      responseMs = reviewDurationMs,
      exerciseType = null,
      userAnswer = null,
      wasCorrect = wasCorrect,
      prevState = snapshot.state,
      newState = nextSnapshot.state,
      prevIntervalDays = snapshot.intervalDays,
      newIntervalDays = nextSnapshot.intervalDays,
      prevEase = snapshot.easeFactor,
      newEase = nextSnapshot.easeFactor,
      deviceId = "local",
      synced = false
    )
    reviewLogDao.insertAll(listOf(logEntity))

    val updatedEntity = UserProgressEntity(
      itemKey = itemKey,
      itemType = itemType,
      domain = domain,
      state = nextSnapshot.state,
      easeFactor = nextSnapshot.easeFactor,
      intervalDays = nextSnapshot.intervalDays,
      repetitions = nextSnapshot.repetitions,
      lapses = nextSnapshot.lapses,
      learningStep = nextSnapshot.learningStep,
      dueAt = nextSnapshot.dueAt.toEpochMilli(),
      lastReviewedAt = nowMs,
      totalReviews = (currentEntity?.totalReviews ?: 0) + 1,
      correctReviews = (currentEntity?.correctReviews ?: 0) + (if (wasCorrect) 1 else 0),
      suspended = currentEntity?.suspended ?: false,
      updatedAt = nowMs,
      updatedByDevice = "local",
      dirty = true,
      deleted = false
    )
    progressDao.upsert(updatedEntity)
    updatedEntity.toDomain()
  }
}
