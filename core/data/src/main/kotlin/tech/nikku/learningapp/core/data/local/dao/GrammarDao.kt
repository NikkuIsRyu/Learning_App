package tech.nikku.learningapp.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import tech.nikku.learningapp.core.data.local.entity.GrammarExampleEntity
import tech.nikku.learningapp.core.data.local.entity.GrammarRuleEntity
import tech.nikku.learningapp.core.data.local.relation.GrammarRuleWithExamples
import tech.nikku.learningapp.core.domain.model.Domain

@Dao
interface GrammarDao {
  @Upsert
  suspend fun upsertRules(rules: List<GrammarRuleEntity>)

  @Upsert
  suspend fun upsertExamples(examples: List<GrammarExampleEntity>)

  @Query(
    """
    SELECT * FROM grammar_rules
    WHERE domain = :domain
      AND (:chapter IS NULL OR chapter = :chapter)
      AND (:category IS NULL OR category = :category)
      AND (:query IS NULL OR titleEn LIKE '%' || :query || '%' OR titleDe LIKE '%' || :query || '%' OR pattern LIKE '%' || :query || '%')
    ORDER BY chapter, sortOrder
    """
  )
  fun observeRules(domain: Domain, chapter: Int?, category: String?, query: String?): Flow<List<GrammarRuleEntity>>

  @Transaction
  @Query("SELECT * FROM grammar_rules WHERE id = :id")
  fun observeRuleWithExamples(id: String): Flow<GrammarRuleWithExamples?>

  @Query("DELETE FROM grammar_rules WHERE contentVersion < :version")
  suspend fun deleteStale(version: Int): Int
}
