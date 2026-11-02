package tech.nikku.learningapp

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class MainActivitySmokeTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun appStartsAndDisplaysTitle() {
    composeTestRule.onNodeWithText("Learning App").assertExists()
  }
}
