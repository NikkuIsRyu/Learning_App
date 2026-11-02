package tech.nikku.learningapp.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.ItemType
import tech.nikku.learningapp.core.domain.model.ReviewItem
import tech.nikku.learningapp.core.domain.model.SrsState
import tech.nikku.learningapp.core.domain.model.UserProgress

class StartReviewSessionUseCaseTest {

  private fun makeItem(id: String, state: SrsState): ReviewItem {
    return ReviewItem(
      progress = UserProgress(
        itemKey = id,
        itemType = ItemType.WORD,
        domain = Domain.JAPANESE,
        state = state
      )
    )
  }

  @Test
  fun interleavesOneNewPerFourReviews() {
    val reviews = (1..10).map { makeItem("r$it", SrsState.REVIEW) }
    val newItems = (1..3).map { makeItem("n$it", SrsState.NEW) }

    val interleaved = StartReviewSessionUseCase.interleave(reviews, newItems)

    val expectedOrder = listOf(
      "r1", "r2", "r3", "r4", "n1",
      "r5", "r6", "r7", "r8", "n2",
      "r9", "r10", "n3"
    )

    assertEquals(expectedOrder, interleaved.map { it.progress.itemKey })
  }

  @Test
  fun interleavesWhenOnlyReviewsExist() {
    val reviews = (1..5).map { makeItem("r$it", SrsState.REVIEW) }
    val interleaved = StartReviewSessionUseCase.interleave(reviews, emptyList())
    assertEquals(listOf("r1", "r2", "r3", "r4", "r5"), interleaved.map { it.progress.itemKey })
  }

  @Test
  fun interleavesWhenOnlyNewItemsExist() {
    val newItems = (1..3).map { makeItem("n$it", SrsState.NEW) }
    val interleaved = StartReviewSessionUseCase.interleave(emptyList(), newItems)
    assertEquals(listOf("n1", "n2", "n3"), interleaved.map { it.progress.itemKey })
  }
}
