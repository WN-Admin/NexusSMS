package com.nexusmedia.nexussms.data.repository

import com.nexusmedia.nexussms.data.database.ScheduledMessageDao
import com.nexusmedia.nexussms.data.models.ScheduledMessage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ScheduledMessageRepository @Inject constructor(
    private val scheduledMessageDao: ScheduledMessageDao
) {
    suspend fun insertScheduledMessage(message: ScheduledMessage) {
        scheduledMessageDao.insertScheduledMessage(message)
    }

    suspend fun updateScheduledMessage(message: ScheduledMessage) {
        scheduledMessageDao.updateScheduledMessage(message)
    }

    suspend fun deleteScheduledMessage(message: ScheduledMessage) {
        scheduledMessageDao.deleteScheduledMessage(message)
    }

    suspend fun getDueMessages(currentTime: Long): List<ScheduledMessage> =
        scheduledMessageDao.getDueMessages(currentTime)

    fun getPendingScheduledMessages(): Flow<List<ScheduledMessage>> =
        scheduledMessageDao.getScheduledMessagesByStatus("PENDING")

    fun getScheduledMessagesByStatus(status: String): Flow<List<ScheduledMessage>> =
        scheduledMessageDao.getScheduledMessagesByStatus(status)

    suspend fun getScheduledMessageById(scheduledMessageId: String): ScheduledMessage? =
        scheduledMessageDao.getScheduledMessageById(scheduledMessageId)

    suspend fun cancelScheduledMessage(scheduledMessageId: String, reason: String? = "Cancelled by user") {
        scheduledMessageDao.updateStatus(
            scheduledMessageId = scheduledMessageId,
            status = "CANCELLED",
            reason = reason
        )
    }

    suspend fun rescheduleMessage(scheduledMessageId: String, newTimeMillis: Long) {
        val current = scheduledMessageDao.getScheduledMessageById(scheduledMessageId) ?: return
        scheduledMessageDao.updateScheduledMessage(
            current.copy(
                scheduledTime = newTimeMillis,
                status = "PENDING",
                failureReason = null
            )
        )
    }

    fun getScheduledMessages(conversationId: String): Flow<List<ScheduledMessage>> =
        scheduledMessageDao.getScheduledMessages(conversationId)
}
