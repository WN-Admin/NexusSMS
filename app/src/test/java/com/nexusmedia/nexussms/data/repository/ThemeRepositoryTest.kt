package com.nexusmedia.nexussms.data.repository

import com.nexusmedia.nexussms.data.database.ThemeDao
import com.nexusmedia.nexussms.data.models.Theme
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ThemeRepositoryTest {
    private val themeDao = mockk<ThemeDao>()
    private lateinit var repository: ThemeRepository

    @Before
    fun setup() {
        repository = ThemeRepository(themeDao)
    }

    @Test
    fun testInsertTheme() = runTest {
        val theme = Theme(
            name = "Dark Mode",
            primaryColor = "#121212",
            secondaryColor = "#03DAC6",
            backgroundColor = "#121212",
            textColor = "#FFFFFF",
            bubbleColorSent = "#03DAC6",
            bubbleColorReceived = "#424242"
        )
        coEvery { themeDao.insertTheme(theme) } returns Unit

        repository.insertTheme(theme)

        coVerify { themeDao.insertTheme(theme) }
    }

    @Test
    fun testGetThemeById() = runTest {
        val theme = Theme(
            id = "theme1",
            name = "Light Mode",
            primaryColor = "#6200EE",
            secondaryColor = "#03DAC6",
            backgroundColor = "#FFFFFF",
            textColor = "#000000",
            bubbleColorSent = "#6200EE",
            bubbleColorReceived = "#E0E0E0"
        )
        coEvery { themeDao.getThemeById("theme1") } returns theme

        val result = repository.getThemeById("theme1")

        assertNotNull(result)
        assertEquals("Light Mode", result?.name)
        coVerify { themeDao.getThemeById("theme1") }
    }

    @Test
    fun testGetAllThemes() = runTest {
        val themes = listOf(
            Theme(
                name = "Dark",
                primaryColor = "#121212", secondaryColor = "#03DAC6",
                backgroundColor = "#121212", textColor = "#FFFFFF",
                bubbleColorSent = "#03DAC6", bubbleColorReceived = "#424242"
            ),
            Theme(
                name = "Light",
                primaryColor = "#6200EE", secondaryColor = "#03DAC6",
                backgroundColor = "#FFFFFF", textColor = "#000000",
                bubbleColorSent = "#6200EE", bubbleColorReceived = "#E0E0E0"
            )
        )
        coEvery { themeDao.getAllThemes() } returns flowOf(themes)

        val result = repository.getAllThemes()

        assertEquals(2, result.first().size)
        coVerify { themeDao.getAllThemes() }
    }

    @Test
    fun testDeleteTheme() = runTest {
        val theme = Theme(
            id = "theme1",
            name = "Custom",
            primaryColor = "#FF0000", secondaryColor = "#00FF00",
            backgroundColor = "#000000", textColor = "#FFFFFF",
            bubbleColorSent = "#FF0000", bubbleColorReceived = "#00FF00"
        )
        coEvery { themeDao.deleteTheme(theme) } returns Unit

        repository.deleteTheme(theme)

        coVerify { themeDao.deleteTheme(theme) }
    }
}
