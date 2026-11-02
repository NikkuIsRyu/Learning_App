package tech.nikku.learningapp.core.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Upsert
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow
import tech.nikku.learningapp.core.data.local.entity.WordEntity
import tech.nikku.learningapp.core.data.local.relation.ChapterCount
import tech.nikku.learningapp.core.data.local.relation.WordWithPair
import tech.nikku.learningapp.core.domain.model.Domain

@Dao
interface WordDao {
  @Upsert
  suspend fun upsertAll(words: List<WordEntity>)

  @Query("SELECT * FROM words WHERE id = :id")
  fun observe(id: String): Flow<WordEntity?>

  @Transaction
  @Query("SELECT * FROM words WHERE id = :id")
  fun observeWithPair(id: String): Flow<WordWithPair?>

  @Query("SELECT * FROM words WHERE domain = :domain AND chapter = :chapter ORDER BY orderInChapter")
  fun observeChapter(domain: Domain, chapter: Int): Flow<List<WordEntity>>

  @Query("SELECT * FROM words WHERE domain = 'JAPANESE' AND pos = 'VERB' AND verbGroup IS NOT NULL AND dictionaryForm IS NOT NULL AND chapter IN (:chapters) ORDER BY chapter, orderInChapter")
  suspend fun conjugatableVerbs(chapters: List<Int>): List<WordEntity>

  @Query("SELECT chapter, COUNT(*) AS count FROM words WHERE domain = :domain AND chapter IS NOT NULL GROUP BY chapter ORDER BY chapter")
  fun observeChapterCounts(domain: Domain): Flow<List<ChapterCount>>

  /** Dynamic lexicon search/filter built by [LexiconQueryBuilder]; paged. */
  @RawQuery(observedEntities = [WordEntity::class])
  fun searchPaged(query: SupportSQLiteQuery): PagingSource<Int, WordEntity>

  @Query("DELETE FROM words WHERE contentVersion < :version AND source != 'USER'")
  suspend fun deleteStale(version: Int): Int

  @Query("SELECT COUNT(*) FROM words WHERE domain = :domain")
  suspend fun count(domain: Domain): Int
}
