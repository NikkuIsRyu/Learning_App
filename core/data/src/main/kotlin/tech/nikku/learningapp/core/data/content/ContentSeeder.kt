package tech.nikku.learningapp.core.data.content

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import tech.nikku.learningapp.core.data.content.dto.*
import tech.nikku.learningapp.core.data.local.LearningDatabase
import tech.nikku.learningapp.core.data.local.converter.ContentJson
import tech.nikku.learningapp.core.data.local.entity.*
import tech.nikku.learningapp.core.domain.repository.SettingsRepository
import java.io.InputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SeedResult {
  data class Success(
    val version: Int,
    val wordsCount: Int,
    val kanjiCount: Int,
    val kanjiCompoundsCount: Int,
    val grammarRulesCount: Int,
    val grammarExamplesCount: Int,
    val dialoguesCount: Int,
    val dialogueTurnsCount: Int,
    val confusablePairsCount: Int,
    val exercisesCount: Int
  ) : SeedResult

  data class AlreadyUpToDate(val version: Int) : SeedResult
  data class Error(val message: String, val cause: Throwable? = null) : SeedResult
}

@Singleton
class ContentSeeder @Inject constructor(
  @ApplicationContext private val context: Context,
  private val db: LearningDatabase,
  private val settingsRepository: SettingsRepository
) {

  private val json = ContentJson

  suspend fun seed(
    manifestPath: String = "content/manifest.json",
    force: Boolean = false,
    openAsset: (String) -> InputStream = { path -> context.assets.open(path) }
  ): SeedResult = withContext(Dispatchers.IO) {
    try {
      val manifestBytes = openAsset(manifestPath).use { it.readBytes() }
      val manifestText = manifestBytes.toString(Charsets.UTF_8)
      val manifest = json.decodeFromString<ManifestDto>(manifestText)

      val storedVersion = settingsRepository.observeContentVersion().first()
      if (!force && storedVersion >= manifest.contentVersion) {
        return@withContext SeedResult.AlreadyUpToDate(storedVersion)
      }

      val baseDir = if (manifestPath.contains('/')) {
        manifestPath.substringBeforeLast('/') + "/"
      } else {
        ""
      }

      val words = mutableListOf<WordEntity>()
      val kanji = mutableListOf<KanjiEntity>()
      val kanjiCompounds = mutableListOf<KanjiCompoundCrossRef>()
      val grammarRules = mutableListOf<GrammarRuleEntity>()
      val grammarExamples = mutableListOf<GrammarExampleEntity>()
      val dialogues = mutableListOf<DialogueEntity>()
      val dialogueTurns = mutableListOf<DialogueTurnEntity>()
      val confusablePairs = mutableListOf<ConfusablePairEntity>()
      val exercises = mutableListOf<ExerciseEntity>()

      for (file in manifest.files) {
        val fullPath = baseDir + file.path
        val bytes = openAsset(fullPath).use { it.readBytes() }
        val actualSha256 = computeSha256(bytes)
        if (!actualSha256.equals(file.sha256, ignoreCase = true)) {
          throw IllegalStateException(
            "Checksum mismatch for ${file.path}: expected ${file.sha256}, got $actualSha256"
          )
        }

        val text = bytes.toString(Charsets.UTF_8)
        when (file.entity) {
          "word" -> {
            val dtos = json.decodeFromString<List<WordDto>>(text)
            words.addAll(dtos.map { it.toEntity(manifest.contentVersion) })
          }
          "kanji" -> {
            val dtos = json.decodeFromString<List<KanjiDto>>(text)
            kanji.addAll(dtos.map { it.toEntity(manifest.contentVersion) })
          }
          "kanji_compound" -> {
            val dtos = json.decodeFromString<List<KanjiCompoundDto>>(text)
            kanjiCompounds.addAll(dtos.map { it.toEntity() })
          }
          "grammar_rule" -> {
            val dtos = json.decodeFromString<List<GrammarRuleDto>>(text)
            grammarRules.addAll(dtos.map { it.toEntity(manifest.contentVersion) })
          }
          "grammar_example" -> {
            val dtos = json.decodeFromString<List<GrammarExampleDto>>(text)
            grammarExamples.addAll(dtos.map { it.toEntity() })
          }
          "dialogue" -> {
            val dtos = json.decodeFromString<List<DialogueDto>>(text)
            dialogues.addAll(dtos.map { it.toEntity(manifest.contentVersion) })
          }
          "dialogue_turn" -> {
            val dtos = json.decodeFromString<List<DialogueTurnDto>>(text)
            dialogueTurns.addAll(dtos.map { it.toEntity() })
          }
          "confusable_pair" -> {
            val dtos = json.decodeFromString<List<ConfusablePairDto>>(text)
            confusablePairs.addAll(dtos.map { it.toEntity(manifest.contentVersion) })
          }
          "exercise" -> {
            val dtos = json.decodeFromString<List<ExerciseDto>>(text)
            exercises.addAll(dtos.map { it.toEntity(manifest.contentVersion) })
          }
          "vault_roadmap" -> {
            // Evaluated and loaded separately on demand
          }
          else -> {
            Log.w("ContentSeeder", "Unknown entity type: ${file.entity} in ${file.path}")
          }
        }
      }

      // Execute upserts and prune stale rows inside a single database transaction in FK-safe order
      db.withTransaction {
        // 1. Words
        words.chunked(500).forEach { db.wordDao().upsertAll(it) }
        // 2. Kanji
        kanji.chunked(500).forEach { db.kanjiDao().upsertAll(it) }
        // 3. Kanji compounds (FK -> kanji, words)
        kanjiCompounds.chunked(500).forEach { db.kanjiDao().upsertCompounds(it) }
        // 4. Grammar rules
        grammarRules.chunked(500).forEach { db.grammarDao().upsertRules(it) }
        // 5. Grammar examples (FK -> grammar_rules)
        grammarExamples.chunked(500).forEach { db.grammarDao().upsertExamples(it) }
        // 6. Dialogues
        dialogues.chunked(500).forEach { db.dialogueDao().upsertDialogues(it) }
        // 7. Dialogue turns (FK -> dialogues)
        dialogueTurns.chunked(500).forEach { db.dialogueDao().upsertTurns(it) }
        // 8. Confusable pairs
        confusablePairs.chunked(500).forEach { db.confusableDao().upsertAll(it) }
        // 9. Exercises (FK -> grammar_rules)
        if (exercises.isNotEmpty()) {
          exercises.chunked(500).forEach { db.exerciseDao().upsertAll(it) }
        }

        // Delete stale rows from older versions
        db.wordDao().deleteStale(manifest.contentVersion)
        db.kanjiDao().deleteStale(manifest.contentVersion)
        db.grammarDao().deleteStale(manifest.contentVersion)
        db.dialogueDao().deleteStale(manifest.contentVersion)
        db.confusableDao().deleteStale(manifest.contentVersion)
        db.exerciseDao().deleteStale(manifest.contentVersion)
      }

      settingsRepository.setContentVersion(manifest.contentVersion)

      val logMsg = "words=${words.size}, kanji=${kanji.size}, grammar=${grammarRules.size}, dialogues=${dialogues.size}"
      Log.i("ContentSeeder", logMsg)

      SeedResult.Success(
        version = manifest.contentVersion,
        wordsCount = words.size,
        kanjiCount = kanji.size,
        kanjiCompoundsCount = kanjiCompounds.size,
        grammarRulesCount = grammarRules.size,
        grammarExamplesCount = grammarExamples.size,
        dialoguesCount = dialogues.size,
        dialogueTurnsCount = dialogueTurns.size,
        confusablePairsCount = confusablePairs.size,
        exercisesCount = exercises.size
      )
    } catch (e: Exception) {
      Log.e("ContentSeeder", "Content seeding failed: ${e.message}", e)
      SeedResult.Error(e.message ?: "Unknown seeding error", e)
    }
  }

  companion object {
    fun computeSha256(bytes: ByteArray): String {
      val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
      return digest.joinToString("") { "%02x".format(it) }
    }
  }
}
