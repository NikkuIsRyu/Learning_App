package tech.nikku.learningapp.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import tech.nikku.learningapp.core.data.local.dao.GrammarDao
import tech.nikku.learningapp.core.data.local.mapper.toDomain
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.GrammarRule
import tech.nikku.learningapp.core.domain.repository.GrammarRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GrammarRepositoryImpl @Inject constructor(
  private val grammarDao: GrammarDao
) : GrammarRepository {

  override fun observeRules(domain: Domain, category: String?): Flow<List<GrammarRule>> {
    return grammarDao.observeRules(domain, chapter = null, category = category, query = null).map { list ->
      list.map { it.toDomain() }
    }
  }

  override fun observeRulesForChapter(chapter: Int): Flow<List<GrammarRule>> {
    return grammarDao.observeRules(Domain.JAPANESE, chapter = chapter, category = null, query = null).map { list ->
      list.map { it.toDomain() }
    }
  }

  override suspend fun getRuleById(id: String): GrammarRule? {
    val withExamples = grammarDao.observeRuleWithExamples(id).first() ?: return null
    val examples = withExamples.examples.map { it.toDomain() }
    return withExamples.rule.toDomain(examples)
  }
}
