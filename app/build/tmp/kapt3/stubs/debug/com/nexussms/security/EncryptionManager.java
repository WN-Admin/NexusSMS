package com.nexussms.security;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyStore;
import java.security.SecureRandom;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\nJ\u000e\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\nJ\u000e\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\nJ\u000e\u0010\u0011\u001a\u00020\n2\u0006\u0010\u0012\u001a\u00020\nJ\b\u0010\u0013\u001a\u00020\u0014H\u0002J\u0010\u0010\u0015\u001a\u0004\u0018\u00010\n2\u0006\u0010\u000e\u001a\u00020\nJ\u0016\u0010\u0016\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\n2\u0006\u0010\u0017\u001a\u00020\nR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0018"}, d2 = {"Lcom/nexussms/security/EncryptionManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "encryptedSharedPreferences", "Landroid/content/SharedPreferences;", "masterKey", "Landroidx/security/crypto/MasterKey;", "decryptAES256", "", "encryptedText", "deleteSecureData", "", "key", "encryptAES256", "plaintext", "generateMessageSignature", "message", "getOrCreateAESKey", "Ljavax/crypto/SecretKey;", "retrieveSecureData", "storeSecureData", "value", "app_debug"})
public final class EncryptionManager {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.security.crypto.MasterKey masterKey = null;
    @org.jetbrains.annotations.NotNull
    private final android.content.SharedPreferences encryptedSharedPreferences = null;
    
    @javax.inject.Inject
    public EncryptionManager(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String encryptAES256(@org.jetbrains.annotations.NotNull
    java.lang.String plaintext) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String decryptAES256(@org.jetbrains.annotations.NotNull
    java.lang.String encryptedText) {
        return null;
    }
    
    public final void storeSecureData(@org.jetbrains.annotations.NotNull
    java.lang.String key, @org.jetbrains.annotations.NotNull
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String retrieveSecureData(@org.jetbrains.annotations.NotNull
    java.lang.String key) {
        return null;
    }
    
    public final void deleteSecureData(@org.jetbrains.annotations.NotNull
    java.lang.String key) {
    }
    
    private final javax.crypto.SecretKey getOrCreateAESKey() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String generateMessageSignature(@org.jetbrains.annotations.NotNull
    java.lang.String message) {
        return null;
    }
}