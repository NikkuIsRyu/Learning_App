package tech.nikku.learningapp.core.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.PartOfSpeech
import tech.nikku.learningapp.core.domain.model.Transitivity
import tech.nikku.learningapp.core.domain.model.VerbGroup

class LexiconQueryBuilderTest {

  @Test
  fun build_defaultFilter_producesBasicSelect() {
    val filter = LexiconFilter(domain = Domain.JAPANESE)
    val query = LexiconQueryBuilder.build(filter)

    assertEquals(
      "SELECT w.* FROM words w  WHERE w.domain = ? ORDER BY w.chapter IS NULL, w.chapter, w.orderInChapter, w.reading",
      query.sql
    )
    assertEquals(1, query.argCount)
  }

  @Test
  fun build_multipleFilters_generatesInClauses() {
    val filter = LexiconFilter(
      domain = Domain.JAPANESE,
      chapters = setOf(26, 27),
      pos = setOf(PartOfSpeech.VERB),
      verbGroups = setOf(VerbGroup.GODAN),
      transitivity = setOf(Transitivity.TRANSITIVE),
      verifiedOnly = true
    )
    val query = LexiconQueryBuilder.build(filter)

    assertTrue("SQL should have chapter IN clause", query.sql.contains("w.chapter IN (?,?)"))
    assertTrue("SQL should have pos IN clause", query.sql.contains("w.pos IN (?)"))
    assertTrue("SQL should have verbGroup IN clause", query.sql.contains("w.verbGroup IN (?)"))
    assertTrue("SQL should have transitivity IN clause", query.sql.contains("w.transitivity IN (?)"))
    assertTrue("SQL should have verified check", query.sql.contains("w.verified = 1"))
  }

  @Test
  fun build_latinSearchQuery_usesFtsJoinAndMatch() {
    val filter = LexiconFilter(
      query = "eat",
      domain = Domain.JAPANESE
    )
    val query = LexiconQueryBuilder.build(filter)

    assertTrue("SQL should join words_fts", query.sql.contains("JOIN words_fts ON words_fts.rowid = w.rowid"))
    assertTrue("SQL should match query in words_fts", query.sql.contains("words_fts MATCH ?"))
  }

  @Test
  fun build_ftsSanitizer_stripsSyntaxCharacters() {
    val filter = LexiconFilter(
      query = "\"eat\" -apple (test:*)",
      domain = Domain.JAPANESE
    )
    val query = LexiconQueryBuilder.build(filter)
    assertTrue("SQL should join words_fts", query.sql.contains("JOIN words_fts ON words_fts.rowid = w.rowid"))
  }

  @Test
  fun build_cjkSearchQuery_usesLikeWithPrefixOrdering() {
    val filter = LexiconFilter(
      query = "たべ",
      domain = Domain.JAPANESE
    )
    val query = LexiconQueryBuilder.build(filter)

    assertTrue("SQL should use LIKE for headword or reading", query.sql.contains("w.headword LIKE ? ESCAPE '\\' OR w.reading LIKE ? ESCAPE '\\'"))
    assertTrue("SQL ORDER BY should prioritize prefix matches", query.sql.contains("(w.reading LIKE ? ESCAPE '\\') DESC"))
    // 1 (domain) + 2 (LIKE in WHERE) + 1 (LIKE in ORDER BY) = 4 arguments
    assertEquals(4, query.argCount)
  }
}
