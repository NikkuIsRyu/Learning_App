package tech.nikku.learningapp.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tech.nikku.learningapp.core.data.local.entity.ReviewLogEntity
import tech.nikku.learningapp.core.data.local.relation.DailyActivity

@Dao
interface ReviewLogDao {
  /** IGNORE => idempotent union of logs across devices (G-Set semantics). Returns -1 for duplicates. */
  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertAll(logs: List<ReviewLogEntity>): List<Long>

  @Query("SELECT * FROM review_logs WHERE itemKey = :key ORDER BY reviewedAt ASC, id ASC")
  suspend fun logsFor(key: String): List<ReviewLogEntity>

  @Query("SELECT * FROM review_logs WHERE synced = 0 ORDER BY reviewedAt LIMIT :limit")
  suspend fun unsynced(limit: Int): List<ReviewLogEntity>

  @Query("UPDATE review_logs SET synced = 1 WHERE id IN (:ids)")
  suspend fun markSynced(ids: List<String>) // caller chunks to <= 500

  @Query(
    """
    SELECT ((reviewedAt + :tzOffsetMs) / 86400000) AS epochDay,
           COUNT(*) AS reviews,
           SUM(CASE WHEN wasCorrect = 1 THEN 1 ELSE 0 END) AS correct
    FROM review_logs WHERE reviewedAt >= :sinceMs
    GROUP BY epochDay ORDER BY epochDay
    """
  )
  fun observeDailyActivity(sinceMs: Long, tzOffsetMs: Long): Flow<List<DailyActivity>>
}
