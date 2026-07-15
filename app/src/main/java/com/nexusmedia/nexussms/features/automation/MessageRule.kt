package com.nexusmedia.nexussms.features.automation

data class MessageRule(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val isEnabled: Boolean = true,
    val priority: Int = 0,

    val senderPattern: String? = null,
    val contentPattern: String? = null,
    val platform: String? = null,
    val timeRangeStart: Int? = null,
    val timeRangeEnd: Int? = null,
    val minMessageLength: Int? = null,
    val maxMessageLength: Int? = null,

    val actions: List<RuleAction>,

    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggered: Long? = null,
    val triggerCount: Long = 0,
    val lastTriggeredMessage: String? = null
)

data class RuleAction(
    val type: ActionType,
    val config: Map<String, String> = emptyMap()
)

enum class ActionType {
    COPY_TO_CLIPBOARD,
    FORWARD_TO_CONTACT,
    FORWARD_TO_EMAIL,
    AUTO_REPLY,
    ARCHIVE,
    MARK_AS_READ,
    DELETE,
    MUTE_CONVERSATION,
    LABEL,
    NOTIFICATION,
    WEBHOOK,
    EXTRACT_AND_COPY,
    SOUND_ALERT,
    VIBRATE_PATTERN,
    BLOCK_SENDER
}

data class RuleExecutionLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val ruleId: String,
    val ruleName: String,
    val messageId: String,
    val senderNumber: String,
    val messagePreview: String,
    val actionsExecuted: List<ActionType>,
    val executedAt: Long = System.currentTimeMillis(),
    val success: Boolean = true,
    val error: String? = null
)
