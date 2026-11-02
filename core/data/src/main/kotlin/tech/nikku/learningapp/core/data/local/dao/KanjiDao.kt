package tech.nikku.learningapp.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import tech.nikku.learningapp.core.data.local.entity.KanjiCompoundCrossRef
import tech.nikku.learningapp.core.data.local.entity.KanjiEntity
import tech.nikku.learningapp.core.data.local.relation.KanjiWithCompounds

@Dao
interface KanjiDao {
  @Upsert
  suspend fun upsertAll(kanji: List<KanjiEntity>)

  @Upsert
  suspend fun upsertCompounds(refs: List<KanjiCompoundCrossRef>)

  @Query("SELECT * FROM kanji WHERE introducedChapter = :chapter ORDER BY frequencyRank IS NULL, frequencyRank, literal")
  fun observeForChapter(chapter: Int): Flow<List<KanjiEntity>>

  @Transaction
  @Query("SELECT * FROM kanji WHERE literal = :literal")
  fun observeDetail(literal: String): Flow<KanjiWithCompounds?>

  @Query("DELETE FROM kanji WHERE contentVersion < :version")
  suspend fun deleteStale(version: Int): Int
}
