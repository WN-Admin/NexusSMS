package com.nexussms.data.repository

import com.nexussms.data.database.ShortcutDao
import com.nexussms.data.models.Shortcut
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ShortcutRepository @Inject constructor(
    private val shortcutDao: ShortcutDao
) {
    suspend fun insertShortcut(shortcut: Shortcut): Long {
        return shortcutDao.insertShortcut(shortcut)
    }

    suspend fun updateShortcut(shortcut: Shortcut) {
        shortcutDao.updateShortcut(shortcut)
    }

    suspend fun deleteShortcut(shortcut: Shortcut) {
        shortcutDao.deleteShortcut(shortcut)
    }

    fun getAllShortcuts(): Flow<List<Shortcut>> {
        return shortcutDao.getAllShortcuts()
    }

    fun getShortcut(trigger: String): Flow<Shortcut?> {
        return shortcutDao.getShortcut(trigger)
    }

    fun searchShortcuts(pattern: String): Flow<List<Shortcut>> {
        return shortcutDao.searchShortcuts("%$pattern%")
    }

    suspend fun incrementUsageCount(id: Long) {
        shortcutDao.incrementUsageCount(id)
    }
}
