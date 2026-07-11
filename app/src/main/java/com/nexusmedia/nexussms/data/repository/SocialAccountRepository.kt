package com.nexusmedia.nexussms.data.repository

import com.nexusmedia.nexussms.data.database.SocialAccountDao
import com.nexusmedia.nexussms.data.models.SocialAccount
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SocialAccountRepository @Inject constructor(
    private val socialAccountDao: SocialAccountDao
) {
    suspend fun insertAccount(account: SocialAccount): Long =
        socialAccountDao.insertAccount(account)

    suspend fun updateAccount(account: SocialAccount) {
        socialAccountDao.updateAccount(account)
    }

    suspend fun deleteAccount(account: SocialAccount) {
        socialAccountDao.deleteAccount(account)
    }

    suspend fun getAccountByPlatform(platform: String): SocialAccount? =
        socialAccountDao.getAccountByPlatform(platform)

    fun getAccountsByPlatform(platform: String): Flow<List<SocialAccount>> =
        socialAccountDao.getAccountsByPlatform(platform)

    fun getConnectedAccounts(): Flow<List<SocialAccount>> = socialAccountDao.getConnectedAccounts()

    fun getAllAccounts(): Flow<List<SocialAccount>> = socialAccountDao.getAllAccounts()
}
