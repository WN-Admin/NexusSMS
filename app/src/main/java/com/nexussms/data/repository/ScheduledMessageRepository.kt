package com.nexussms.data.repository

import com.nexussms.data.database.ScheduledMessageDao
import com.nexussms.data.models.ScheduledMessage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ScheduledMessageRepository @Inject constructor(
    private val scheduledMessageDao: ScheduledMessageDao
) {
    suspend fun insertScheduledMessage(message: ScheduledMessage): Long {
        return scheduledMessageDao.insertScheduledMessage(message)
    }

    suspend fun updateScheduledMessage(message: ScheduledMessage) {
        scheduledMessageDao.updateScheduledMessage(message)
    }

    suspend fun deleteScheduledMessage(message: ScheduledMessage) {
        scheduledMessageDao.deleteScheduledMessage(message)
    }

    suspend fun deleteScheduledMessageById(id: Long) {
        scheduledMessageDao.deleteScheduledMessageById(id)
    }

    fun getAllScheduledMessages(): Flow<List<ScheduledMessage>> {
        return scheduledMessageDao.getAllScheduledMessages()
    }

    fun getPendingScheduledMessages(): Flow<List<ScheduledMessage>> {
        return scheduledMessageDao.getPendingScheduledMessages()
    }

    fun getScheduledMessage(id: Long): Flow<ScheduledMessage?> {
        return scheduledMessageDao.getScheduledMessage(id)
    }
}
