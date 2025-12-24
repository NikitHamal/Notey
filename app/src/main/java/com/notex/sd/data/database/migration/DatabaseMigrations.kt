package com.notex.sd.data.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migrations for NoteXDatabase.
 *
 * This object contains all manual migrations for the database.
 * Manual migrations provide explicit control over schema changes and
 * are preferred over auto-migrations for production applications.
 */
object DatabaseMigrations {

    /**
     * Migration from version 1 to version 2.
     *
     * Adds the note_links table for wiki-style [[note]] linking feature.
     * This enables users to create bidirectional links between notes,
     * similar to features in Obsidian and Notion.
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create note_links table for wiki-style note linking
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `note_links` (
                    `id` TEXT NOT NULL,
                    `source_note_id` TEXT NOT NULL,
                    `target_note_id` TEXT NOT NULL,
                    `link_text` TEXT NOT NULL,
                    `created_at` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`source_note_id`) REFERENCES `notes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`target_note_id`) REFERENCES `notes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )

            // Create indices for efficient querying
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_note_links_source_note_id` ON `note_links` (`source_note_id`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_note_links_target_note_id` ON `note_links` (`target_note_id`)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_note_links_source_note_id_target_note_id` ON `note_links` (`source_note_id`, `target_note_id`)"
            )
        }
    }

    /**
     * List of all migrations in order.
     * Add new migrations here as they are created.
     */
    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_1_2
    )
}
