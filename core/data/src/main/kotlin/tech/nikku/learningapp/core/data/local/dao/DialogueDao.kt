package tech.nikku.learningapp.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import tech.nikku.learningapp.core.data.local.entity.DialogueEntity
import tech.nikku.learningapp.core.data.local.entity.DialogueTurnEntity
import tech.nikku.learningapp.core.data.local.relation.DialogueWithTurns
import tech.nikku.learningapp.core.domain.model.Domain

@Dao
interface DialogueDao {
  @Upsert
  suspend fun upsertDialogues(d: List<DialogueEntity>)

  @Upsert
  suspend fun upsertTurns(t: List<DialogueTurnEntity>)

  @Query("SELECT * FROM dialogues WHERE domain = :domain ORDER BY chapter, sortOrder")
  fun observeAll(domain: Domain): Flow<List<DialogueEntity>>

  @Transaction
  @Query("SELECT * FROM dialogues WHERE id = :id")
  fun observeWithTurns(id: String): Flow<DialogueWithTurns?>

  @Query("DELETE FROM dialogues WHERE contentVersion < :version")
  suspend fun deleteStale(version: Int): Int
}
