package tech.nikku.learningapp.core.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import tech.nikku.learningapp.core.data.local.LearningDatabase
import tech.nikku.learningapp.core.data.local.mapper.toDomain
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.Exercise
import tech.nikku.learningapp.core.domain.model.ExerciseType
import tech.nikku.learningapp.core.domain.repository.ExerciseRepository

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
  private val db: LearningDatabase
) : ExerciseRepository {

  private val exerciseDao = db.exerciseDao()

  override suspend fun getExercisesForChapter(
    domain: Domain,
    chapter: Int,
    type: ExerciseType?
  ): List<Exercise> {
    val types = if (type != null) listOf(type) else ExerciseType.entries
    return exerciseDao.forChapters(domain, listOf(chapter), types).map { it.toDomain() }
  }

  override suspend fun getExerciseById(id: String): Exercise? {
    return exerciseDao.get(id)?.toDomain()
  }
}
