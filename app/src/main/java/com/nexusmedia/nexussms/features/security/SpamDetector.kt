package com.nexusmedia.nexussms.features.security

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class SpamPattern(
    val id: String,
    val name: String,
    val category: SpamCategory,
    val patterns: List<String>,
    val keywords: List<String>,
    val riskLevel: RiskLevel,
    val description: String,
    val recommendedAction: String
)

enum class SpamCategory {
    OTP_PHISHING,
    BANK_IMPERSONATION,
    DELIVERY_SCAM,
    LOTTERY_SCAM,
    TECH_SUPPORT_SCAM,
    ROMANCE_SCAM,
    INVESTMENT_SCAM,
    TAX_SCAM,
    CUSTOMS_SCAM,
    UTILITY_SCAM,
    UNKNOWN
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class DetectionResult(
    val isSpam: Boolean,
    val riskLevel: RiskLevel,
    val matchedPatterns: List<SpamPattern>,
    val confidence: Float,
    val detectedAt: Long = System.currentTimeMillis(),
    val messagePreview: String
)

data class SpamStats(
    val totalScanned: Long = 0,
    val spamDetected: Long = 0,
    val byCategory: Map<SpamCategory, Long> = emptyMap(),
    val byRiskLevel: Map<RiskLevel, Long> = emptyMap(),
    val lastScanTime: Long = 0
)

@Singleton
class SpamDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _stats = MutableStateFlow(SpamStats())
    val stats: StateFlow<SpamStats> = _stats.asStateFlow()

    private val _detectionHistory = MutableStateFlow<List<DetectionResult>>(emptyList())
    val detectionHistory: StateFlow<List<DetectionResult>> = _detectionHistory.asStateFlow()

