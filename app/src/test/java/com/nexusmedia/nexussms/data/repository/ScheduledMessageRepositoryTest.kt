package com.nexusmedia.nexussms.data.repository

import com.nexusmedia.nexussms.data.database.ScheduledMessageDao
import com.nexusmedia.nexussms.data.models.ScheduledMessage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ScheduledMessageRepositoryTest {
    private val scheduledMessageDao = mockk<ScheduledMessageDao>()
    private lateinit var repository: ScheduledMessageRepository

    @Before
    fun setup() {
        repository = ScheduledMessageRepository(scheduledMessageDao)
    }

    @Test
    fun testInsertScheduledMessage() = runTest {
        val message = ScheduledMessage(
            conversationId = "c1",
            recipientPhoneNumber = "+1234567890",
            content = "Happy Birthday!",
            scheduledTime = System.currentTimeMillis() + 86400000
        )
        coEvery { scheduledMessageDao.insertScheduledMessage(message) } returns Unit

        repository.insertScheduledMessage(message)

        coVerify { scheduledMessageDao.insertScheduledMessage(message) }
    }

    @Test
    fun testGetDueMessages() = runTest {
        val now = System.currentTimeMillis()
        val dueMessages = listOf(
            ScheduledMessage(
                id = "sm1",
                conversationId = "c1",
                recipientPhoneNumber = "+11111",
                content = "Due now",
                scheduledTime = now - 1000
            )
        )
        coEvery { scheduledMessageDao.getDueMessages(now) } returns dueMessages

        val result = repository.getDueMessages(now)

        assertEquals(1, result.size)
        assertEquals("sm1", result[0].id)
        coVerify { scheduledMessageDao.getDueMessages(now) }
    }

    @Test
    fun testCancelScheduledMessageUpdatesStatus() = runTest {
        coEvery { scheduledMessageDao.updateStatus("sm1", "CANCELLED", "Cancelled by user") } returns Unit

        repository.cancelScheduledMessage("sm1")

        coVerify {
            scheduledMessageDao.updateStatus("sm1", "CANCELLED", "Cancelled by user")
        }
    }

    @Test
    fun testCancelScheduledMessageWithCustomReason() = runTest {
        coEvery { scheduledMessageDao.updateStatus("sm1", "CANCELLED", "Out of office") } returns Unit

        repository.cancelScheduledMessage("sm1", "Out of office")

        coVerify {
            scheduledMessageDao.updateStatus("sm1", "CANCELLED", "Out of office")
        }
    }

    @Test
    fun testRescheduleMessage() = runTest {
        val original = ScheduledMessage(
            id = "sm1",
            conversationId = "c1",
            recipientPhoneNumber = "+11111",
            content = "Reminder",
            scheduledTime = 1000L,
            status = "FAILED",
            failureReason = "Network error"
        )
        val newTime = 2000L
        coEvery { scheduledMessageDao.getScheduledMessageById("sm1") } returns original
        coEvery { scheduledMessageDao.updateScheduledMessage(any()) } returns Unit

        repository.rescheduleMessage("sm1", newTime)

        coVerify {
            scheduledMessageDao.getScheduledMessageById("sm1")
            scheduledMessageDao.updateScheduledMessage(match {
                it.scheduledTime == newTime &&
                it.status == "PENDING" &&
                it.failureReason == null
            })
        }
    }
}
