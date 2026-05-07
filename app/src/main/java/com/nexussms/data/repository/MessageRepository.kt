package com.nexussms.data.repository

import com.nexussms.data.database.MessageDao
import com.nexussms.data.models.Message
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessageRepository @Inject constructor(
    private val messageDao: MessageDao
) {
    suspend fun insertMessage(message: Message): Long =
        messageDao.insertMessage(message)

    suspend fun insertMessages(messages: List<Message>) {
        messageDao.insertMessages(messages)
    }

    suspend fun updateMessage(message: Message) = messageDao.updateMessage(message)

    suspend fun deleteMessage(message: Message) = messageDao.deleteMessage(message)

    suspend fun getMessageById(messageId: String): Message? = messageDao.getMessageById(messageId)

    fun getMessagesByType(type: String): Flow<List<Message>> = messageDao.getMessagesByType(type)

    fun getMessagesByConversation(
        conversationId: String,
        limit: Int,
        offset: Int
    ): Flow<List<Message>> = messageDao.getMessagesByConversation(conversationId, limit, offset)

    fun getMessagesByStatus(
        conversationId: String,
        status: String
    ): Flow<List<Message>> = messageDao.getMessagesByStatus(conversationId, status)

    fun getConversationMessages(conversationId: String): Flow<List<Message>> =
        messageDao.getMessagesByConversation(conversationId, 100, 0)

    fun getPendingScheduledMessages(): Flow<List<Message>> = messageDao.getPendingScheduledMessages()

    suspend fun hardDeleteMessage(messageId: String) = messageDao.hardDeleteMessage(messageId)
}
