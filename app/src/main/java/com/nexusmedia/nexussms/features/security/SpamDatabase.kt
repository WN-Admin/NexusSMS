package com.nexusmedia.nexussms.features.security

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "spam_detections")
data class SpamDetectionEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val messagePreview: String,
    val isSpam: Boolean,
    val riskLevel: String,
    val matchedPatternIds: String,
    val confidence: Float,
    val detectedAt: Long = System.currentTimeMillis(),
    val senderNumber: String? = null,
    val conversationId: String? = null,
    val actionTaken: String? = null
)

@Entity(tableName = "spam_rules")
data class SpamRuleEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val pattern: String,
    val riskLevel: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggered: Long? = null,
    val triggerCount: Long = 0
)

@Dao
interface SpamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetection(detection: SpamDetectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: SpamRuleEntity)

    @Update
    suspend fun updateDetection(detection: SpamDetectionEntity)

    @Update
    suspend fun updateRule(rule: SpamRuleEntity)

    @Delete
    suspend fun deleteDetection(detection: SpamDetectionEntity)

    @Delete
    suspend fun deleteRule(rule: SpamRuleEntity)

    @Query("SELECT * FROM spam_detections ORDER BY detectedAt DESC LIMIT :limit")
    fun getRecentDetections(limit: Int = 50): Flow<List<SpamDetectionEntity>>

    @Query("SELECT * FROM spam_detections WHERE isSpam = 1 ORDER BY detectedAt DESC")
    fun getSpamDetections(): Flow<List<SpamDetectionEntity>>

    @Query("SELECT * FROM spam_detections WHERE riskLevel = :riskLevel ORDER BY detectedAt DESC")
    fun getDetectionsByRisk(riskLevel: String): Flow<List<SpamDetectionEntity>>

    @Query("SELECT * FROM spam_rules WHERE isActive = 1")
    fun getActiveRules(): Flow<List<SpamRuleEntity>>

    @Query("SELECT * FROM spam_rules")
    fun getAllRules(): Flow<List<SpamRuleEntity>>

    @Query("SELECT * FROM spam_rules WHERE id = :ruleId")
    suspend fun getRuleById(ruleId: String): SpamRuleEntity?

    @Query("SELECT COUNT(*) FROM spam_detections WHERE isSpam = 1")
    suspend fun getSpamCount(): Long

    @Query("SELECT COUNT(*) FROM spam_detections")
    suspend fun getTotalScanned(): Long

    @Query("SELECT * FROM spam_detections WHERE senderNumber = :senderNumber ORDER BY detectedAt DESC")
    fun getDetectionsForSender(senderNumber: String): Flow<List<SpamDetectionEntity>>

    @Query("SELECT * FROM spam_detections WHERE conversationId = :conversationId ORDER BY detectedAt DESC")
    fun getDetectionsForConversation(conversationId: String): Flow<List<SpamDetectionEntity>>

    @Query("UPDATE spam_rules SET lastTriggered = :timestamp, triggerCount = triggerCount + 1 WHERE id = :ruleId")
    suspend fun incrementRuleTrigger(ruleId: String, timestamp: Long)
}
