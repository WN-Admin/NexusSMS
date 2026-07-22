package com.nexusmedia.nexussms.data.repository

import com.nexusmedia.nexussms.data.database.UnifiedContactDao
import com.nexusmedia.nexussms.data.database.ConversationDao
import com.nexusmedia.nexussms.data.models.UnifiedContact
import com.nexusmedia.nexussms.data.models.PlatformIdentity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnifiedContactRepository @Inject constructor(
    private val unifiedContactDao: UnifiedContactDao,
    private val conversationDao: ConversationDao,
    private val gson: Gson
) {
    fun getAllContacts(): Flow<List<UnifiedContact>> = unifiedContactDao.getUnifiedContacts()

    fun getVisibleContacts(): Flow<List<UnifiedContact>> = unifiedContactDao.getVisibleUnifiedContacts()

    fun getFavoriteContacts(): Flow<List<UnifiedContact>> = unifiedContactDao.getFavoriteUnifiedContacts()

    fun getHiddenContacts(): Flow<List<UnifiedContact>> = unifiedContactDao.getHiddenUnifiedContacts()

    fun searchContacts(query: String): Flow<List<UnifiedContact>> = unifiedContactDao.searchUnifiedContacts("%$query%")

    suspend fun getContactById(id: String): UnifiedContact? = unifiedContactDao.getUnifiedContactById(id)

    suspend fun getContactByPhoneNumber(phoneNumber: String): UnifiedContact? =
        unifiedContactDao.getUnifiedContactByPhone(phoneNumber)

    suspend fun getContactByPlatformIdentity(platform: String, platformId: String): UnifiedContact? =
        unifiedContactDao.getUnifiedContactByPlatformIdentity(platform, platformId)

    suspend fun insertContact(contact: UnifiedContact): String {
        unifiedContactDao.insertUnifiedContact(contact)
        return contact.id
    }

    suspend fun updateContact(contact: UnifiedContact) {
        unifiedContactDao.updateUnifiedContact(contact.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteContact(contact: UnifiedContact) {
        unifiedContactDao.deleteUnifiedContact(contact)
    }

    suspend fun addPhoneNumber(contactId: String, phoneNumber: String) {
        val contact = unifiedContactDao.getUnifiedContactById(contactId) ?: return
        val phones: MutableList<String> = gson.fromJson(
            contact.phoneNumbers,
            object : TypeToken<MutableList<String>>() {}.type
        )
        if (!phones.contains(phoneNumber)) {
            phones.add(phoneNumber)
            unifiedContactDao.updateUnifiedContact(
                contact.copy(
                    phoneNumbers = gson.toJson(phones),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun removePhoneNumber(contactId: String, phoneNumber: String) {
        val contact = unifiedContactDao.getUnifiedContactById(contactId) ?: return
        val phones: MutableList<String> = gson.fromJson(
            contact.phoneNumbers,
            object : TypeToken<MutableList<String>>() {}.type
        )
        phones.remove(phoneNumber)
        unifiedContactDao.updateUnifiedContact(
            contact.copy(
                phoneNumbers = gson.toJson(phones),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun addPlatformIdentity(contactId: String, identity: PlatformIdentity) {
        val contact = unifiedContactDao.getUnifiedContactById(contactId) ?: return
        val identities: MutableList<PlatformIdentity> = gson.fromJson(
            contact.platformIdentities,
            object : TypeToken<MutableList<PlatformIdentity>>() {}.type
        )
        identities.removeAll { it.platform == identity.platform && it.id == identity.id }
        identities.add(identity)
        unifiedContactDao.updateUnifiedContact(
            contact.copy(
                platformIdentities = gson.toJson(identities),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun removePlatformIdentity(contactId: String, platform: String, platformId: String) {
        val contact = unifiedContactDao.getUnifiedContactById(contactId) ?: return
        val identities: MutableList<PlatformIdentity> = gson.fromJson(
            contact.platformIdentities,
            object : TypeToken<MutableList<PlatformIdentity>>() {}.type
        )
        identities.removeAll { it.platform == platform && it.id == platformId }
        unifiedContactDao.updateUnifiedContact(
            contact.copy(
                platformIdentities = gson.toJson(identities),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleFavorite(contactId: String) {
        val contact = unifiedContactDao.getUnifiedContactById(contactId) ?: return
        unifiedContactDao.updateUnifiedContact(
            contact.copy(
                isFavorite = !contact.isFavorite,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun setHidden(contactId: String, hidden: Boolean) {
        val contact = unifiedContactDao.getUnifiedContactById(contactId) ?: return
        unifiedContactDao.updateUnifiedContact(
            contact.copy(
                isHidden = hidden,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun mergeContacts(primaryId: String, secondaryId: String) {
        val primary = unifiedContactDao.getUnifiedContactById(primaryId) ?: return
        val secondary = unifiedContactDao.getUnifiedContactById(secondaryId) ?: return

        val primaryPhones: MutableList<String> = gson.fromJson(
            primary.phoneNumbers,
            object : TypeToken<MutableList<String>>() {}.type
        )
        val secondaryPhones: MutableList<String> = gson.fromJson(
            secondary.phoneNumbers,
            object : TypeToken<MutableList<String>>() {}.type
        )
        val primaryIdentities: MutableList<PlatformIdentity> = gson.fromJson(
            primary.platformIdentities,
            object : TypeToken<MutableList<PlatformIdentity>>() {}.type
        )
        val secondaryIdentities: MutableList<PlatformIdentity> = gson.fromJson(
            secondary.platformIdentities,
            object : TypeToken<MutableList<PlatformIdentity>>() {}.type
        )

        val mergedPhones = (primaryPhones + secondaryPhones).distinct()
        val mergedIdentities = (primaryIdentities + secondaryIdentities).distinctBy { "${it.platform}_${it.id}" }

        unifiedContactDao.mergeContactsAtomic(
            primary = primary.copy(
                phoneNumbers = gson.toJson(mergedPhones),
                platformIdentities = gson.toJson(mergedIdentities),
                isFavorite = primary.isFavorite || secondary.isFavorite,
                notes = listOfNotNull(primary.notes, secondary.notes).joinToString("\n").ifBlank { null },
                updatedAt = System.currentTimeMillis()
            ),
            secondary = secondary
        )
    }

    suspend fun autoLinkFromConversations() {
        val conversations = conversationDao.getAllConversationsList()
        val existingContacts = unifiedContactDao.getAllUnifiedContactsList()

        for (conversation in conversations) {
            val participantPhones: List<String> = gson.fromJson(
                conversation.participantPhoneNumbers,
                object : TypeToken<List<String>>() {}.type
            )

            var linked = false
            for (phone in participantPhones) {
                if (phone.startsWith("+")) {
                    val existing = existingContacts.find { contact ->
                        val phones: List<String> = gson.fromJson(
                            contact.phoneNumbers,
                            object : TypeToken<List<String>>() {}.type
                        )
                        phones.contains(phone)
                    }
                    if (existing != null) {
                        if (conversation.sourcePlatform != "SMS") {
                            addPlatformIdentity(
                                existing.id,
                                PlatformIdentity(
                                    platform = conversation.sourcePlatform,
                                    id = participantPhones.first(),
                                    username = conversation.displayName,
                                    displayName = conversation.displayName
                                )
                            )
                        }
                        linked = true
                        break
                    }
                }
            }

            if (!linked && conversation.sourcePlatform == "SMS") {
                val phone = participantPhones.firstOrNull { it.startsWith("+") }
                if (phone != null) {
                    insertContact(
                        UnifiedContact(
                            displayName = conversation.displayName,
                            phoneNumbers = gson.toJson(listOf(phone)),
                            avatarUri = conversation.avatarUrl
                        )
                    )
                }
            }
        }
    }
}
