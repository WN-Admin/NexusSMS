package com.nexussms.data.repository

import com.nexussms.data.database.MessageDao
import com.nexussms.data.models.Message
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessageRepository @Inject constructor(
    private val messageDao: MessageDao
) {
    suspend fun insertMessage(message: Message): Long {
        return messageDao.insertMessage(message)
    }

    suspend fun updateMessage(message: Message) {
        messageDao.updateMessage(message)
    }

    suspend fun deleteMessage(message: Message) {
        messageDao.deleteMessage(message)
    }

    fun getMessage(id: Long): Flow<Message?> {
        return messageDao.getMessage(id)
    }

    fun getConversationMessages(conversationId: Long): Flow<List<Message>> {
        return messageDao.getConversationMessages(conversationId)
    }

    fun getUnreadMessages(conversationId: Long): Flow<List<Message>> {
        return messageDao.getUnreadMessages(conversationId)
    }

    fun getUnreadCount(conversationId: Long): Flow<Int> {
        return messageDao.getUnreadCount(conversationId)
    }

    suspend fun deleteConversationMessages(conversationId: Long) {
        messageDao.deleteConversationMessages(conversationId)
    }

    suspend fun markConversationAsRead(conversationId: Long) {
        messageDao.markConversationAsRead(conversationId)
    }

    fun getRecentMessages(startTime: Long, limit: Int = 100): Flow<List<Message>> {
        return messageDao.getRecentMessages(startTime, limit)
    }

    fun getMessagesByType(type: String): Flow<List<Message>> {
        return messageDao.getMessagesByType(type)
    }
}
