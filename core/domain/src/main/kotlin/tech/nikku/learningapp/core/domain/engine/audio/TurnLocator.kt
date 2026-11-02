package tech.nikku.learningapp.core.domain.engine.audio

import tech.nikku.learningapp.core.domain.model.DialogueTurn

object TurnLocator {

  /**
   * Locates the active turn index for [positionMs] within a sorted list of [DialogueTurn]s using binary search.
   *
   * @param turns List of dialogue turns sorted by [DialogueTurn.startMs] in ascending order.
   * @param positionMs The current playback position in milliseconds.
   * @return The 0-based index of the matching turn in [turns], or -1 if no turn encompasses [positionMs].
   */
  fun locateTurnIndex(turns: List<DialogueTurn>, positionMs: Long): Int {
    if (turns.isEmpty() || positionMs < 0) return -1

    var low = 0
    var high = turns.size - 1

    while (low <= high) {
      val mid = (low + high) ushr 1
      val turn = turns[mid]
      val start = turn.startMs
      val end = turn.endMs

      if (start == null || end == null) {
        return -1
      }

      when {
        positionMs < start -> high = mid - 1
        positionMs >= end -> low = mid + 1
        else -> return mid
      }
    }
    return -1
  }
}
