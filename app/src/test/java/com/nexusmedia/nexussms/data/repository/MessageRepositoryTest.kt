package com.nexusmedia.nexussms.data.repository

import com.nexusmedia.nexussms.data.database.MessageDao
import com.nexusmedia.nexussms.data.models.Message
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MessageRepositoryTest {
    private val messageDao = mockk<MessageDao>()
    private lateinit var repository: MessageRepository

    @Before
    fun setup() {
        repository = MessageRepository(messageDao)
    }

    @Test
    fun testInsertMessage() = runTest {
        val message = Message(
            conversationId = "c1",
            content = "Hello",
            senderPhoneNumber = "p1",
            recipientPhoneNumber = "p2",
            type = "TEXT",
            status = "SENT"
        )
        coEvery { messageDao.insertMessage(message) } returns 1L

        val result = repository.insertMessage(message)

        assertEquals(1L, result)
        coVerify { messageDao.insertMessage(message) }
    }

    @Test
    fun testGetMessageByIdReturnsExpected() = runTest {
        val message = Message(
            id = "m1",
            conversationId = "c1",
            content = "Test",
            senderPhoneNumber = "p1",
            recipientPhoneNumber = "p2",
            type = "TEXT",
            status = "SENT"
        )
        coEvery { messageDao.getMessageById("m1") } returns message

        val result = repository.getMessageById("m1")

        assertNotNull(result)
        assertEquals("m1", result?.id)
        assertEquals("Test", result?.content)
        coVerify { messageDao.getMessageById("m1") }
    }

    @Test
    fun testGetConversationMessagesReturnsFlow() = runTest {
        val messages = listOf(
            Message(
                conversationId = "c1",
                content = "A",
                senderPhoneNumber = "p1",
                recipientPhoneNumber = "p2",
                type = "TEXT",
                status = "SENT"
            ),
            Message(
                conversationId = "c1",
                content = "B",
                senderPhoneNumber = "p1",
                recipientPhoneNumber = "p2",
                type = "TEXT",
                status = "SENT"
            )
        )
        coEvery { messageDao.getMessagesByConversation("c1", 100, 0) } returns flowOf(messages)

        val result = repository.getConversationMessages("c1")

        assertEquals(2, result.first().size)
        coVerify { messageDao.getMessagesByConversation("c1", 100, 0) }
    }

    @Test
    fun testUpdateMessageCallsDao() = runTest {
        val message = Message(
            id = "m1",
            conversationId = "c1",
            content = "Updated",
            senderPhoneNumber = "p1",
            recipientPhoneNumber = "p2",
            type = "TEXT",
            status = "SENT"
        )
        coEvery { messageDao.updateMessage(message) } returns Unit

        repository.updateMessage(message)

        coVerify { messageDao.updateMessage(message) }
    }

    @Test
    fun testDeleteMessageCallsDao() = runTest {
        val message = Message(
            id = "m1",
            conversationId = "c1",
            content = "Delete me",
            senderPhoneNumber = "p1",
            recipientPhoneNumber = "p2",
            type = "TEXT",
            status = "SENT"
        )
        coEvery { messageDao.deleteMessage(message) } returns Unit

        repository.deleteMessage(message)

        coVerify { messageDao.deleteMessage(message) }
    }
}
