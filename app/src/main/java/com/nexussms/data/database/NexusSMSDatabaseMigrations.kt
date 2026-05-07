package com.nexussms.data.database

import androidx.room.RoomDatabase
import androidx.room.migration.Migration

/**
 * Placeholder for Room migrations.
 *
 * Phase 1A requires migrations infrastructure; actual migrations can be added
 * when the schema evolves.
 */
internal object NexusSMSDatabaseMigrations {
    val migrations: List<Migration> = emptyList()
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

