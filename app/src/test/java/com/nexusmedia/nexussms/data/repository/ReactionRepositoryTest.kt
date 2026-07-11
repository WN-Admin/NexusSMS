package com.nexusmedia.nexussms.data.repository

import com.nexusmedia.nexussms.data.database.ReactionDao
import com.nexusmedia.nexussms.data.models.Reaction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ReactionRepositoryTest {
    private val reactionDao = mockk<ReactionDao>()
    private lateinit var repository: ReactionRepository

    @Before
    fun setup() {
        repository = ReactionRepository(reactionDao)
    }

    @Test
    fun testInsertReaction() = runTest {
        val reaction = Reaction(
            messageId = "m1",
            emoji = "\u2764\uFE0F",
            senderPhoneNumber = "+1234567890"
        )
        coEvery { reactionDao.insertReaction(reaction) } returns Unit

        repository.insertReaction(reaction)

        coVerify { reactionDao.insertReaction(reaction) }
    }

    @Test
    fun testDeleteReaction() = runTest {
        val reaction = Reaction(
            messageId = "m1",
            emoji = "\uD83D\uDC4D",
            senderPhoneNumber = "+1234567890"
        )
        coEvery { reactionDao.deleteReaction(reaction) } returns Unit

        repository.deleteReaction(reaction)

        coVerify { reactionDao.deleteReaction(reaction) }
    }

    @Test
    fun testGetReactionsByMessage() = runTest {
        val reactions = listOf(
            Reaction(messageId = "m1", emoji = "\u2764\uFE0F", senderPhoneNumber = "+11111"),
            Reaction(messageId = "m1", emoji = "\uD83D\uDE02", senderPhoneNumber = "+22222")
        )
        coEvery { reactionDao.getReactionsByMessage("m1") } returns flowOf(reactions)

        val result = repository.getReactionsByMessage("m1")

        assertEquals(2, result.first().size)
        assertTrue(result.first().all { it.messageId == "m1" })
        coVerify { reactionDao.getReactionsByMessage("m1") }
    }
}
