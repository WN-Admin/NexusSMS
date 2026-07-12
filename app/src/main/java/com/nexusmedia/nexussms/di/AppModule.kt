package com.nexusmedia.nexussms.di

import android.content.Context
import androidx.room.Room
import com.nexusmedia.nexussms.data.database.AppSecuritySettingsDao
import com.nexusmedia.nexussms.data.database.BackupMetadataDao
import com.nexusmedia.nexussms.data.database.NexusSMSDatabase
import com.nexusmedia.nexussms.data.database.addMigrations
import com.nexusmedia.nexussms.data.repository.ConversationRepository
import com.nexusmedia.nexussms.data.repository.MessageRepository
import com.nexusmedia.nexussms.data.repository.ReactionRepository
import com.nexusmedia.nexussms.data.repository.ScheduledMessageRepository
import com.nexusmedia.nexussms.data.repository.ShortcutRepository
import com.nexusmedia.nexussms.data.repository.SignatureRepository
import com.nexusmedia.nexussms.data.repository.SocialAccountRepository
import com.nexusmedia.nexussms.data.repository.ThemeRepository
import com.nexusmedia.nexussms.features.security.AppLockManager
import com.nexusmedia.nexussms.features.security.BiometricAuthManager
import com.nexusmedia.nexussms.features.security.SessionManager
import com.nexusmedia.nexussms.features.backup.GoogleDriveBackupService
import com.nexusmedia.nexussms.features.backup.GoogleDriveClient
import com.nexusmedia.nexussms.security.EncryptionManager
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

    // ThemeManager uses @Inject constructor, resolved by Hilt automatically

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
