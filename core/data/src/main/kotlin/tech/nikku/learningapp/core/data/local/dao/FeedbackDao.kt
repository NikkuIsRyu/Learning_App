package tech.nikku.learningapp.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import tech.nikku.learningapp.core.data.local.entity.FeedbackEntity

@Dao
interface FeedbackDao {
  @Insert
  suspend fun insert(f: FeedbackEntity)

  @Query("SELECT * FROM feedback_queue WHERE synced = 0 ORDER BY createdAt LIMIT :limit")
  suspend fun unsynced(limit: Int): List<FeedbackEntity>

  @Query("UPDATE feedback_queue SET synced = 1 WHERE id IN (:ids)")
  suspend fun markSynced(ids: List<String>)
}
