package com.nexusmedia.nexussms.data.database

import androidx.room.*
import com.nexusmedia.nexussms.security.e2e.E2ESessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface E2ESessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: E2ESessionEntity)

    @Query("SELECT * FROM e2e_sessions WHERE contactId = :contactId")
    suspend fun getSession(contactId: String): E2ESessionEntity?

    @Query("SELECT * FROM e2e_sessions WHERE contactId = :contactId")
    fun observeSession(contactId: String): Flow<E2ESessionEntity?>

    @Query("SELECT contactId FROM e2e_sessions")
    fun getAllSessionContacts(): Flow<List<String>>

    @Query("DELETE FROM e2e_sessions WHERE contactId = :contactId")
    suspend fun deleteSession(contactId: String)

    @Query("DELETE FROM e2e_sessions")
    suspend fun deleteAllSessions()
}
