package tech.nikku.learningapp.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import tech.nikku.learningapp.core.data.local.LexiconFilter
import tech.nikku.learningapp.core.data.local.LexiconQueryBuilder
import tech.nikku.learningapp.core.data.local.dao.WordDao
import tech.nikku.learningapp.core.data.local.mapper.toDomain
import tech.nikku.learningapp.core.domain.model.*
import tech.nikku.learningapp.core.domain.repository.LexiconRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LexiconRepositoryImpl @Inject constructor(
  private val wordDao: WordDao
) : LexiconRepository {

  override fun observeWords(
    domain: Domain,
    chapters: Set<Int>,
    pos: PartOfSpeech?,
    verbGroup: VerbGroup?,
    jlpt: JlptLevel?,
    searchQuery: String
  ): Flow<List<Word>> {
    if (chapters.size == 1 && pos == null && verbGroup == null && jlpt == null && searchQuery.isBlank()) {
      return wordDao.observeChapter(domain, chapters.first()).map { list -> list.map { it.toDomain() } }
    }
    return flow {
      val filter = LexiconFilter(
        query = searchQuery,
        domain = domain,
        chapters = chapters,
        pos = pos?.let { setOf(it) } ?: emptySet(),
        verbGroups = verbGroup?.let { setOf(it) } ?: emptySet(),
        jlpt = jlpt?.let { setOf(it) } ?: emptySet()
      )
      val query = LexiconQueryBuilder.build(filter)
      emit(wordDao.searchList(query).map { it.toDomain() })
    }
  }

  override fun observeChapterCounts(domain: Domain): Flow<Map<Int, Int>> {
    return wordDao.observeChapterCounts(domain).map { list ->
      list.associate { it.chapter to it.count }
    }
  }

  override suspend fun getWordById(id: String): Word? {
    return wordDao.observe(id).first()?.toDomain()
  }

  override suspend fun getWordsForChapter(domain: Domain, chapter: Int): List<Word> {
    return wordDao.observeChapter(domain, chapter).first().map { it.toDomain() }
  }

  override suspend fun searchWords(domain: Domain, query: String, limit: Int): List<Word> {
    val filter = LexiconFilter(
      query = query,
      domain = domain
    )
    val q = LexiconQueryBuilder.build(filter)
    val list = wordDao.searchList(q).map { it.toDomain() }
    return if (limit > 0) list.take(limit) else list
  }
}
