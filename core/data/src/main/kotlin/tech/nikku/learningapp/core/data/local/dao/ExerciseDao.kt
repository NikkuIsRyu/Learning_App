package tech.nikku.learningapp.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import tech.nikku.learningapp.core.data.local.entity.ExerciseEntity
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.ExerciseType

@Dao
interface ExerciseDao {
  @Upsert
  suspend fun upsertAll(exercises: List<ExerciseEntity>)

  @Query("SELECT * FROM exercises WHERE domain = :domain AND chapter IN (:chapters) AND type IN (:types) ORDER BY chapter, sortOrder")
  suspend fun forChapters(domain: Domain, chapters: List<Int>, types: List<ExerciseType>): List<ExerciseEntity>

  @Query("SELECT * FROM exercises WHERE id = :id")
  suspend fun get(id: String): ExerciseEntity?

  @Query("DELETE FROM exercises WHERE contentVersion < :version")
  suspend fun deleteStale(version: Int): Int
}
