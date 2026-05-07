package com.nexussms.utils

/**
 * Small, dependency-free validation helpers.
 *
 * Phase 1A calls out validation logic scaffolding; this file provides
 * conservative checks that are safe to use from UI/domain layers.
 */
object Validators {

    fun isValidPhoneNumber(phone: String): Boolean {
        val trimmed = phone.trim()
        // Very permissive: optional leading '+', then 7..15 digits.
        return trimmed.matches("^\\+?\\d{7,15}$".toRegex())
    }

    fun isNonBlank(value: String?): Boolean = !value.isNullOrBlank()

    fun isValidShortcutTrigger(trigger: String): Boolean {
        // Examples: "!ato", "@brb"
        val t = trigger.trim()
        return (t.startsWith("!") || t.startsWith("@")) && t.length >= 2
    }
}

