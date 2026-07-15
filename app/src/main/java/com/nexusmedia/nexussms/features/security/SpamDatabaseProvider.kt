package com.nexusmedia.nexussms.features.security

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SpamDetectionEntity::class,
        SpamRuleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SpamDatabase : RoomDatabase() {
    abstract fun spamDao(): SpamDao
}