    private val patterns = listOf(
        SpamPattern(
            id = "otp_1",
            name = "OTP Request Phishing",
            category = SpamCategory.OTP_PHISHING,
            patterns = listOf(
                "(?i)your\\s+otp\\s+is\\s+\\d{4,8}",
                "(?i)verification\\s+code\\s*:\\s*\\d{4,8}",
                "(?i)one\\s*time\\s+password\\s*:\\s*\\d{4,8}",
                "(?i)\\d{4,8}\\s+is\\s+your\\s+(?:otp|code|pin)",
                "(?i)enter\\s+\\d{4,8}\\s+to\\s+(?:verify|confirm|validate)"
            ),
            keywords = listOf("otp", "verification code", "one time password", "verify your account"),
            riskLevel = RiskLevel.CRITICAL,
            description = "Attempt to steal OTP codes via phishing",
            recommendedAction = "Block sender and report as phishing"
        ),
        SpamPattern(
            id = "bank_1",
            name = "Bank Impersonation",
            category = SpamCategory.BANK_IMPERSONATION,
            patterns = listOf(
                "(?i)(?:your|the)\\s+(?:bank|account)\\s+(?:has been|is)\\s+(?:locked|suspended|compromised)",
                "(?i)unauthorized\\s+(?:transaction|activity|access)\\s+(?:detected|on your account)",
                "(?i)verify\\s+(?:your|the)\\s+(?:account|identity)\\s+(?:immediately|now|urgently)",
                "(?i)click\\s+(?:here|this link)\\s+to\\s+(?:verify|confirm|secure)\\s+your\\s+account",
                "(?i)(?:secure|protect)\\s+your\\s+(?:account|banking)\\s+(?:now|immediately)"
            ),
            keywords = listOf("bank", "account locked", "account suspended", "unauthorized transaction", "verify identity", "secure your account"),
            riskLevel = RiskLevel.HIGH,
            description = "Impersonation of bank or financial institution",
            recommendedAction = "Do not click links. Contact your bank directly."
        ),
        SpamPattern(
            id = "delivery_1",
            name = "Package Delivery Scam",
            category = SpamCategory.DELIVERY_SCAM,
            patterns = listOf(
                "(?i)(?:your|a)\\s+package\\s+(?:is|has been)\\s+(?:delayed|held|waiting)",
                "(?i)(?:missed|failed)\\s+(?:delivery|delivery attempt)",
                "(?i)(?:pay|confirm)\\s+(?:a )?\\$?\\d+\\.?\\d*\\s+(?:fee|charge|tariff|customs)",
                "(?i)(?:customs|duties|taxes)\\s+(?:are|is)\\s+(?:due|owing|required)",
                "(?i)track\\s+(?:your|the)\\s+package\\s+(?:here|at|by)"
            ),
            keywords = listOf("package delivery", "missed delivery", "customs fee", "delivery fee", "track package", "hold package"),
            riskLevel = RiskLevel.HIGH,
            description = "Fake package delivery notification with phishing links",
            recommendedAction = "Do not pay any fees via SMS. Contact carrier directly."
        ),
        SpamPattern(
            id = "lottery_1",
            name = "Lottery/Prize Scam",
            category = SpamCategory.LOTTERY_SCAM,
            patterns = listOf(
                "(?i)you(?:'ve| have)\\s+(?:won|been selected|been chosen)",
                "(?i)(?:congratulations|congrats)\\s+(?:you|!)",
                "(?i)claim\\s+(?:your|the)\\s+(?:prize|reward|winnings)",
                "(?i)(?:prize|reward)\\s+(?:of|amounting to)\\s+\\$?[\\d,]+",
                "(?i)winner\\s+(?:notification|alert|announcement)"
            ),
            keywords = listOf("you won", "congratulations", "claim prize", "lottery winner", "selected winner", "prize money"),
            riskLevel = RiskLevel.MEDIUM,
            description = "Fake lottery or prize notification scam",
            recommendedAction = "Delete message. Legitimate prizes don't require fees."
        ),
        SpamPattern(
            id = "tech_1",
            name = "Tech Support Scam",
            category = SpamCategory.TECH_SUPPORT_SCAM,
            patterns = listOf(
                "(?i)(?:your|the)\\s+(?:computer|device|phone|system)\\s+(?:is|has been)\\s+(?:compromised|infected|hacked)",
                "(?i)(?:virus|malware|spyware)\\s+(?:detected|found|identified)",
                "(?i)call\\s+(?:this|the)\\s+(?:number|support line)\\s+(?:immediately|now|urgently)",
                "(?i)microsoft|apple|google|amazon\\s+(?:support|security|team)",
                "(?i)(?:your|the)\\s+(?:subscription|warranty)\\s+(?:has expired|is expiring)"
            ),
            keywords = listOf("virus detected", "computer infected", "tech support", "microsoft support", "apple support", "subscription expired"),
            riskLevel = RiskLevel.HIGH,
            description = "Fake tech support or virus alert scam",
            recommendedAction = "Ignore. Legitimate companies don't send SMS for tech support."
        ),
        SpamPattern(
            id = "romance_1",
            name = "Romance/Dating Scam",
            category = SpamCategory.ROMANCE_SCAM,
            patterns = listOf(
                "(?i)(?:hi|hello|hey)\\s+(?:dear|sweetheart|love|darling)",
                "(?i)(?:i|we)\\s+(?:met|saw|found)\\s+(?:you|your profile)",
                "(?i)(?:send|share)\\s+(?:me\\s+)?(?:money|funds|gift cards?)",
                "(?i)(?:emergency|urgent|urgent help)\\s+(?:need|required|assistance)",
                "(?i)(?:love|miss|care)\\s+(?:you|dearly|so much)"
            ),
            keywords = listOf("dear love", "send money", "gift card", "emergency help", "love you", "miss you"),
            riskLevel = RiskLevel.MEDIUM,
            description = "Romance or dating scam attempt",
            recommendedAction = "Do not send money. Report and block sender."
        ),
        SpamPattern(
            id = "invest_1",
            name = "Investment/Crypto Scam",
            category = SpamCategory.INVESTMENT_SCAM,
            patterns = listOf(
                "(?i)(?:guaranteed|guarantee)\\s+(?:returns?|profits?|income)",
                "(?i)(?:double|triple|multiply)\\s+(?:your|the)\\s+(?:money|investment|funds)",
                "(?i)(?:limited|exclusive|special)\\s+(?:offer|opportunity|deal)",
                "(?i)(?:invest|trade)\\s+(?:now|today|immediately)\\s+(?:and|to)",
                "(?i)(?:crypto|bitcoin|nft|defi)\\s+(?: opportunity|investment|guaranteed)"
            ),
            keywords = listOf("guaranteed returns", "double your money", "investment opportunity", "crypto investment", "limited offer"),
            riskLevel = RiskLevel.HIGH,
            description = "Fake investment or cryptocurrency scam",
            recommendedAction = "Delete immediately. No legitimate investment guarantees returns."
        ),
        SpamPattern(
            id = "tax_1",
            name = "Tax/Refund Scam",
            category = SpamCategory.TAX_SCAM,
            patterns = listOf(
                "(?i)(?:tax|irs|cra|hmrc)\\s+(?:refund|rebate|credit|payment)",
                "(?i)(?:you|we)\\s+(?:owe|owed|overpaid)\\s+(?:taxes?|money)",
                "(?i)(?:file|claim|apply)\\s+(?:for|your)\\s+(?:refund|rebate|credit)",
                "(?i)(?:past|overdue|outstanding)\\s+(?:tax|payment)\\s+(?:notice|alert)",
                "(?i)(?:government|federal|state)\\s+(?:refund|stimulus|payment)"
            ),
            keywords = listOf("tax refund", "irs notice", "tax payment", "refund pending", "government payment"),
            riskLevel = RiskLevel.HIGH,
            description = "Fake tax authority or refund scam",
            recommendedAction = "Verify with official tax authority. Don't click links."
        ),
        SpamPattern(
            id = "customs_1",
            name = "Customs/Shipping Fee Scam",
            category = SpamCategory.CUSTOMS_SCAM,
            patterns = listOf(
                "(?i)(?:customs|shipping|handling)\\s+(?:fee|charge|payment|due)",
                "(?i)(?:package|parcel|item)\\s+(?:is|being)\\s+(?:held|detained|seized)",
                "(?i)(?:pay|release)\\s+(?:fee|charge)\\s+(?:of\\s+)?\\$?[\\d,.]+",
                "(?i)(?:dhl|fedex|ups|usps|canada post)\\s+(?:fee|charge|payment)",
                "(?i)(?:international|customs)\\s+(?:clearance|processing)\\s+(?:fee|charge)"
            ),
            keywords = listOf("customs fee", "shipping fee", "package held", "release fee", "dhl fee", "fedex fee"),
            riskLevel = RiskLevel.HIGH,
            description = "Fake customs or shipping fee scam",
            recommendedAction = "Contact shipping company directly. Don't pay via SMS."
        ),
        SpamPattern(
            id = "utility_1",
            name = "Utility/Service Scam",
            category = SpamCategory.UTILITY_SCAM,
            patterns = listOf(
                "(?i)(?:your|the)\\s+(?:electricity|gas|water|internet|phone)\\s+(?:will be|is about to be)\\s+(?:disconnected|cut off|terminated)",
                "(?i)(?:final\\s+notice|last\\s+warning|urgent\\s+notice)\\s+(?:for|regarding)\\s+(?:payment|bill)",
                "(?i)(?:pay|settle)\\s+(?:your|the)\\s+(?:bill|account)\\s+(?:now|immediately|today)",
                "(?i)(?:service|power|internet)\\s+(?: interruption|disconnection|cutoff)\\s+(?:scheduled|pending)"
            ),
            keywords = listOf("service disconnection", "final notice", "power cut", "internet disconnected", "utility bill"),
            riskLevel = RiskLevel.HIGH,
            description = "Fake utility company disconnection threat",
            recommendedAction = "Contact utility company directly. Don't pay via SMS links."
        )
    )

