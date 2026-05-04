package com.nexussms.data.repository

import com.nexussms.data.database.ThemeDao
import com.nexussms.data.models.Theme
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ThemeRepository @Inject constructor(
    private val themeDao: ThemeDao
) {
    suspend fun insertTheme(theme: Theme): Long {
        return themeDao.insertTheme(theme)
    }

    suspend fun updateTheme(theme: Theme) {
        themeDao.updateTheme(theme)
    }

    suspend fun deleteTheme(theme: Theme) {
        themeDao.deleteTheme(theme)
    }

    fun getAllThemes(): Flow<List<Theme>> {
        return themeDao.getAllThemes()
    }

    fun getTheme(id: Long): Flow<Theme?> {
        return themeDao.getTheme(id)
    }

    fun getDefaultThemes(): Flow<List<Theme>> {
        return themeDao.getDefaultThemes()
    }

    fun getCustomThemes(): Flow<List<Theme>> {
        return themeDao.getCustomThemes()
    }
}
