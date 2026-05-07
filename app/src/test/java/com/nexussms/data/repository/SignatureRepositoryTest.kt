package com.nexussms.data.repository

import com.nexussms.data.database.SignatureDao
import com.nexussms.data.models.Signature
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SignatureRepositoryTest {
    private val signatureDao = mockk<SignatureDao>()
    private lateinit var repository: SignatureRepository

    @Before
    fun setup() {
        repository = SignatureRepository(signatureDao)
    }

    @Test
    fun testInsertSignature() = runTest {
        val signature = Signature(name = "Work", content = "-- Sent from NexusSMS")
        coEvery { signatureDao.insertSignature(signature) } returns Unit

        repository.insertSignature(signature)

        coVerify { signatureDao.insertSignature(signature) }
    }

    @Test
    fun testGetAllSignatures() = runTest {
        val signatures = listOf(
            Signature(name = "Personal", content = "Cheers!"),
            Signature(name = "Work", content = "-- Regards")
        )
        coEvery { signatureDao.getAllSignatures() } returns flowOf(signatures)

        val result = repository.getAllSignatures()

        assertEquals(2, result.first().size)
        coVerify { signatureDao.getAllSignatures() }
    }

    @Test
    fun testGetDefaultSignature() = runTest {
        val signature = Signature(id = "s1", name = "Default", content = "Sent from my phone", isDefault = true)
        coEvery { signatureDao.getDefaultSignature() } returns signature

        val result = repository.getDefaultSignature()

        assertNotNull(result)
        assertTrue(result!!.isDefault)
        coVerify { signatureDao.getDefaultSignature() }
    }

    @Test
    fun testSetDefaultSignatureCallsClearAndMark() = runTest {
        coEvery { signatureDao.clearDefaultSignature() } returns Unit
        coEvery { signatureDao.markAsDefault("sig1") } returns Unit

        repository.setDefaultSignature("sig1")

        coVerify {
            signatureDao.clearDefaultSignature()
            signatureDao.markAsDefault("sig1")
        }
    }
}