    fun analyzeMessage(message: String): DetectionResult {
        val matchedPatterns = mutableListOf<SpamPattern>()
        var maxRiskLevel = RiskLevel.LOW

        for (pattern in patterns) {
            var matched = false

            for (regex in pattern.patterns) {
                if (Regex(regex).containsMatchIn(message)) {
                    matched = true
                    break
                }
            }

            if (!matched) {
                val lowerMessage = message.lowercase()
                for (keyword in pattern.keywords) {
                    if (lowerMessage.contains(keyword.lowercase())) {
                        matched = true
                        break
                    }
                }
            }

            if (matched) {
                matchedPatterns.add(pattern)
                if (pattern.riskLevel.ordinal > maxRiskLevel.ordinal) {
                    maxRiskLevel = pattern.riskLevel
                }
            }
        }

        val confidence = when {
            matchedPatterns.isEmpty() -> 0f
            matchedPatterns.size == 1 -> 0.6f
            matchedPatterns.size == 2 -> 0.75f
            matchedPatterns.size == 3 -> 0.85f
            else -> 0.95f
        }

        val adjustedConfidence = if (matchedPatterns.any { it.riskLevel == RiskLevel.CRITICAL }) {
            (confidence + 0.2f).coerceAtMost(1f)
        } else {
            confidence
        }

        val result = DetectionResult(
            isSpam = matchedPatterns.isNotEmpty(),
            riskLevel = maxRiskLevel,
            matchedPatterns = matchedPatterns,
            confidence = adjustedConfidence,
            messagePreview = message.take(100)
        )

        updateStats(result)

        val history = _detectionHistory.value.toMutableList()
        history.add(0, result)
        if (history.size > 100) history.removeLast()
        _detectionHistory.value = history

        return result
    }

    fun isSpam(message: String): Boolean {
        return analyzeMessage(message).isSpam
    }

    fun getRiskLevel(message: String): RiskLevel {
        return analyzeMessage(message).riskLevel
    }

    fun getPatternsForCategory(category: SpamCategory): List<SpamPattern> {
        return patterns.filter { it.category == category }
    }

    fun getAllPatterns(): List<SpamPattern> {
        return patterns
    }

    fun getStats(): SpamStats {
        return _stats.value
    }

    fun clearHistory() {
        _detectionHistory.value = emptyList()
        _stats.value = SpamStats()
    }

    private fun updateStats(result: DetectionResult) {
        val current = _stats.value
        val newByCategory = current.byCategory.toMutableMap()
        val newByRiskLevel = current.byRiskLevel.toMutableMap()

        if (result.isSpam) {
            result.matchedPatterns.forEach { pattern ->
                newByCategory[pattern.category] = (newByCategory[pattern.category] ?: 0) + 1
            }
            newByRiskLevel[result.riskLevel] = (newByRiskLevel[result.riskLevel] ?: 0) + 1
        }

        _stats.value = current.copy(
            totalScanned = current.totalScanned + 1,
            spamDetected = current.spamDetected + if (result.isSpam) 1 else 0,
            byCategory = newByCategory,
            byRiskLevel = newByRiskLevel,
            lastScanTime = System.currentTimeMillis()
        )
    }
}
