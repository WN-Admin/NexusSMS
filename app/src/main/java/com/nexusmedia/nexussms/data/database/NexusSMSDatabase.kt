package com.nexusmedia.nexussms.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nexusmedia.nexussms.data.models.Message
import com.nexusmedia.nexussms.data.models.Conversation
import com.nexusmedia.nexussms.data.models.Shortcut
import com.nexusmedia.nexussms.data.models.ScheduledMessage
import com.nexusmedia.nexussms.data.models.Signature
import com.nexusmedia.nexussms.data.models.Theme
import com.nexusmedia.nexussms.data.models.SocialAccount
import com.nexusmedia.nexussms.data.models.Reaction
import com.nexusmedia.nexussms.data.models.BackupMetadata
import com.nexusmedia.nexussms.data.models.AppSecuritySettings
import com.nexusmedia.nexussms.data.models.ContactAvatar
import com.nexusmedia.nexussms.data.models.Template
import com.nexusmedia.nexussms.data.models.UnifiedContact
import com.nexusmedia.nexussms.data.converters.DateConverter
import com.nexusmedia.nexussms.security.e2e.E2ESessionEntity

@Database(
    entities = [
        Message::class,
        Conversation::class,
        Shortcut::class,
        ScheduledMessage::class,
        Signature::class,
        Theme::class,
        SocialAccount::class,
        Reaction::class,
        BackupMetadata::class,
        AppSecuritySettings::class,
        ContactAvatar::class,
        Template::class,
        UnifiedContact::class,
        E2ESessionEntity::class
    ],
    version = 10,
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class NexusSMSDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun shortcutDao(): ShortcutDao
    abstract fun scheduledMessageDao(): ScheduledMessageDao
    abstract fun signatureDao(): SignatureDao
    abstract fun themeDao(): ThemeDao
    abstract fun socialAccountDao(): SocialAccountDao
    abstract fun reactionDao(): ReactionDao
    abstract fun backupMetadataDao(): BackupMetadataDao
    abstract fun appSecuritySettingsDao(): AppSecuritySettingsDao
    abstract fun contactAvatarDao(): ContactAvatarDao
    abstract fun templateDao(): TemplateDao
    abstract fun unifiedContactDao(): UnifiedContactDao
    abstract fun e2eSessionDao(): E2ESessionDao
}
