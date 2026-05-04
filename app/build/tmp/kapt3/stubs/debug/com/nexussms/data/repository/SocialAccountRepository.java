package com.nexussms.data.repository;

import com.nexussms.data.database.SocialAccountDao;
import com.nexussms.data.models.SocialAccount;
import kotlinx.coroutines.flow.Flow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0019\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tJ\u001a\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\f0\u000b2\u0006\u0010\r\u001a\u00020\u000eJ\u0012\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\f0\u000bJ\u0012\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\f0\u000bJ\u0019\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tJ\u0019\u0010\u0013\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u0014"}, d2 = {"Lcom/nexussms/data/repository/SocialAccountRepository;", "", "socialAccountDao", "Lcom/nexussms/data/database/SocialAccountDao;", "(Lcom/nexussms/data/database/SocialAccountDao;)V", "deleteAccount", "", "account", "Lcom/nexussms/data/models/SocialAccount;", "(Lcom/nexussms/data/models/SocialAccount;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAccountsByPlatform", "Lkotlinx/coroutines/flow/Flow;", "", "platform", "", "getActiveAccounts", "getAllAccounts", "insertAccount", "", "updateAccount", "app_debug"})
public final class SocialAccountRepository {
    @org.jetbrains.annotations.NotNull
    private final com.nexussms.data.database.SocialAccountDao socialAccountDao = null;
    
    @javax.inject.Inject
    public SocialAccountRepository(@org.jetbrains.annotations.NotNull
    com.nexussms.data.database.SocialAccountDao socialAccountDao) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object insertAccount(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.SocialAccount account, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object updateAccount(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.SocialAccount account, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object deleteAccount(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.SocialAccount account, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.SocialAccount>> getActiveAccounts() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.SocialAccount>> getAccountsByPlatform(@org.jetbrains.annotations.NotNull
    java.lang.String platform) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.SocialAccount>> getAllAccounts() {
        return null;
    }
}