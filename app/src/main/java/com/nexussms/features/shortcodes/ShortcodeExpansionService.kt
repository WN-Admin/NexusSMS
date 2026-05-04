package com.nexussms.features.shortcodes

import com.nexussms.data.repository.ShortcutRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class ShortcodeExpansionService @Inject constructor(
    private val shortcutRepository: ShortcutRepository
) {
    private val shortcodePattern = Regex("^[!@]([a-zA-Z0-9]+)$")

    suspend fun expandMessage(message: String): String {
        var expandedMessage = message
        val words = message.split(" ")

        for (word in words) {
            if (shortcodePattern.matches(word)) {
                val trigger = word
                val shortcut = shortcutRepository.getShortcut(trigger).first()
                if (shortcut != null) {
                    expandedMessage = expandedMessage.replace(trigger, shortcut.expansion)
                    // Increment usage count
                    shortcutRepository.incrementUsageCount(shortcut.id)
                }
            }
        }

        return expandedMessage
    }

    suspend fun createShortcut(trigger: String, expansion: String, category: String = "") {
        val shortcut = com.nexussms.data.models.Shortcut(
            trigger = trigger,
            expansion = expansion,
            category = category,
            usageCount = 0
        )
        shortcutRepository.insertShortcut(shortcut)
    }

    suspend fun deleteShortcut(trigger: String) {
        val shortcut = shortcutRepository.getShortcut(trigger).first()
        if (shortcut != null) {
            shortcutRepository.deleteShortcut(shortcut)
        }
    }

    suspend fun getAllShortcuts() = shortcutRepository.getAllShortcuts()

    suspend fun getFrequentlyUsedShortcuts(limit: Int = 10): List<com.nexussms.data.models.Shortcut> {
        return shortcutRepository.getAllShortcuts().first().take(limit)
    }
}
