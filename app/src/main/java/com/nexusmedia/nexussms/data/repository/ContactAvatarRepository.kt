package com.nexusmedia.nexussms.data.repository

import com.nexusmedia.nexussms.data.database.ContactAvatarDao
import com.nexusmedia.nexussms.data.models.ContactAvatar
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ContactAvatarRepository @Inject constructor(
    private val contactAvatarDao: ContactAvatarDao
) {
    suspend fun upsert(avatar: ContactAvatar) = contactAvatarDao.upsert(avatar)

    suspend fun upsertAll(avatars: List<ContactAvatar>) = contactAvatarDao.upsertAll(avatars)

    suspend fun getByPhone(normalizedPhone: String): ContactAvatar? =
        contactAvatarDao.getByPhone(normalizedPhone)

    fun getAll(): Flow<List<ContactAvatar>> = contactAvatarDao.getAll()

    suspend fun deleteByPhone(normalizedPhone: String) = contactAvatarDao.deleteByPhone(normalizedPhone)
}
