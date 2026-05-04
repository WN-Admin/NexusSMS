package com.nexussms.data.repository

import com.nexussms.data.database.SocialAccountDao
import com.nexussms.data.models.SocialAccount
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SocialAccountRepository @Inject constructor(
    private val socialAccountDao: SocialAccountDao
) {
    suspend fun insertAccount(account: SocialAccount): Long {
        return socialAccountDao.insertAccount(account)
    }

    suspend fun updateAccount(account: SocialAccount) {
        socialAccountDao.updateAccount(account)
    }

    suspend fun deleteAccount(account: SocialAccount) {
        socialAccountDao.deleteAccount(account)
    }

    fun getActiveAccounts(): Flow<List<SocialAccount>> {
        return socialAccountDao.getActiveAccounts()
    }

    fun getAccountsByPlatform(platform: String): Flow<List<SocialAccount>> {
        return socialAccountDao.getAccountsByPlatform(platform)
    }

    fun getAllAccounts(): Flow<List<SocialAccount>> {
        return socialAccountDao.getAllAccounts()
    }
}
