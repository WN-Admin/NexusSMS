package com.nexusmedia.nexussms.features.smartreply

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartReplyService @Inject constructor() {

    data class SmartReplySuggestion(val text: String, val confidence: Float)

    fun getSuggestions(lastMessage: String, isIncoming: Boolean): List<SmartReplySuggestion> {
        if (!isIncoming) return emptyList()

        val suggestions = mutableListOf<SmartReplySuggestion>()
        val lower = lastMessage.lowercase()

        when {
            lower.contains("?") -> {
                suggestions.add(SmartReplySuggestion("Yes", 0.8f))
                suggestions.add(SmartReplySuggestion("No", 0.7f))
                suggestions.add(SmartReplySuggestion("Maybe", 0.5f))
            }
            lower.contains("hello") || lower.contains("hi ") || lower.contains("hey") -> {
                suggestions.add(SmartReplySuggestion("Hey!", 0.9f))
                suggestions.add(SmartReplySuggestion("Hi there", 0.8f))
            }
            lower.contains("thank") -> {
                suggestions.add(SmartReplySuggestion("You're welcome!", 0.9f))
                suggestions.add(SmartReplySuggestion("No problem", 0.8f))
            }
            lower.contains("sorry") -> {
                suggestions.add(SmartReplySuggestion("It's okay", 0.8f))
                suggestions.add(SmartReplySuggestion("No worries", 0.7f))
            }
            lower.contains("love") -> {
                suggestions.add(SmartReplySuggestion("Love you too!", 0.9f))
                suggestions.add(SmartReplySuggestion("\u2764\uFE0F", 0.7f))
            }
            lower.contains("ok") || lower.contains("okay") -> {
                suggestions.add(SmartReplySuggestion("\uD83D\uDC4D", 0.9f))
                suggestions.add(SmartReplySuggestion("Sounds good", 0.7f))
            }
            lower.contains("meeting") || lower.contains("dinner") || lower.contains("movie") -> {
                suggestions.add(SmartReplySuggestion("Sounds great!", 0.8f))
                suggestions.add(SmartReplySuggestion("I'm in", 0.7f))
                suggestions.add(SmartReplySuggestion("Can't, sorry", 0.6f))
            }
            lower.contains("how are you") || lower.contains("how's it going") -> {
                suggestions.add(SmartReplySuggestion("Good, you?", 0.9f))
                suggestions.add(SmartReplySuggestion("Doing well!", 0.8f))
            }
            lower.contains("good morning") || lower.contains("good night") -> {
                suggestions.add(SmartReplySuggestion("Good morning!", 0.9f))
                suggestions.add(SmartReplySuggestion("Good night!", 0.8f))
            }
            lower.contains("yes") -> {
                suggestions.add(SmartReplySuggestion("Great!", 0.8f))
                suggestions.add(SmartReplySuggestion("\uD83D\uDC4D", 0.7f))
            }
            lower.contains("no") -> {
                suggestions.add(SmartReplySuggestion("Ok", 0.7f))
                suggestions.add(SmartReplySuggestion("That's fine", 0.6f))
            }
            lower.contains("lol") || lower.contains("haha") -> {
                suggestions.add(SmartReplySuggestion("\uD83D\uDE02", 0.9f))
                suggestions.add(SmartReplySuggestion("Right?!", 0.7f))
            }
            lower.contains("on my way") || lower.contains("coming") -> {
                suggestions.add(SmartReplySuggestion("Ok, see you!", 0.9f))
                suggestions.add(SmartReplySuggestion("Take your time", 0.7f))
            }
            else -> {
                suggestions.add(SmartReplySuggestion("\uD83D\uDC4D", 0.5f))
                suggestions.add(SmartReplySuggestion("OK", 0.4f))
            }
        }
        return suggestions.take(3)
    }
}
