package com.nexussms.data.repository

import com.nexussms.data.database.ShortcutDao
import com.nexussms.data.models.Shortcut
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ShortcutRepository @Inject constructor(
    private val shortcutDao: ShortcutDao
) {
    suspend fun insertShortcut(shortcut: Shortcut) {
        shortcutDao.insertShortcut(shortcut)
    }

    suspend fun updateShortcut(shortcut: Shortcut) {
        shortcutDao.updateShortcut(shortcut)
    }

    suspend fun deleteShortcut(shortcut: Shortcut) {
        shortcutDao.deleteShortcut(shortcut)
    }

    suspend fun getShortcutById(shortcutId: String): Shortcut? = shortcutDao.getShortcutById(shortcutId)

    suspend fun getShortcutByTrigger(trigger: String): Shortcut? = shortcutDao.getShortcutByTrigger(trigger)

    fun getShortcutsByContact(contactPhoneNumber: String): Flow<List<Shortcut>> =
        shortcutDao.getShortcutsByContact(contactPhoneNumber)

    fun getGlobalShortcuts(): Flow<List<Shortcut>> = shortcutDao.getGlobalShortcuts()
}
