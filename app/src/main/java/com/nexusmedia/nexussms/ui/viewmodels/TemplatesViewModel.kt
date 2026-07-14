package com.nexusmedia.nexussms.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusmedia.nexussms.data.models.Template
import com.nexusmedia.nexussms.data.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TemplatesViewModel @Inject constructor(
    private val templateRepository: TemplateRepository
) : ViewModel() {

    private val _filterCategory = MutableStateFlow<String?>(null)
    val filterCategory: StateFlow<String?> = _filterCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val templates: StateFlow<List<Template>> = combine(
        templateRepository.getAllTemplates(),
        _filterCategory,
        _searchQuery
    ) { all, category, query ->
        all.filter { t ->
            (category == null || t.category == category) &&
                (query.isBlank() || t.name.contains(query, ignoreCase = true) || t.content.contains(query, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories = listOf("General", "Greetings", "Thanks", "Farewell", "Custom")

    fun setFilter(category: String?) {
        _filterCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addTemplate(name: String, content: String, category: String) {
        viewModelScope.launch {
            templateRepository.insertTemplate(
                Template(name = name, content = content, category = category)
            )
        }
    }

    fun updateTemplate(template: Template) {
        viewModelScope.launch {
            templateRepository.updateTemplate(template.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteTemplate(template: Template) {
        viewModelScope.launch {
            templateRepository.deleteTemplate(template)
        }
    }

    fun incrementUsage(id: String) {
        viewModelScope.launch {
            templateRepository.incrementUsage(id)
        }
    }
}
