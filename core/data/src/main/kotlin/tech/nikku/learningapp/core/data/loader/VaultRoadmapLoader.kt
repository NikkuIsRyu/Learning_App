package tech.nikku.learningapp.core.data.loader

import android.content.Context
import kotlinx.serialization.json.Json
import tech.nikku.learningapp.core.domain.model.VaultModule

object VaultRoadmapLoader {
  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  }

  fun loadFromString(jsonString: String): List<VaultModule> {
    return json.decodeFromString(jsonString)
  }

  fun loadFromAssets(context: Context, path: String = "content/vault/roadmap.json"): List<VaultModule> {
    val content = context.assets.open(path).bufferedReader().use { it.readText() }
    return loadFromString(content)
  }
}
