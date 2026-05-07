package com.nexussms.data.repository;

import com.nexussms.data.database.SocialAccountDao;
import com.nexussms.data.models.SocialAccount;
import kotlinx.coroutines.flow.Flow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0016\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\u000b\u001a\u00020\f2\u0006\u0010\b\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\r\u001a\u00020\f2\u0006\u0010\b\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\nJ\u0012\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\u00100\u000fJ\u001a\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\u00100\u000f2\u0006\u0010\u0012\u001a\u00020\u0013J\u0012\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\u00100\u000fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcom/nexussms/data/repository/SocialAccountRepository;", "", "socialAccountDao", "Lcom/nexussms/data/database/SocialAccountDao;", "<init>", "(Lcom/nexussms/data/database/SocialAccountDao;)V", "insertAccount", "", "account", "Lcom/nexussms/data/models/SocialAccount;", "(Lcom/nexussms/data/models/SocialAccount;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateAccount", "", "deleteAccount", "getActiveAccounts", "Lkotlinx/coroutines/flow/Flow;", "", "getAccountsByPlatform", "platform", "", "getAllAccounts", "app_debug"})
public final class SocialAccountRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.nexussms.data.database.SocialAccountDao socialAccountDao = null;
    
    @javax.inject.Inject()
    public SocialAccountRepository(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.database.SocialAccountDao socialAccountDao) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertAccount(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.models.SocialAccount account, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateAccount(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.models.SocialAccount account, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteAccount(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.models.SocialAccount account, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.SocialAccount>> getActiveAccounts() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.SocialAccount>> getAccountsByPlatform(@org.jetbrains.annotations.NotNull()
    java.lang.String platform) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.nexussms.data.models.SocialAccount>> getAllAccounts() {
        return null;
    }
}