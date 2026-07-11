package com.nexusmedia.nexussms.data.repository

import com.nexusmedia.nexussms.data.database.ShortcutDao
import com.nexusmedia.nexussms.data.models.Shortcut
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ShortcutRepositoryTest {
    private val shortcutDao = mockk<ShortcutDao>()
    private lateinit var repository: ShortcutRepository

    @Before
    fun setup() {
        repository = ShortcutRepository(shortcutDao)
    }

    @Test
    fun testInsertShortcut() = runTest {
        val shortcut = Shortcut(trigger = "!ato", expansion = "At the office")
        coEvery { shortcutDao.insertShortcut(shortcut) } returns Unit

        repository.insertShortcut(shortcut)

        coVerify { shortcutDao.insertShortcut(shortcut) }
    }

    @Test
    fun testGetShortcutById() = runTest {
        val shortcut = Shortcut(id = "s1", trigger = "!brb", expansion = "Be right back")
        coEvery { shortcutDao.getShortcutById("s1") } returns shortcut

        val result = repository.getShortcutById("s1")

        assertNotNull(result)
        assertEquals("!brb", result?.trigger)
        coVerify { shortcutDao.getShortcutById("s1") }
    }

    @Test
    fun testGetGlobalShortcuts() = runTest {
        val shortcuts = listOf(
            Shortcut(trigger = "!omw", expansion = "On my way"),
            Shortcut(trigger = "!ttyl", expansion = "Talk to you later")
        )
        coEvery { shortcutDao.getGlobalShortcuts() } returns flowOf(shortcuts)

        val result = repository.getGlobalShortcuts()

        assertEquals(2, result.first().size)
        coVerify { shortcutDao.getGlobalShortcuts() }
    }

    @Test
    fun testUpdateShortcut() = runTest {
        val shortcut = Shortcut(id = "s1", trigger = "!brb", expansion = "Be right back")
        coEvery { shortcutDao.updateShortcut(shortcut) } returns Unit

        repository.updateShortcut(shortcut)

        coVerify { shortcutDao.updateShortcut(shortcut) }
    }
}
