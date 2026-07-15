package com.nexusmedia.nexussms.data.database

import androidx.room.*
import com.nexusmedia.nexussms.data.models.UnifiedContact
import kotlinx.coroutines.flow.Flow

@Dao
interface UnifiedContactDao {
    @Query("SELECT * FROM unified_contacts ORDER BY isFavorite DESC, displayName ASC")
    fun getUnifiedContacts(): Flow<List<UnifiedContact>>

    @Query("SELECT * FROM unified_contacts WHERE isHidden = 0 ORDER BY isFavorite DESC, displayName ASC")
    fun getVisibleUnifiedContacts(): Flow<List<UnifiedContact>>

    @Query("SELECT * FROM unified_contacts WHERE isFavorite = 1 ORDER BY displayName ASC")
    fun getFavoriteUnifiedContacts(): Flow<List<UnifiedContact>>

    @Query("SELECT * FROM unified_contacts WHERE isHidden = 1 ORDER BY displayName ASC")
    fun getHiddenUnifiedContacts(): Flow<List<UnifiedContact>>

    @Query("SELECT * FROM unified_contacts WHERE displayName LIKE :query OR phoneNumbers LIKE :query ORDER BY displayName ASC")
    fun searchUnifiedContacts(query: String): Flow<List<UnifiedContact>>

    @Query("SELECT * FROM unified_contacts WHERE id = :id")
    suspend fun getUnifiedContactById(id: String): UnifiedContact?

    @Query("SELECT * FROM unified_contacts WHERE phoneNumbers LIKE '%' || :phoneNumber || '%'")
    suspend fun getUnifiedContactByPhone(phoneNumber: String): UnifiedContact?

    @Query("SELECT * FROM unified_contacts WHERE platformIdentities LIKE '%' || :platform || '%' AND platformIdentities LIKE '%' || :platformId || '%'")
    suspend fun getUnifiedContactByPlatformIdentity(platform: String, platformId: String): UnifiedContact?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnifiedContact(contact: UnifiedContact)

    @Update
    suspend fun updateUnifiedContact(contact: UnifiedContact)

    @Delete
    suspend fun deleteUnifiedContact(contact: UnifiedContact)

    @Query("SELECT * FROM unified_contacts ORDER BY displayName ASC")
    suspend fun getAllUnifiedContactsList(): List<UnifiedContact>
}
