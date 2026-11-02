package tech.nikku.learningapp.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import tech.nikku.learningapp.core.data.local.dao.KanjiDao
import tech.nikku.learningapp.core.data.local.mapper.toDomain
import tech.nikku.learningapp.core.domain.model.Kanji
import tech.nikku.learningapp.core.domain.model.KanjiCompound
import tech.nikku.learningapp.core.domain.repository.KanjiRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KanjiRepositoryImpl @Inject constructor(
  private val kanjiDao: KanjiDao
) : KanjiRepository {

  override fun observeKanjiForChapter(chapter: Int): Flow<List<Kanji>> {
    return kanjiDao.observeForChapter(chapter).map { list ->
      list.map { it.toDomain() }
    }
  }

  override suspend fun getKanjiByLiteral(literal: String): Kanji? {
    val detail = kanjiDao.observeDetail(literal).first() ?: return null
    val compounds = detail.compounds.map { word ->
      KanjiCompound(
        kanjiLiteral = detail.kanji.literal,
        compound = word.headword,
        reading = word.reading,
        rubyMarkup = word.rubyMarkup,
        meaningEn = word.meaningEn,
        meaningDe = word.meaningDe,
        wordId = word.id
      )
    }
    return detail.kanji.toDomain(compounds)
  }

  override suspend fun getAllKanji(): List<Kanji> {
    return kanjiDao.getAll().map { it.toDomain() }
  }
}
