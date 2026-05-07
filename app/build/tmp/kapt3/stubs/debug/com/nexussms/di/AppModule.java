package com.nexussms.di;

import android.content.Context;
import androidx.room.Room;
import com.nexussms.data.database.NexusSMSDatabase;
import com.nexussms.data.repository.ConversationRepository;
import com.nexussms.data.repository.MessageRepository;
import com.nexussms.data.repository.ScheduledMessageRepository;
import com.nexussms.data.repository.ShortcutRepository;
import com.nexussms.data.repository.SignatureRepository;
import com.nexussms.data.repository.SocialAccountRepository;
import com.nexussms.data.repository.ThemeRepository;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@dagger.Module()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0006\u001a\u00020\u0007H\u0007J\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0005H\u0007J\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\n\u001a\u00020\u0005H\u0007J\u0010\u0010\r\u001a\u00020\u000e2\u0006\u0010\n\u001a\u00020\u0005H\u0007J\u0010\u0010\u000f\u001a\u00020\u00102\u0006\u0010\n\u001a\u00020\u0005H\u0007J\u0010\u0010\u0011\u001a\u00020\u00122\u0006\u0010\n\u001a\u00020\u0005H\u0007J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\n\u001a\u00020\u0005H\u0007J\u0010\u0010\u0015\u001a\u00020\u00162\u0006\u0010\n\u001a\u00020\u0005H\u0007\u00a8\u0006\u0017"}, d2 = {"Lcom/nexussms/di/AppModule;", "", "<init>", "()V", "provideDatabase", "Lcom/nexussms/data/database/NexusSMSDatabase;", "context", "Landroid/content/Context;", "provideMessageRepository", "Lcom/nexussms/data/repository/MessageRepository;", "database", "provideConversationRepository", "Lcom/nexussms/data/repository/ConversationRepository;", "provideShortcutRepository", "Lcom/nexussms/data/repository/ShortcutRepository;", "provideScheduledMessageRepository", "Lcom/nexussms/data/repository/ScheduledMessageRepository;", "provideSignatureRepository", "Lcom/nexussms/data/repository/SignatureRepository;", "provideThemeRepository", "Lcom/nexussms/data/repository/ThemeRepository;", "provideSocialAccountRepository", "Lcom/nexussms/data/repository/SocialAccountRepository;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class AppModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.nexussms.di.AppModule INSTANCE = null;
    
    private AppModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.nexussms.data.database.NexusSMSDatabase provideDatabase(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.nexussms.data.repository.MessageRepository provideMessageRepository(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.database.NexusSMSDatabase database) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.nexussms.data.repository.ConversationRepository provideConversationRepository(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.database.NexusSMSDatabase database) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.nexussms.data.repository.ShortcutRepository provideShortcutRepository(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.database.NexusSMSDatabase database) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.nexussms.data.repository.ScheduledMessageRepository provideScheduledMessageRepository(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.database.NexusSMSDatabase database) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.nexussms.data.repository.SignatureRepository provideSignatureRepository(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.database.NexusSMSDatabase database) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.nexussms.data.repository.ThemeRepository provideThemeRepository(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.database.NexusSMSDatabase database) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.nexussms.data.repository.SocialAccountRepository provideSocialAccountRepository(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.database.NexusSMSDatabase database) {
        return null;
    }
}