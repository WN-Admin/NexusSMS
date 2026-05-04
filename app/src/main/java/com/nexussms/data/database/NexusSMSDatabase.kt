package com.nexussms.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nexussms.data.models.Message
import com.nexussms.data.models.Conversation
import com.nexussms.data.models.Shortcut
import com.nexussms.data.models.ScheduledMessage
import com.nexussms.data.models.UserSignature
import com.nexussms.data.models.Theme
import com.nexussms.data.models.SocialAccount
import com.nexussms.data.models.ContactShortcut

@Database(
    entities = [
        Message::class,
        Conversation::class,
        Shortcut::class,
        ScheduledMessage::class,
        UserSignature::class,
        Theme::class,
        SocialAccount::class,
        ContactShortcut::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NexusSMSDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun shortcutDao(): ShortcutDao
    abstract fun scheduledMessageDao(): ScheduledMessageDao
    abstract fun signatureDao(): SignatureDao
    abstract fun themeDao(): ThemeDao
    abstract fun socialAccountDao(): SocialAccountDao
    abstract fun contactShortcutDao(): ContactShortcutDao

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
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
