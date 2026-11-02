package tech.nikku.learningapp.core.domain.engine.audio

import org.junit.Assert.assertEquals
import org.junit.Test
import tech.nikku.learningapp.core.domain.model.DialogueTurn

class TurnLocatorTest {

  private val turns = listOf(
    DialogueTurn(
      dialogueId = "d1",
      idx = 0,
      speaker = "A",
      text = "Turn 0",
      startMs = 1000L,
      endMs = 3500L
    ),
    DialogueTurn(
      dialogueId = "d1",
      idx = 1,
      speaker = "B",
      text = "Turn 1",
      startMs = 4000L,
      endMs = 7000L
    ),
    DialogueTurn(
      dialogueId = "d1",
      idx = 2,
      speaker = "A",
      text = "Turn 2",
      startMs = 7500L,
      endMs = 10000L
    )
  )

  @Test
  fun locateTurnIndex_withinBounds_returnsCorrectIndex() {
    assertEquals(0, TurnLocator.locateTurnIndex(turns, 2000L))
    assertEquals(1, TurnLocator.locateTurnIndex(turns, 5500L))
    assertEquals(2, TurnLocator.locateTurnIndex(turns, 8000L))
  }

  @Test
  fun locateTurnIndex_atStartBoundary_returnsMatchingTurn() {
    assertEquals(0, TurnLocator.locateTurnIndex(turns, 1000L))
    assertEquals(1, TurnLocator.locateTurnIndex(turns, 4000L))
    assertEquals(2, TurnLocator.locateTurnIndex(turns, 7500L))
  }

  @Test
  fun locateTurnIndex_atEndBoundaryMinusOne_returnsMatchingTurn() {
    assertEquals(0, TurnLocator.locateTurnIndex(turns, 3499L))
    assertEquals(1, TurnLocator.locateTurnIndex(turns, 6999L))
    assertEquals(2, TurnLocator.locateTurnIndex(turns, 9999L))
  }

  @Test
  fun locateTurnIndex_inGapBetweenTurns_returnsNegativeOne() {
    // Gap between Turn 0 (ends at 3500) and Turn 1 (starts at 4000)
    assertEquals(-1, TurnLocator.locateTurnIndex(turns, 3500L))
    assertEquals(-1, TurnLocator.locateTurnIndex(turns, 3750L))
    assertEquals(-1, TurnLocator.locateTurnIndex(turns, 3999L))

    // Gap between Turn 1 (ends at 7000) and Turn 2 (starts at 7500)
    assertEquals(-1, TurnLocator.locateTurnIndex(turns, 7000L))
    assertEquals(-1, TurnLocator.locateTurnIndex(turns, 7250L))
    assertEquals(-1, TurnLocator.locateTurnIndex(turns, 7499L))
  }

  @Test
  fun locateTurnIndex_outsideOverallRange_returnsNegativeOne() {
    // Before first turn
    assertEquals(-1, TurnLocator.locateTurnIndex(turns, 0L))
    assertEquals(-1, TurnLocator.locateTurnIndex(turns, 999L))

    // After last turn
    assertEquals(-1, TurnLocator.locateTurnIndex(turns, 10000L))
    assertEquals(-1, TurnLocator.locateTurnIndex(turns, 15000L))

    // Negative position
    assertEquals(-1, TurnLocator.locateTurnIndex(turns, -500L))
  }

  @Test
  fun locateTurnIndex_emptyList_returnsNegativeOne() {
    assertEquals(-1, TurnLocator.locateTurnIndex(emptyList(), 1500L))
  }

  @Test
  fun locateTurnIndex_missingTimestamps_returnsNegativeOne() {
    val untimedTurns = listOf(
      DialogueTurn(dialogueId = "d1", idx = 0, speaker = "A", text = "Untimed", startMs = null, endMs = null)
    )
    assertEquals(-1, TurnLocator.locateTurnIndex(untimedTurns, 1500L))
  }
}
