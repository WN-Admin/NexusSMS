package com.nexussms.data.repository

import com.nexussms.data.database.SocialAccountDao
import com.nexussms.data.models.SocialAccount
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SocialAccountRepositoryTest {
    private val socialAccountDao = mockk<SocialAccountDao>()
    private lateinit var repository: SocialAccountRepository

    @Before
    fun setup() {
        repository = SocialAccountRepository(socialAccountDao)
    }

    @Test
    fun testInsertAccount() = runTest {
        val account = SocialAccount(
            platform = "DISCORD",
            userId = "user123",
            username = "testuser",
            displayName = "Test User",
            accessToken = "encrypted_token"
        )
        coEvery { socialAccountDao.insertAccount(account) } returns 1L

        val result = repository.insertAccount(account)

        assertEquals(1L, result)
        coVerify { socialAccountDao.insertAccount(account) }
    }

    @Test
    fun testGetConnectedAccounts() = runTest {
        val accounts = listOf(
            SocialAccount(
                platform = "TELEGRAM",
                userId = "tg1",
                username = "teleuser",
                displayName = "Tele User",
                accessToken = "tok1",
                isConnected = true
            )
        )
        coEvery { socialAccountDao.getConnectedAccounts() } returns flowOf(accounts)

        val result = repository.getConnectedAccounts()

        assertTrue(result.first().all { it.isConnected })
        assertEquals(1, result.first().size)
        coVerify { socialAccountDao.getConnectedAccounts() }
    }

    @Test
    fun testDeleteAccount() = runTest {
        val account = SocialAccount(
            platform = "FACEBOOK_MESSENGER",
            userId = "fb1",
            username = "fbuser",
            displayName = "FB User",
            accessToken = "tok1"
        )
        coEvery { socialAccountDao.deleteAccount(account) } returns Unit

        repository.deleteAccount(account)

        coVerify { socialAccountDao.deleteAccount(account) }
    }

    @Test
    fun testGetAllAccounts() = runTest {
        val accounts = listOf(
            SocialAccount(
                platform = "DISCORD",
                userId = "d1",
                username = "disc",
                displayName = "Disc User",
                accessToken = "tok1"
            ),
            SocialAccount(
                platform = "MATRIX",
                userId = "m1",
                username = "mat",
                displayName = "Matrix User",
                accessToken = "tok2"
            )
        )
        coEvery { socialAccountDao.getAllAccounts() } returns flowOf(accounts)

        val result = repository.getAllAccounts()

        assertEquals(2, result.first().size)
        coVerify { socialAccountDao.getAllAccounts() }
    }
}
