package com.nexussms.data.repository

import com.nexussms.data.database.SignatureDao
import com.nexussms.data.models.UserSignature
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignatureRepository @Inject constructor(
    private val signatureDao: SignatureDao
) {
    suspend fun insertSignature(signature: UserSignature): Long {
        return signatureDao.insertSignature(signature)
    }

    suspend fun updateSignature(signature: UserSignature) {
        signatureDao.updateSignature(signature)
    }

    suspend fun deleteSignature(signature: UserSignature) {
        signatureDao.deleteSignature(signature)
    }

    fun getAllSignatures(): Flow<List<UserSignature>> {
        return signatureDao.getAllSignatures()
    }

    fun getDefaultSignature(): Flow<UserSignature?> {
        return signatureDao.getDefaultSignature()
    }
}
