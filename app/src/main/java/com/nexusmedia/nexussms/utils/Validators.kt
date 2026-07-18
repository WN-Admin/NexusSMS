package com.nexusmedia.nexussms.utils

object Validators {

    fun isValidPhoneNumber(phone: String): Boolean {
        val trimmed = phone.trim()
        return trimmed.matches("^\\+?\\d{7,15}$".toRegex())
    }

    fun isNonBlank(value: String?): Boolean = !value.isNullOrBlank()

    fun isValidShortcutTrigger(trigger: String): Boolean {
        val t = trigger.trim()
        return (t.startsWith("!") || t.startsWith("@")) && t.length >= 2
    }

    fun normalizePhone(phone: String): String {
        return phone.replace(Regex("[^+\\d]"), "")
    }

    fun escapeLikeQuery(query: String): String {
        return query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
    }
}

