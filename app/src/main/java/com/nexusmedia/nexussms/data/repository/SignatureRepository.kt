package com.nexusmedia.nexussms.data.repository

import com.nexusmedia.nexussms.data.database.SignatureDao
import com.nexusmedia.nexussms.data.models.Signature
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignatureRepository @Inject constructor(
    private val signatureDao: SignatureDao
) {
    suspend fun insertSignature(signature: Signature) {
        signatureDao.insertSignature(signature)
    }

    suspend fun updateSignature(signature: Signature) {
        signatureDao.updateSignature(signature)
    }

    suspend fun deleteSignature(signature: Signature) {
        signatureDao.deleteSignature(signature)
    }

    fun getAllSignatures(): Flow<List<Signature>> {
        return signatureDao.getAllSignatures()
    }

    suspend fun getSignatureById(signatureId: String): Signature? =
        signatureDao.getSignatureById(signatureId)

    suspend fun getDefaultSignature(): Signature? = signatureDao.getDefaultSignature()

    suspend fun setDefaultSignature(signatureId: String) {
        signatureDao.clearDefaultSignature()
        signatureDao.markAsDefault(signatureId)
    }
}
