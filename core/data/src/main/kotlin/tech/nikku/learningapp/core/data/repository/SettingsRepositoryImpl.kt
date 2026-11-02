package tech.nikku.learningapp.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.nikku.learningapp.core.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
  private val dataStore: DataStore<Preferences>
) : SettingsRepository {

  companion object {
    val KEY_TONE_COLORS = booleanPreferencesKey("tone_colors_enabled")
    val KEY_FURIGANA_MODE = stringPreferencesKey("furigana_mode")
    val KEY_DARK_THEME = stringPreferencesKey("dark_theme")
    val KEY_UI_LANGUAGE = stringPreferencesKey("ui_language")
    val KEY_DAILY_NEW_LIMIT = intPreferencesKey("daily_new_limit")
    val KEY_ACTIVE_CHAPTERS = stringSetPreferencesKey("active_chapters")
    val KEY_PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
    val KEY_CONTENT_VERSION = intPreferencesKey("content_version")
    val KEY_RESET_AT = longPreferencesKey("reset_at")

    const val DEFAULT_FURIGANA_MODE = "FURIGANA"
    const val DEFAULT_UI_LANGUAGE = "en"
    const val DEFAULT_DAILY_NEW_LIMIT = 15
    val DEFAULT_ACTIVE_CHAPTERS: Set<Int> = (26..32).toSet()
    const val DEFAULT_PLAYBACK_SPEED = 1.0f
    const val DEFAULT_CONTENT_VERSION = 0
    const val DEFAULT_RESET_AT = 0L
  }

  override fun observeToneColorsEnabled(): Flow<Boolean> {
    return dataStore.data.map { preferences ->
      preferences[KEY_TONE_COLORS] ?: true
    }
  }

  override suspend fun setToneColorsEnabled(enabled: Boolean) {
    dataStore.edit { preferences ->
      preferences[KEY_TONE_COLORS] = enabled
    }
  }

  override fun observeFuriganaMode(): Flow<String> {
    return dataStore.data.map { preferences ->
      preferences[KEY_FURIGANA_MODE] ?: DEFAULT_FURIGANA_MODE
    }
  }

  override suspend fun setFuriganaMode(mode: String) {
    dataStore.edit { preferences ->
      preferences[KEY_FURIGANA_MODE] = mode
    }
  }

  override fun observeDarkTheme(): Flow<Boolean?> {
    return dataStore.data.map { preferences ->
      when (preferences[KEY_DARK_THEME]) {
        "DARK" -> true
        "LIGHT" -> false
        else -> null
      }
    }
  }

  override suspend fun setDarkTheme(darkTheme: Boolean?) {
    dataStore.edit { preferences ->
      if (darkTheme == null) {
        preferences.remove(KEY_DARK_THEME)
      } else {
        preferences[KEY_DARK_THEME] = if (darkTheme) "DARK" else "LIGHT"
      }
    }
  }

  override fun observeUiLanguage(): Flow<String> {
    return dataStore.data.map { preferences ->
      preferences[KEY_UI_LANGUAGE] ?: DEFAULT_UI_LANGUAGE
    }
  }

  override suspend fun setUiLanguage(language: String) {
    dataStore.edit { preferences ->
      preferences[KEY_UI_LANGUAGE] = language
    }
  }

  override fun observeDailyNewLimit(): Flow<Int> {
    return dataStore.data.map { preferences ->
      preferences[KEY_DAILY_NEW_LIMIT] ?: DEFAULT_DAILY_NEW_LIMIT
    }
  }

  override suspend fun setDailyNewLimit(limit: Int) {
    dataStore.edit { preferences ->
      preferences[KEY_DAILY_NEW_LIMIT] = limit
    }
  }

  override fun observeActiveChapters(): Flow<Set<Int>> {
    return dataStore.data.map { preferences ->
      preferences[KEY_ACTIVE_CHAPTERS]?.mapNotNull { it.toIntOrNull() }?.toSet()
        ?: DEFAULT_ACTIVE_CHAPTERS
    }
  }

  override suspend fun setActiveChapters(chapters: Set<Int>) {
    dataStore.edit { preferences ->
      preferences[KEY_ACTIVE_CHAPTERS] = chapters.map { it.toString() }.toSet()
    }
  }

  override fun observePlaybackSpeed(): Flow<Float> {
    return dataStore.data.map { preferences ->
      preferences[KEY_PLAYBACK_SPEED] ?: DEFAULT_PLAYBACK_SPEED
    }
  }

  override suspend fun setPlaybackSpeed(speed: Float) {
    dataStore.edit { preferences ->
      preferences[KEY_PLAYBACK_SPEED] = speed
    }
  }

  override fun observeContentVersion(): Flow<Int> {
    return dataStore.data.map { preferences ->
      preferences[KEY_CONTENT_VERSION] ?: DEFAULT_CONTENT_VERSION
    }
  }

  override suspend fun setContentVersion(version: Int) {
    dataStore.edit { preferences ->
      preferences[KEY_CONTENT_VERSION] = version
    }
  }

  override fun observeResetAt(): Flow<Long> {
    return dataStore.data.map { preferences ->
      preferences[KEY_RESET_AT] ?: DEFAULT_RESET_AT
    }
  }

  override suspend fun setResetAt(timestamp: Long) {
    dataStore.edit { preferences ->
      preferences[KEY_RESET_AT] = timestamp
    }
  }
}
