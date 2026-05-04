package com.nexussms.data.repository

import com.nexussms.data.database.ConversationDao
import com.nexussms.data.models.Conversation
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConversationRepository @Inject constructor(
    private val conversationDao: ConversationDao
) {
    suspend fun insertConversation(conversation: Conversation): Long {
        return conversationDao.insertConversation(conversation)
    }

    suspend fun updateConversation(conversation: Conversation) {
        conversationDao.updateConversation(conversation)
    }

    suspend fun deleteConversation(conversation: Conversation) {
        conversationDao.deleteConversation(conversation)
    }

    suspend fun deleteConversationById(id: Long) {
        conversationDao.deleteConversationById(id)
    }

    fun getAllConversations(): Flow<List<Conversation>> {
        return conversationDao.getAllConversations()
    }

    fun getConversation(id: Long): Flow<Conversation?> {
        return conversationDao.getConversation(id)
    }

    fun getConversationByPhone(phone: String): Flow<Conversation?> {
        return conversationDao.getConversationByPhone(phone)
    }

    fun getPinnedConversations(): Flow<List<Conversation>> {
        return conversationDao.getPinnedConversations()
    }

    suspend fun incrementUnreadCount(id: Long) {
        conversationDao.incrementUnreadCount(id)
    }

    suspend fun clearUnreadCount(id: Long) {
        conversationDao.clearUnreadCount(id)
    }
}
