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

    val migrations: List<Migration> = listOf(MIGRATION_1_2, MIGRATION_2_3)
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

