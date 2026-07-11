package com.nexusmedia.nexussms.features.shortcodes

import com.nexusmedia.nexussms.data.models.Shortcut
import com.nexusmedia.nexussms.data.repository.ShortcutRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class ShortcodeExpansionService @Inject constructor(
    private val shortcutRepository: ShortcutRepository
) {
    private val shortcodePattern = Regex("^[!@]([a-zA-Z0-9]+)$")

    data class ShortcutPreview(
        val trigger: String,
        val expansion: String,
        val category: String
    )

    suspend fun expandMessage(message: String): String {
        var expandedMessage = message
        val words = message.split(" ")

        for (word in words) {
            if (shortcodePattern.matches(word)) {
                val trigger = word
                val shortcut = shortcutRepository.getShortcutByTrigger(trigger)
                if (shortcut != null) {
                    expandedMessage = expandedMessage.replace(trigger, shortcut.expansion)
                    // Increment usage count
                    val updatedShortcut = shortcut.copy(
                        usageCount = shortcut.usageCount + 1,
                        lastUsed = System.currentTimeMillis()
                    )
                    shortcutRepository.updateShortcut(updatedShortcut)
                }
            }
        }

        return expandedMessage
    }

    suspend fun createShortcut(trigger: String, expansion: String, category: String = "General") {
        val shortcut = Shortcut(
            trigger = trigger,
            expansion = expansion,
            category = category,
            usageCount = 0
        )
        shortcutRepository.insertShortcut(shortcut)
    }

    suspend fun updateShortcut(trigger: String, newExpansion: String, newCategory: String? = null): Boolean {
        val existing = shortcutRepository.getShortcutByTrigger(trigger) ?: return false
        shortcutRepository.updateShortcut(
            existing.copy(
                expansion = newExpansion,
                category = newCategory ?: existing.category,
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    suspend fun deleteShortcut(trigger: String) {
        val shortcut = shortcutRepository.getShortcutByTrigger(trigger)
        if (shortcut != null) {
            shortcutRepository.deleteShortcut(shortcut)
        }
    }

    fun getAllShortcuts() = shortcutRepository.getGlobalShortcuts()

    suspend fun previewExpansions(message: String): List<ShortcutPreview> {
        val words = message.split(" ").filter { shortcodePattern.matches(it) }.distinct()
        if (words.isEmpty()) return emptyList()

        val previews = mutableListOf<ShortcutPreview>()
        for (trigger in words) {
            val shortcut = shortcutRepository.getShortcutByTrigger(trigger) ?: continue
            previews += ShortcutPreview(
                trigger = shortcut.trigger,
                expansion = shortcut.expansion,
                category = shortcut.category
            )
        }
        return previews
    }

    suspend fun getFrequentlyUsedShortcuts(limit: Int = 10): List<Shortcut> {
        return shortcutRepository.getGlobalShortcuts().first().take(limit)
    }
}
