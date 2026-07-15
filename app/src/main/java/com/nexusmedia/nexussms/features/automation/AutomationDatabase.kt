package com.nexusmedia.nexussms.features.automation

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "automation_rules")
data class AutomationRuleEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val isEnabled: Boolean,
    val priority: Int,
    val senderPattern: String?,
    val contentPattern: String?,
    val platform: String?,
    val timeRangeStart: Int?,
    val timeRangeEnd: Int?,
    val minMessageLength: Int?,
    val maxMessageLength: Int?,
    val actionsJson: String,
    val createdAt: Long,
    val lastTriggered: Long?,
    val triggerCount: Long,
    val lastTriggeredMessage: String?
)

@Entity(tableName = "automation_execution_logs")
data class ExecutionLogEntity(
    @PrimaryKey
    val id: String,
    val ruleId: String,
    val ruleName: String,
    val messageId: String,
    val senderNumber: String,
    val messagePreview: String,
    val actionsExecutedJson: String,
    val executedAt: Long,
    val success: Boolean,
    val error: String?
)

@Dao
interface AutomationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: AutomationRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ExecutionLogEntity)

    @Update
    suspend fun updateRule(rule: AutomationRuleEntity)

    @Delete
    suspend fun deleteRule(rule: AutomationRuleEntity)

    @Query("SELECT * FROM automation_rules ORDER BY priority DESC, createdAt DESC")
    fun getAllRules(): Flow<List<AutomationRuleEntity>>

    @Query("SELECT * FROM automation_rules WHERE isEnabled = 1 ORDER BY priority DESC")
    fun getEnabledRules(): Flow<List<AutomationRuleEntity>>

    @Query("SELECT * FROM automation_rules WHERE id = :ruleId")
    suspend fun getRuleById(ruleId: String): AutomationRuleEntity?

    @Query("SELECT * FROM automation_execution_logs ORDER BY executedAt DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<ExecutionLogEntity>>

    @Query("SELECT * FROM automation_execution_logs WHERE ruleId = :ruleId ORDER BY executedAt DESC")
    fun getLogsForRule(ruleId: String): Flow<List<ExecutionLogEntity>>

    @Query("UPDATE automation_rules SET lastTriggered = :timestamp, triggerCount = triggerCount + 1, lastTriggeredMessage = :messagePreview WHERE id = :ruleId")
    suspend fun incrementRuleTrigger(ruleId: String, timestamp: Long, messagePreview: String)

    @Query("DELETE FROM automation_execution_logs WHERE executedAt < :cutoffTime")
    suspend fun deleteOldLogs(cutoffTime: Long)
}
