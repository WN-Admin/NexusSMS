package com.nexussms.data.repository

import com.nexussms.data.database.ThemeDao
import com.nexussms.data.models.Theme
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ThemeRepository @Inject constructor(
    private val themeDao: ThemeDao
) {
    suspend fun insertTheme(theme: Theme) {
        themeDao.insertTheme(theme)
    }

    suspend fun updateTheme(theme: Theme) {
        themeDao.updateTheme(theme)
    }

    suspend fun deleteTheme(theme: Theme) {
        themeDao.deleteTheme(theme)
    }

    suspend fun getThemeById(themeId: String): Theme? = themeDao.getThemeById(themeId)

    fun getThemeFlow(themeId: String): Flow<Theme?> = themeDao.getThemeFlow(themeId)

    suspend fun getDefaultTheme(): Theme? = themeDao.getDefaultTheme()

    fun getAllThemes(): Flow<List<Theme>> = themeDao.getAllThemes()

    fun getBuiltInThemes(): Flow<List<Theme>> = themeDao.getBuiltInThemes()

    fun getCustomThemes(): Flow<List<Theme>> = themeDao.getCustomThemes()
}
