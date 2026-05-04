package com.nexussms.di

import android.content.Context
import androidx.room.Room
import com.nexussms.data.database.NexusSMSDatabase
import com.nexussms.data.repository.ConversationRepository
import com.nexussms.data.repository.MessageRepository
import com.nexussms.data.repository.ScheduledMessageRepository
import com.nexussms.data.repository.ShortcutRepository
import com.nexussms.data.repository.SignatureRepository
import com.nexussms.data.repository.SocialAccountRepository
import com.nexussms.data.repository.ThemeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): NexusSMSDatabase {
        return Room.databaseBuilder(
            context,
            NexusSMSDatabase::class.java,
            "nexussms_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideMessageRepository(database: NexusSMSDatabase): MessageRepository {
        return MessageRepository(database.messageDao())
    }

    @Provides
    @Singleton
    fun provideConversationRepository(database: NexusSMSDatabase): ConversationRepository {
        return ConversationRepository(database.conversationDao())
    }

    @Provides
    @Singleton
    fun provideShortcutRepository(database: NexusSMSDatabase): ShortcutRepository {
        return ShortcutRepository(database.shortcutDao())
    }

    @Provides
    @Singleton
    fun provideScheduledMessageRepository(database: NexusSMSDatabase): ScheduledMessageRepository {
        return ScheduledMessageRepository(database.scheduledMessageDao())
    }

    @Provides
    @Singleton
    fun provideSignatureRepository(database: NexusSMSDatabase): SignatureRepository {
        return SignatureRepository(database.signatureDao())
    }

    @Provides
    @Singleton
    fun provideThemeRepository(database: NexusSMSDatabase): ThemeRepository {
        return ThemeRepository(database.themeDao())
    }

    @Provides
    @Singleton
    fun provideSocialAccountRepository(database: NexusSMSDatabase): SocialAccountRepository {
        return SocialAccountRepository(database.socialAccountDao())
    }
}
