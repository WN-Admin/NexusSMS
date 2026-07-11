package com.nexusmedia.nexussms.data.repository

import com.nexusmedia.nexussms.data.database.ConversationDao
import com.nexusmedia.nexussms.data.models.Conversation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ConversationRepositoryTest {
    private val conversationDao = mockk<ConversationDao>()
    private lateinit var repository: ConversationRepository

    @Before
    fun setup() {
        repository = ConversationRepository(conversationDao)
    }

    @Test
    fun testInsertConversation() = runTest {
        val conversation = Conversation(
            participantPhoneNumbers = "[\"+1234567890\"]",
            displayName = "Test"
        )
        coEvery { conversationDao.insertConversation(conversation) } returns Unit

        repository.insertConversation(conversation)

        coVerify { conversationDao.insertConversation(conversation) }
    }

    @Test
    fun testGetAllConversations() = runTest {
        val conversations = listOf(
            Conversation(participantPhoneNumbers = "[\"+11111\"]", displayName = "A"),
            Conversation(participantPhoneNumbers = "[\"+22222\"]", displayName = "B")
        )
        coEvery { conversationDao.getAllConversations() } returns flowOf(conversations)

        val result = repository.getAllConversations()

        assertEquals(2, result.first().size)
        coVerify { conversationDao.getAllConversations() }
    }

    @Test
    fun testGetPinnedConversations() = runTest {
        val pinned = listOf(
            Conversation(participantPhoneNumbers = "[\"+11111\"]", displayName = "Pinned", isPinned = true)
        )
        coEvery { conversationDao.getPinnedConversations() } returns flowOf(pinned)

        val result = repository.getPinnedConversations()

        assertTrue(result.first().all { it.isPinned })
        coVerify { conversationDao.getPinnedConversations() }
    }

    @Test
    fun testMarkConversationAsReadUpdatesUnreadCount() = runTest {
        val conversation = Conversation(
            id = "conv1",
            participantPhoneNumbers = "[\"+11111\"]",
            displayName = "Test",
            unreadCount = 5
        )
        coEvery { conversationDao.getConversationById("conv1") } returns conversation
        coEvery { conversationDao.updateConversation(any()) } returns Unit

        repository.markConversationAsRead("conv1")

        coVerify {
            conversationDao.getConversationById("conv1")
            conversationDao.updateConversation(match { it.unreadCount == 0 })
        }
    }

    @Test
    fun testDeleteConversationById() = runTest {
        val conversation = Conversation(
            id = "conv1",
            participantPhoneNumbers = "[\"+11111\"]",
            displayName = "Delete me"
        )
        coEvery { conversationDao.getConversationById("conv1") } returns conversation
        coEvery { conversationDao.deleteConversation(conversation) } returns Unit

        repository.deleteConversationById("conv1")

        coVerify {
            conversationDao.getConversationById("conv1")
            conversationDao.deleteConversation(conversation)
        }
    }

    @Test
    fun testClearUnreadCount() = runTest {
        val conversation = Conversation(
            id = "conv1",
            participantPhoneNumbers = "[\"+11111\"]",
            displayName = "Test",
            unreadCount = 3
        )
        coEvery { conversationDao.getConversationById("conv1") } returns conversation
        coEvery { conversationDao.updateConversation(any()) } returns Unit

        repository.clearUnreadCount("conv1")

        coVerify {
            conversationDao.getConversationById("conv1")
            conversationDao.updateConversation(match { it.unreadCount == 0 })
        }
    }
}
