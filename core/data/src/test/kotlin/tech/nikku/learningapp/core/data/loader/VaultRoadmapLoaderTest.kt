package tech.nikku.learningapp.core.data.loader

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VaultRoadmapLoaderTest {

  @Test
  fun loadFromString_parsesAllModules() {
    val sample = """
      [
        {
          "id": "vault:cs:algo",
          "title": "Algorithms",
          "badge": "Next up",
          "description": "Trees and graphs"
        }
      ]
    """.trimIndent()

    val modules = VaultRoadmapLoader.loadFromString(sample)
    assertEquals(1, modules.size)
    assertEquals("vault:cs:algo", modules[0].id)
    assertEquals("Algorithms", modules[0].title)
    assertEquals("Next up", modules[0].badge)
  }

  @Test
  fun loadFromAssets_loads8RoadmapModules() {
    val context: Context = ApplicationProvider.getApplicationContext()
    val modules = VaultRoadmapLoader.loadFromAssets(context)
    assertEquals(8, modules.size)

    val algo = modules.find { it.id == "vault:cs:algo" }
    assertNotNull(algo)
    assertEquals("Next up", algo?.badge)

    val badges = modules.map { it.badge }.toSet()
    assertTrue(badges.contains("Planned"))
    assertTrue(badges.contains("Drafting"))
    assertTrue(badges.contains("Next up"))
  }
}
