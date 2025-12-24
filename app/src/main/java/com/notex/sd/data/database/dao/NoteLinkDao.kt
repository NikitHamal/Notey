package com.notex.sd.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.notex.sd.data.database.entity.NoteLinkEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for note links.
 * Handles wiki-style [[note]] linking operations.
 */
@Dao
interface NoteLinkDao {

    /**
     * Insert a new link or replace if exists.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: NoteLinkEntity)

    /**
     * Insert multiple links.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinks(links: List<NoteLinkEntity>)

    /**
     * Delete a specific link.
     */
    @Query("DELETE FROM note_links WHERE id = :linkId")
    suspend fun deleteLink(linkId: String)

    /**
     * Delete all links from a source note.
     */
    @Query("DELETE FROM note_links WHERE source_note_id = :noteId")
    suspend fun deleteLinksFromNote(noteId: String)

    /**
     * Get all links from a specific note.
     */
    @Query("SELECT * FROM note_links WHERE source_note_id = :noteId ORDER BY created_at DESC")
    fun getLinksFromNote(noteId: String): Flow<List<NoteLinkEntity>>

    /**
     * Get all links from a specific note (sync version).
     */
    @Query("SELECT * FROM note_links WHERE source_note_id = :noteId ORDER BY created_at DESC")
    suspend fun getLinksFromNoteSync(noteId: String): List<NoteLinkEntity>

    /**
     * Get all backlinks (notes that link TO this note).
     */
    @Query("SELECT * FROM note_links WHERE target_note_id = :noteId ORDER BY created_at DESC")
    fun getBacklinksToNote(noteId: String): Flow<List<NoteLinkEntity>>

    /**
     * Get all backlinks count for a note.
     */
    @Query("SELECT COUNT(*) FROM note_links WHERE target_note_id = :noteId")
    fun getBacklinksCount(noteId: String): Flow<Int>

    /**
     * Check if a specific link exists.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM note_links WHERE source_note_id = :sourceId AND target_note_id = :targetId)")
    suspend fun linkExists(sourceId: String, targetId: String): Boolean

    /**
     * Get all links (for building knowledge graph).
     */
    @Query("SELECT * FROM note_links ORDER BY created_at DESC")
    fun getAllLinks(): Flow<List<NoteLinkEntity>>

    /**
     * Get notes that are highly linked (popular notes).
     */
    @Query("""
        SELECT target_note_id, COUNT(*) as link_count
        FROM note_links
        GROUP BY target_note_id
        ORDER BY link_count DESC
        LIMIT :limit
    """)
    suspend fun getMostLinkedNotes(limit: Int = 10): List<LinkCountResult>

    /**
     * Update all links for a note (delete old, insert new).
     */
    @Transaction
    suspend fun updateLinksForNote(noteId: String, newLinks: List<NoteLinkEntity>) {
        deleteLinksFromNote(noteId)
        insertLinks(newLinks)
    }
}

/**
 * Result class for link count queries.
 */
data class LinkCountResult(
    val target_note_id: String,
    val link_count: Int
)
