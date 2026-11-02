package tech.nikku.learningapp.core.data.local

import androidx.sqlite.db.SimpleSQLiteQuery
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.JlptLevel
import tech.nikku.learningapp.core.domain.model.PartOfSpeech
import tech.nikku.learningapp.core.domain.model.Transitivity
import tech.nikku.learningapp.core.domain.model.VerbGroup

data class LexiconFilter(
  val query: String = "",
  val domain: Domain = Domain.JAPANESE,
  val chapters: Set<Int> = emptySet(),
  val pos: Set<PartOfSpeech> = emptySet(),
  val verbGroups: Set<VerbGroup> = emptySet(),
  val transitivity: Set<Transitivity> = emptySet(),
  val jlpt: Set<JlptLevel> = emptySet(),
  val verifiedOnly: Boolean = false
)

object LexiconQueryBuilder {
  fun build(f: LexiconFilter): SimpleSQLiteQuery {
    val where = mutableListOf("w.domain = ?")
    val args = mutableListOf<Any>(f.domain.name)

    fun <T> inClause(column: String, values: Collection<T>, map: (T) -> Any) {
      if (values.isEmpty()) return
      where += "$column IN (${values.joinToString(",") { "?" }})"
      args.addAll(values.map(map))
    }

    inClause("w.chapter", f.chapters) { it }
    inClause("w.pos", f.pos) { it.name }
    inClause("w.verbGroup", f.verbGroups) { it.name }
    inClause("w.transitivity", f.transitivity) { it.name }
    inClause("w.jlpt", f.jlpt) { it.name }
    if (f.verifiedOnly) where += "w.verified = 1"

    var join = ""
    val orderArgs = mutableListOf<Any>()
    var order = "w.chapter IS NULL, w.chapter, w.orderInChapter, w.reading"
    val q = f.query.trim()
    if (q.isNotEmpty()) {
      if (q.all { it.code < 0x2E80 }) { // Latin / pinyin / romaji
        join = "JOIN words_fts ON words_fts.rowid = w.rowid"
        where += "words_fts MATCH ?"
        args += q.toFtsPrefixQuery()
      } else { // kana / kanji / hanzi
        val like = "%" + q.escapeLike() + "%"
        where += "(w.headword LIKE ? ESCAPE '\\' OR w.reading LIKE ? ESCAPE '\\')"
        args += like
        args += like
        order = "(w.reading LIKE ? ESCAPE '\\') DESC, $order" // prefix matches first
        orderArgs += q.escapeLike() + "%"
      }
    }
    val sql = "SELECT w.* FROM words w $join WHERE ${where.joinToString(" AND ")} ORDER BY $order"
    return SimpleSQLiteQuery(sql, (args + orderArgs).toTypedArray())
  }

  private fun String.escapeLike() = replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

  /** "to eat" -> `to* eat*` ; strips FTS syntax characters. */
  private fun String.toFtsPrefixQuery() =
    split(Regex("\\s+")).map { it.replace(Regex("[\"*^():\\-]"), "") }.filter { it.isNotBlank() }.joinToString(" ") { "$it*" }
}
