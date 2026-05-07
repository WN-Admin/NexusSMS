package com.nexussms.di

import android.content.Context
import androidx.room.Room
import com.nexussms.data.database.AppSecuritySettingsDao
import com.nexussms.data.database.BackupMetadataDao
import com.nexussms.data.database.NexusSMSDatabase
import com.nexussms.data.database.addMigrations
import com.nexussms.data.repository.ConversationRepository
import com.nexussms.data.repository.MessageRepository
import com.nexussms.data.repository.ReactionRepository
import com.nexussms.data.repository.ScheduledMessageRepository
import com.nexussms.data.repository.ShortcutRepository
import com.nexussms.data.repository.SignatureRepository
import com.nexussms.data.repository.SocialAccountRepository
import com.nexussms.data.repository.ThemeRepository
import com.nexussms.features.security.AppLockManager
import com.nexussms.features.security.BiometricAuthManager
import com.nexussms.features.security.SessionManager
import com.nexussms.features.backup.GoogleDriveBackupService
import com.nexussms.features.backup.GoogleDriveClient
import com.nexussms.security.EncryptionManager
import com.nexussms.features.theme.ThemeManager
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
            .addMigrations()
            .fallbackToDestructiveMigration()
            .build()
    }

    // --- DAOs ---

    @Provides
    @Singleton
    fun provideBackupMetadataDao(database: NexusSMSDatabase): BackupMetadataDao =
        database.backupMetadataDao()

    @Provides
    @Singleton
    fun provideAppSecuritySettingsDao(database: NexusSMSDatabase): AppSecuritySettingsDao =
        database.appSecuritySettingsDao()

    // --- Repositories ---

    @Provides
    @Singleton
    fun provideMessageRepository(database: NexusSMSDatabase): MessageRepository =
        MessageRepository(database.messageDao())

    @Provides
    @Singleton
    fun provideConversationRepository(database: NexusSMSDatabase): ConversationRepository =
        ConversationRepository(database.conversationDao())

    @Provides
    @Singleton
    fun provideShortcutRepository(database: NexusSMSDatabase): ShortcutRepository =
        ShortcutRepository(database.shortcutDao())

    @Provides
    @Singleton
    fun provideScheduledMessageRepository(database: NexusSMSDatabase): ScheduledMessageRepository =
        ScheduledMessageRepository(database.scheduledMessageDao())

    @Provides
    @Singleton
    fun provideSignatureRepository(database: NexusSMSDatabase): SignatureRepository =
        SignatureRepository(database.signatureDao())

    @Provides
    @Singleton
    fun provideThemeRepository(database: NexusSMSDatabase): ThemeRepository =
        ThemeRepository(database.themeDao())

    @Provides
    @Singleton
    fun provideSocialAccountRepository(database: NexusSMSDatabase): SocialAccountRepository =
        SocialAccountRepository(database.socialAccountDao())

    @Provides
    @Singleton
    fun provideReactionRepository(database: NexusSMSDatabase): ReactionRepository =
        ReactionRepository(database.reactionDao())

    // --- Features ---

    @Provides
    @Singleton
    fun provideThemeManager(themeRepository: ThemeRepository): ThemeManager =
        ThemeManager(themeRepository)

    // --- Security ---

    @Provides
    @Singleton
    fun provideEncryptionManager(
        @ApplicationContext context: Context
    ): EncryptionManager = EncryptionManager(context)

    @Provides
    @Singleton
    fun provideBiometricAuthManager(
        @ApplicationContext context: Context,
        appSecuritySettingsDao: AppSecuritySettingsDao
    ): BiometricAuthManager = BiometricAuthManager(context, appSecuritySettingsDao)

    @Provides
    @Singleton
    fun provideAppLockManager(
        appSecuritySettingsDao: AppSecuritySettingsDao
    ): AppLockManager = AppLockManager(appSecuritySettingsDao)

    @Provides
    @Singleton
    fun provideSessionManager(
        appSecuritySettingsDao: AppSecuritySettingsDao
    ): SessionManager = SessionManager(appSecuritySettingsDao)

    // --- Services ---

    @Provides
    @Singleton
    fun provideGoogleDriveClient(
        @ApplicationContext context: Context
    ): GoogleDriveClient = GoogleDriveClient(context)

    @Provides
    @Singleton
    fun provideGoogleDriveBackupService(
        @ApplicationContext context: Context,
        backupMetadataDao: BackupMetadataDao,
        shortcutRepository: ShortcutRepository,
        signatureRepository: SignatureRepository,
        themeRepository: ThemeRepository,
        googleDriveClient: GoogleDriveClient,
        encryptionManager: EncryptionManager
    ): GoogleDriveBackupService = GoogleDriveBackupService(
        context,
        backupMetadataDao,
        shortcutRepository,
        signatureRepository,
        themeRepository,
        googleDriveClient,
        encryptionManager
    )
}
