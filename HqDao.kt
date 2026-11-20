package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM hq_members ORDER BY sortOrder ASC, characterName ASC")
    fun observeAll(): Flow<List<MemberEntity>>

    @Query("SELECT COUNT(*) FROM hq_members")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<MemberEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: MemberEntity)

    @Query("DELETE FROM hq_members WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface OrganizationRoleDao {
    @Query("SELECT * FROM hq_organization_roles ORDER BY tierOrder ASC")
    fun observeAll(): Flow<List<OrganizationRoleEntity>>

    @Query("SELECT COUNT(*) FROM hq_organization_roles")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<OrganizationRoleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: OrganizationRoleEntity)

    @Query("DELETE FROM hq_organization_roles WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM hq_announcements ORDER BY isPinned DESC, createdAtMs DESC")
    fun observeAll(): Flow<List<AnnouncementEntity>>

    @Query("SELECT COUNT(*) FROM hq_announcements")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<AnnouncementEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: AnnouncementEntity)

    @Query("DELETE FROM hq_announcements WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface EventDao {
    @Query("SELECT * FROM hq_events ORDER BY status DESC, createdAtMs DESC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT COUNT(*) FROM hq_events")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: EventEntity)

    @Query("DELETE FROM hq_events WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface MediaGalleryDao {
    @Query("SELECT * FROM hq_media_gallery ORDER BY createdAtMs DESC")
    fun observeAll(): Flow<List<MediaGalleryEntity>>

    @Query("SELECT COUNT(*) FROM hq_media_gallery")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<MediaGalleryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: MediaGalleryEntity)

    @Query("DELETE FROM hq_media_gallery WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface ArchiveDao {
    @Query("SELECT * FROM hq_archive_entries ORDER BY sortOrder ASC, title ASC")
    fun observeAll(): Flow<List<ArchiveEntryEntity>>

    @Query("SELECT COUNT(*) FROM hq_archive_entries")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ArchiveEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ArchiveEntryEntity)

    @Query("DELETE FROM hq_archive_entries WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM hq_activity_logs ORDER BY timestampMs DESC LIMIT 25")
    fun observeRecent(): Flow<List<ActivityLogEntity>>

    @Query("SELECT COUNT(*) FROM hq_activity_logs")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ActivityLogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ActivityLogEntity)
}
