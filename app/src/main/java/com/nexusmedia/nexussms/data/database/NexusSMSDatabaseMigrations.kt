package com.nexusmedia.nexussms.data.database

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object NexusSMSDatabaseMigrations {
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE conversations ADD COLUMN sourcePlatform TEXT NOT NULL DEFAULT 'SMS'")
            db.execSQL("ALTER TABLE conversations ADD COLUMN sourceAccountId TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE messages ADD COLUMN sourcePlatform TEXT NOT NULL DEFAULT 'SMS'")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE messages ADD COLUMN sourceSmsId INTEGER DEFAULT NULL")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_messages_conversationId_sourceSmsId ON messages (conversationId, sourceSmsId)")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS contact_avatars (
                    normalized_phone TEXT NOT NULL PRIMARY KEY,
                    photo_uri TEXT DEFAULT NULL,
                    display_name TEXT DEFAULT NULL,
                    updated_at INTEGER NOT NULL DEFAULT 0
                )
            """)
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE messages ADD COLUMN isLocked INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS templates (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    content TEXT NOT NULL,
                    category TEXT NOT NULL DEFAULT 'General',
                    usageCount INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL DEFAULT 0,
                    updatedAt INTEGER NOT NULL DEFAULT 0
                )
            """)
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS unified_contacts (
                    id TEXT NOT NULL PRIMARY KEY,
                    displayName TEXT NOT NULL,
                    phoneNumbers TEXT NOT NULL DEFAULT '[]',
                    platformIdentities TEXT NOT NULL DEFAULT '[]',
                    avatarUri TEXT DEFAULT NULL,
                    isFavorite INTEGER NOT NULL DEFAULT 0,
                    isHidden INTEGER NOT NULL DEFAULT 0,
                    notes TEXT DEFAULT NULL,
                    createdAt INTEGER NOT NULL DEFAULT 0,
                    updatedAt INTEGER NOT NULL DEFAULT 0
                )
            """)
            db.execSQL("CREATE INDEX IF NOT EXISTS index_unified_contacts_displayName ON unified_contacts (displayName)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_unified_contacts_isFavorite ON unified_contacts (isFavorite)")
        }
    }

    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS spam_detections (
                    id TEXT NOT NULL PRIMARY KEY,
                    messagePreview TEXT NOT NULL,
                    isSpam INTEGER NOT NULL,
                    riskLevel TEXT NOT NULL,
                    matchedPatternIds TEXT NOT NULL,
                    confidence REAL NOT NULL,
                    detectedAt INTEGER NOT NULL DEFAULT 0,
                    senderNumber TEXT DEFAULT NULL,
                    conversationId TEXT DEFAULT NULL,
                    actionTaken TEXT DEFAULT NULL
                )
            """)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS spam_rules (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    pattern TEXT NOT NULL,
                    riskLevel TEXT NOT NULL,
                    isActive INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL DEFAULT 0,
                    lastTriggered INTEGER DEFAULT NULL,
                    triggerCount INTEGER NOT NULL DEFAULT 0
                )
            """)
        }
    }

    val migrations: List<Migration> = listOf(
        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
        MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8
    )
}

/**
 * Adds known migrations when available.
 *
 * If no migrations are defined yet, this is a no-op.
 */
fun RoomDatabase.Builder<NexusSMSDatabase>.addMigrations(): RoomDatabase.Builder<NexusSMSDatabase> {
    val migrations = NexusSMSDatabaseMigrations.migrations
    return if (migrations.isNotEmpty()) {
        this.addMigrations(*migrations.toTypedArray())
    } else {
        this
    }
}

