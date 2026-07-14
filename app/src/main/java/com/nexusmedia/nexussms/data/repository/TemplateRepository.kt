package com.nexusmedia.nexussms.data.repository

import com.nexusmedia.nexussms.data.database.TemplateDao
import com.nexusmedia.nexussms.data.models.Template
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepository @Inject constructor(
    private val templateDao: TemplateDao
) {
    fun getAllTemplates() = templateDao.getAllTemplates()
    fun getTemplatesByCategory(category: String) = templateDao.getTemplatesByCategory(category)
    suspend fun getTemplateById(id: String) = templateDao.getTemplateById(id)
    suspend fun insertTemplate(template: Template) = templateDao.insertTemplate(template)
    suspend fun updateTemplate(template: Template) = templateDao.updateTemplate(template)
    suspend fun deleteTemplate(template: Template) = templateDao.deleteTemplate(template)
    suspend fun incrementUsage(id: String) = templateDao.incrementUsage(id)
}
