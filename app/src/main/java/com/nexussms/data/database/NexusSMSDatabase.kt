package com.nexussms.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nexussms.data.models.Message
import com.nexussms.data.models.Conversation
import com.nexussms.data.models.Shortcut
import com.nexussms.data.models.ScheduledMessage
import com.nexussms.data.models.Signature
import com.nexussms.data.models.Theme
import com.nexussms.data.models.SocialAccount
import com.nexussms.data.models.Reaction
import com.nexussms.data.models.BackupMetadata
import com.nexussms.data.models.AppSecuritySettings
import com.nexussms.data.converters.DateConverter
import com.nexussms.data.converters.JsonConverter

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
        AppSecuritySettings::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateConverter::class, JsonConverter::class)
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

    companion object {
        @Volatile
        private var Instance: NexusSMSDatabase? = null

        fun getDatabase(context: Context): NexusSMSDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    NexusSMSDatabase::class.java,
                    "nexussms_database"
                )
                    .addMigrations()
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
