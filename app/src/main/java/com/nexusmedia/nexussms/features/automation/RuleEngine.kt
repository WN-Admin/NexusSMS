package com.nexusmedia.nexussms.features.automation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

data class IncomingMessage(
    val id: String,
    val senderNumber: String,
    val senderName: String?,
    val content: String,
    val platform: String,
    val timestamp: Long,
    val conversationId: String
)

@Singleton
class RuleEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val automationDao: AutomationDao
) {
    private val _executionLogs = MutableStateFlow<List<RuleExecutionLog>>(emptyList())
    val executionLogs: StateFlow<List<RuleExecutionLog>> = _executionLogs.asStateFlow()

    private val _stats = MutableStateFlow(RuleEngineStats())
    val stats: StateFlow<RuleEngineStats> = _stats.asStateFlow()

    private val compiledPatternCache = mutableMapOf<String, Regex?>()

    suspend fun evaluateMessage(
        message: IncomingMessage,
        rules: List<MessageRule>,
        actionExecutor: suspend (RuleAction, IncomingMessage) -> Result<Unit>
    ): List<RuleExecutionLog> {
        val logs = mutableListOf<RuleExecutionLog>()
        val sortedRules = rules.filter { it.isEnabled }.sortedByDescending { it.priority }

        for (rule in sortedRules) {
            try {
                if (matchesRule(message, rule)) {
                    val log = executeRule(message, rule, actionExecutor)
                    logs.add(log)
                }
            } catch (e: Exception) {
                Timber.w(e, "Rule ${rule.id} evaluation failed")
            }
        }

        val currentLogs = _executionLogs.value.toMutableList()
        currentLogs.addAll(0, logs)
        if (currentLogs.size > 1000) {
            _executionLogs.value = currentLogs.take(1000)
        } else {
            _executionLogs.value = currentLogs
        }

        updateStats(logs)
        return logs
    }

    private fun matchesRule(message: IncomingMessage, rule: MessageRule): Boolean {
        rule.senderPattern?.let { pattern ->
            val sender = message.senderName ?: message.senderNumber
            val regex = compiledPatternCache.getOrPut(pattern) {
                try {
                    Regex(pattern, RegexOption.IGNORE_CASE)
                } catch (e: Exception) {
                    Timber.w(e, "Invalid sender regex: $pattern")
                    null
                }
            } ?: return false
            if (!regex.containsMatchIn(sender)) {
                return false
            }
        }

        rule.contentPattern?.let { pattern ->
            val regex = compiledPatternCache.getOrPut(pattern) {
                try {
                    Regex(pattern, RegexOption.IGNORE_CASE)
                } catch (e: Exception) {
                    Timber.w(e, "Invalid content regex: $pattern")
                    null
                }
            } ?: return false
            if (!regex.containsMatchIn(message.content)) {
                return false
            }
        }

        rule.platform?.let { platform ->
            if (!message.platform.equals(platform, ignoreCase = true)) {
                return false
            }
        }

        rule.timeRangeStart?.let { start ->
            rule.timeRangeEnd?.let { end ->
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val inRange = if (start <= end) {
                    hour in start..end
                } else {
                    hour >= start || hour <= end
                }
                if (!inRange) return false
            }
        }

        rule.minMessageLength?.let { min ->
            if (message.content.length < min) return false
        }

        rule.maxMessageLength?.let { max ->
            if (message.content.length > max) return false
        }

        return true
    }

    private suspend fun executeRule(
        message: IncomingMessage,
        rule: MessageRule,
        actionExecutor: suspend (RuleAction, IncomingMessage) -> Result<Unit>
    ): RuleExecutionLog {
        val executedActions = mutableListOf<ActionType>()
        var success = true
        var error: String? = null

        for (action in rule.actions) {
            val result = actionExecutor(action, message)
            if (result.isSuccess) {
                executedActions.add(action.type)
            } else {
                success = false
                error = result.exceptionOrNull()?.message ?: "Unknown error"
                break
            }
        }

        return RuleExecutionLog(
            ruleId = rule.id,
            ruleName = rule.name,
            messageId = message.id,
            senderNumber = message.senderNumber,
            messagePreview = message.content.take(100),
            actionsExecuted = executedActions,
            executedAt = System.currentTimeMillis(),
            success = success,
            error = error
        ).also { log ->
            try {
                automationDao.insertLog(
                    ExecutionLogEntity(
                        id = "${rule.id}_${message.id}_${log.executedAt}",
                        ruleId = log.ruleId,
                        ruleName = log.ruleName,
                        messageId = log.messageId,
                        senderNumber = log.senderNumber,
                        messagePreview = log.messagePreview,
                        actionsExecutedJson = log.actionsExecuted.joinToString(",") { it.name },
                        executedAt = log.executedAt,
                        success = log.success,
                        error = log.error
                    )
                )
            } catch (e: Exception) {
                Timber.w(e, "Failed to persist execution log for rule ${rule.id}")
            }
        }
    }

    fun createRule(
        name: String,
        description: String = "",
        senderPattern: String? = null,
        contentPattern: String? = null,
        platform: String? = null,
        actions: List<RuleAction>,
        priority: Int = 0
    ): MessageRule {
        return MessageRule(
            name = name,
            description = description,
            senderPattern = senderPattern,
            contentPattern = contentPattern,
            platform = platform,
            actions = actions,
            priority = priority
        )
    }

    fun createOtpForwardRule(forwardTo: String): MessageRule {
        return createRule(
            name = "Forward OTP Codes",
            description = "Automatically copy OTP codes to clipboard",
            contentPattern = "(?i)(?:otp|code|password)\\s*(?::|is)?\\s*\\d{4,8}",
            actions = listOf(
                RuleAction(
                    type = ActionType.EXTRACT_AND_COPY,
                    config = mapOf(
                        "pattern" to "\\d{4,8}",
                        "description" to "OTP code"
                    )
                )
            ),
            priority = 100
        )
    }

    fun createAutoArchiveRule(senderPattern: String): MessageRule {
        return createRule(
            name = "Auto-Archive from $senderPattern",
            description = "Automatically archive messages matching pattern",
            senderPattern = senderPattern,
            actions = listOf(
                RuleAction(type = ActionType.ARCHIVE)
            ),
            priority = 50
        )
    }

    fun createAutoReplyRule(contentPattern: String, replyMessage: String): MessageRule {
        return createRule(
            name = "Auto-Reply",
            description = "Send auto-reply when message matches pattern",
            contentPattern = contentPattern,
            actions = listOf(
                RuleAction(
                    type = ActionType.AUTO_REPLY,
                    config = mapOf("message" to replyMessage)
                )
            ),
            priority = 30
        )
    }

    private fun updateStats(logs: List<RuleExecutionLog>) {
        val current = _stats.value
        _stats.value = current.copy(
            totalExecutions = current.totalExecutions + logs.size,
            successfulExecutions = current.successfulExecutions + logs.count { it.success },
            failedExecutions = current.failedExecutions + logs.count { !it.success },
            lastExecutionTime = System.currentTimeMillis()
        )
    }
}

data class RuleEngineStats(
    val totalExecutions: Long = 0,
    val successfulExecutions: Long = 0,
    val failedExecutions: Long = 0,
    val lastExecutionTime: Long = 0
) {
    val successRate: Double
        get() = if (totalExecutions > 0) successfulExecutions.toDouble() / totalExecutions else 0.0
}
