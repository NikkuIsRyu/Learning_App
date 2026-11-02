package tech.nikku.learningapp.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import tech.nikku.learningapp.core.data.local.entity.ConfusablePairEntity
import tech.nikku.learningapp.core.domain.model.Domain

@Dao
interface ConfusableDao {
  @Upsert
  suspend fun upsertAll(pairs: List<ConfusablePairEntity>)

  @Query("SELECT * FROM confusable_pairs WHERE domain = :domain ORDER BY chapter, sortOrder")
  fun observeAll(domain: Domain): Flow<List<ConfusablePairEntity>>

  @Query("DELETE FROM confusable_pairs WHERE contentVersion < :version")
  suspend fun deleteStale(version: Int): Int
}
