package tech.nikku.learningapp.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.nikku.learningapp.core.data.local.LearningDatabase
import tech.nikku.learningapp.core.data.local.mapper.toDomain
import tech.nikku.learningapp.core.domain.model.Dialogue
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.repository.DialogueRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DialogueRepositoryImpl @Inject constructor(
  private val db: LearningDatabase
) : DialogueRepository {

  private val dao = db.dialogueDao()

  override fun observeDialogues(domain: Domain): Flow<List<Dialogue>> {
    return dao.observeAll(domain).map { list -> list.map { it.toDomain() } }
  }

  override suspend fun getDialogueById(id: String): Dialogue? {
    return dao.getWithTurns(id)?.toDomain()
  }
}
