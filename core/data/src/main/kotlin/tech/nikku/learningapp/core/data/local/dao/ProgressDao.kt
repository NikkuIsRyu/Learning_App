package tech.nikku.learningapp.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import tech.nikku.learningapp.core.data.local.entity.UserProgressEntity
import tech.nikku.learningapp.core.data.local.relation.ProgressWithHistory
import tech.nikku.learningapp.core.data.local.relation.StateCount
import tech.nikku.learningapp.core.domain.model.Domain

@Dao
interface ProgressDao {
  @Query("SELECT * FROM user_progress WHERE itemKey = :key")
  suspend fun get(key: String): UserProgressEntity?

  @Query("SELECT * FROM user_progress WHERE itemKey IN (:keys)")
  suspend fun getAll(keys: List<String>): List<UserProgressEntity> // caller chunks to <= 500

  @Query("SELECT * FROM user_progress WHERE itemKey = :key")
  fun observe(key: String): Flow<UserProgressEntity?>

  @Upsert
  suspend fun upsert(p: UserProgressEntity)

  @Upsert
  suspend fun upsertAll(p: List<UserProgressEntity>)

  /** `now` is a snapshot — callers re-subscribe on a ticker (see §7.6). */
  @Query("SELECT COUNT(*) FROM user_progress WHERE domain = :domain AND state IN ('LEARNING','REVIEW','RELEARNING') AND suspended = 0 AND deleted = 0 AND dueAt <= :now")
  fun observeDueCount(domain: Domain, now: Long): Flow<Int>

  @Query("SELECT * FROM user_progress WHERE domain = :domain AND state IN ('LEARNING','REVIEW','RELEARNING') AND suspended = 0 AND deleted = 0 AND dueAt <= :now ORDER BY dueAt LIMIT :limit")
  suspend fun dueItems(domain: Domain, now: Long, limit: Int): List<UserProgressEntity>

  @Query("SELECT itemKey FROM user_progress WHERE itemKey IN (:keys)")
  suspend fun existingKeys(keys: List<String>): List<String>

  @Query("SELECT state, COUNT(*) AS count FROM user_progress WHERE domain = :domain AND deleted = 0 GROUP BY state")
  fun observeStateHistogram(domain: Domain): Flow<List<StateCount>>

  @Query("SELECT * FROM user_progress WHERE dirty = 1 LIMIT :limit")
  suspend fun dirty(limit: Int): List<UserProgressEntity>

  /** Compare-and-set: only clears `dirty` if the row was not modified while the push was in flight. */
  @Query("UPDATE user_progress SET dirty = 0 WHERE itemKey = :key AND updatedAt = :expectedUpdatedAt")
  suspend fun markClean(key: String, expectedUpdatedAt: Long): Int

  @Transaction
  @Query("SELECT * FROM user_progress WHERE itemKey = :key")
  suspend fun withHistory(key: String): ProgressWithHistory?
}
