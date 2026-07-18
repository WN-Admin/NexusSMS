package com.nexusmedia.nexussms.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.keyChangeDataStore: DataStore<Preferences> by preferencesDataStore(name = "key_change_warnings")

@Singleton
class KeyChangeWarningStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getKeyChangedContacts(): Flow<Set<String>> {
        return context.keyChangeDataStore.data.map { prefs ->
            val json = prefs[KEY_CHANGED_CONTACTS] ?: ""
            if (json.isBlank()) emptySet() else json.split(",").toSet()
        }
    }

    fun hasKeyChangeWarning(contactId: String): Flow<Boolean> {
        return getKeyChangedContacts().map { it.contains(contactId) }
    }

    suspend fun markKeyChanged(contactId: String) {
        context.keyChangeDataStore.edit { prefs ->
            val current = prefs[KEY_CHANGED_CONTACTS]?.split(",")?.filter { it.isNotBlank() }?.toMutableSet() ?: mutableSetOf()
            current.add(contactId)
            prefs[KEY_CHANGED_CONTACTS] = current.joinToString(",")
        }
    }

    suspend fun dismissKeyChangeWarning(contactId: String) {
        context.keyChangeDataStore.edit { prefs ->
            val current = prefs[KEY_CHANGED_CONTACTS]?.split(",")?.filter { it.isNotBlank() }?.toMutableSet() ?: mutableSetOf()
            current.remove(contactId)
            prefs[KEY_CHANGED_CONTACTS] = current.joinToString(",")
        }
    }

    companion object {
        private val KEY_CHANGED_CONTACTS = stringPreferencesKey("key_changed_contacts")
    }
}
