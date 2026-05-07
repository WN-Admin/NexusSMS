package com.nexussms.data.repository

import com.nexussms.data.database.ReactionCount
import com.nexussms.data.database.ReactionDao
import com.nexussms.data.models.Reaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReactionRepository @Inject constructor(
    private val reactionDao: ReactionDao
) {
    suspend fun insertReaction(reaction: Reaction) {
        reactionDao.insertReaction(reaction)
    }

    suspend fun deleteReaction(reaction: Reaction) {
        reactionDao.deleteReaction(reaction)
    }

    fun getReactionsByMessage(messageId: String): Flow<List<Reaction>> =
        reactionDao.getReactionsByMessage(messageId)

    fun getReactionSummary(messageId: String): Flow<List<ReactionCount>> =
        reactionDao.getReactionSummary(messageId)
}

