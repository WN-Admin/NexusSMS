package com.nexusmedia.nexussms.data.repository

import com.nexusmedia.nexussms.data.database.ConversationDao
import com.nexusmedia.nexussms.data.models.Conversation
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConversationRepository @Inject constructor(
    private val conversationDao: ConversationDao
) {
    suspend fun insertConversation(conversation: Conversation) {
        conversationDao.insertConversation(conversation)
    }

    suspend fun updateConversation(conversation: Conversation) {
        conversationDao.updateConversation(conversation)
    }

    suspend fun deleteConversation(conversation: Conversation) {
        conversationDao.deleteConversation(conversation)
    }

    suspend fun getConversationById(conversationId: String): Conversation? =
        conversationDao.getConversationById(conversationId)

    fun getPinnedConversations(): Flow<List<Conversation>> =
        conversationDao.getPinnedConversations()

    fun getAllConversations(): Flow<List<Conversation>> {
        return conversationDao.getAllConversations()
    }

    fun getConversationsByPlatform(platform: String): Flow<List<Conversation>> {
        return conversationDao.getConversationsByPlatform(platform)
    }

    fun getActivePlatforms(): Flow<List<String>> {
        return conversationDao.getActivePlatforms()
    }

    fun getArchivedConversations(): Flow<List<Conversation>> {
        return conversationDao.getArchivedConversations()
    }

    fun getUnreadConversations(): Flow<List<Conversation>> {
        return conversationDao.getUnreadConversations()
    }

    suspend fun markConversationAsRead(conversationId: String) {
        val conversation = conversationDao.getConversationById(conversationId)
        if (conversation != null) {
            conversationDao.updateConversation(conversation.copy(unreadCount = 0))
        }
    }

    suspend fun clearUnreadCount(conversationId: String) {
        val conversation = conversationDao.getConversationById(conversationId)
        if (conversation != null) {
            conversationDao.updateConversation(conversation.copy(unreadCount = 0))
        }
    }

    suspend fun deleteConversationById(conversationId: String) {
        val conversation = conversationDao.getConversationById(conversationId)
        if (conversation != null) {
            conversationDao.deleteConversation(conversation)
        }
    }

    suspend fun findConversationWithParticipant(phoneNumber: String): Conversation? =
        conversationDao.findConversationWithParticipant(phoneNumber)
}
