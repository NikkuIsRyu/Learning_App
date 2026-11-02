package tech.nikku.learningapp.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SettingsRepositoryTest {

  @get:Rule
  val tempFolder = TemporaryFolder()

  private val testDispatcher = UnconfinedTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  private lateinit var testDataStore: DataStore<Preferences>
  private lateinit var repository: SettingsRepositoryImpl

  @Before
  fun setup() {
    val context: Context = ApplicationProvider.getApplicationContext()
    val testFile = File(tempFolder.root, "test_settings.preferences_pb")
    testDataStore = PreferenceDataStoreFactory.create(
      scope = testScope,
      produceFile = { testFile }
    )
    repository = SettingsRepositoryImpl(testDataStore)
  }

  @Test
  fun defaultValuesAreCorrect() = testScope.runTest {
    assertTrue(repository.observeToneColorsEnabled().first())
    assertEquals(SettingsRepositoryImpl.DEFAULT_FURIGANA_MODE, repository.observeFuriganaMode().first())
    assertNull(repository.observeDarkTheme().first())
    assertEquals("en", repository.observeUiLanguage().first())
    assertEquals(15, repository.observeDailyNewLimit().first())
    assertEquals((26..32).toSet(), repository.observeActiveChapters().first())
    assertEquals(1.0f, repository.observePlaybackSpeed().first(), 0.001f)
    assertEquals(0, repository.observeContentVersion().first())
    assertEquals(0L, repository.observeResetAt().first())
  }

  @Test
  fun updatingSettingsPersistsValues() = testScope.runTest {
    repository.setToneColorsEnabled(false)
    assertFalse(repository.observeToneColorsEnabled().first())

    repository.setFuriganaMode("KANJI_ONLY")
    assertEquals("KANJI_ONLY", repository.observeFuriganaMode().first())

    repository.setDarkTheme(true)
    assertEquals(true, repository.observeDarkTheme().first())

    repository.setDarkTheme(false)
    assertEquals(false, repository.observeDarkTheme().first())

    repository.setDarkTheme(null)
    assertNull(repository.observeDarkTheme().first())

    repository.setUiLanguage("de")
    assertEquals("de", repository.observeUiLanguage().first())

    repository.setDailyNewLimit(30)
    assertEquals(30, repository.observeDailyNewLimit().first())

    repository.setActiveChapters(setOf(27, 28, 29))
    assertEquals(setOf(27, 28, 29), repository.observeActiveChapters().first())

    repository.setPlaybackSpeed(1.25f)
    assertEquals(1.25f, repository.observePlaybackSpeed().first(), 0.001f)

    repository.setContentVersion(3)
    assertEquals(3, repository.observeContentVersion().first())

    repository.setResetAt(1700000000000L)
    assertEquals(1700000000000L, repository.observeResetAt().first())
  }
}
