package com.example.data.repository

import android.net.Uri
import com.example.data.local.ActivityLogEntity
import com.example.data.local.AnnouncementEntity
import com.example.data.local.ArchiveEntryEntity
import com.example.data.local.EventEntity
import com.example.data.local.HqSettingsPreferences
import com.example.data.local.JdmcHqDatabase
import com.example.data.local.LocalMediaStorage
import com.example.data.local.MediaGalleryEntity
import com.example.data.local.MemberEntity
import com.example.data.local.OrganizationRoleEntity
import com.example.data.local.SessionPreferencesStore
import com.example.domain.model.ArchiveEntry
import com.example.domain.model.ClubActivityEntry
import com.example.domain.model.ClubAnnouncement
import com.example.domain.model.ClubEvent
import com.example.domain.model.ClubOrganizationRole
import com.example.domain.model.DefaultJdmcHqSeed
import com.example.domain.model.EventStatus
import com.example.domain.model.MediaAlbum
import com.example.domain.model.MediaGalleryItem
import com.example.domain.model.MemberProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class HqRepository(
    private val db: JdmcHqDatabase,
    private val mediaStorage: LocalMediaStorage,
    private val sessionStore: SessionPreferencesStore
) {
    val membersFlow: Flow<List<MemberProfile>> =
        db.memberDao().observeAll().map { list -> list.map { it.toDomain() } }

    val organizationRolesFlow: Flow<List<ClubOrganizationRole>> =
        db.organizationRoleDao().observeAll().map { list -> list.map { it.toDomain() } }

    val announcementsFlow: Flow<List<ClubAnnouncement>> =
        db.announcementDao().observeAll().map { list -> list.map { it.toDomain() } }

    val eventsFlow: Flow<List<ClubEvent>> =
        db.eventDao().observeAll().map { list -> list.map { it.toDomain() } }

    val mediaGalleryFlow: Flow<List<MediaGalleryItem>> =
        db.mediaGalleryDao().observeAll().map { list -> list.map { it.toDomain() } }

    val archiveEntriesFlow: Flow<List<ArchiveEntry>> =
        db.archiveDao().observeAll().map { list -> list.map { it.toDomain() } }

    val recentActivityFlow: Flow<List<ClubActivityEntry>> =
        db.activityLogDao().observeRecent().map { list -> list.map { it.toDomain() } }

    val hqSettingsFlow: Flow<HqSettingsPreferences> = sessionStore.hqSettingsFlow

    suspend fun ensureSeeded() {
        val now = System.currentTimeMillis()
        if (db.memberDao().count() == 0) {
            db.memberDao().upsertAll(DefaultJdmcHqSeed.initialMembers.map(MemberEntity::fromDomain))
        }
        if (db.organizationRoleDao().count() == 0) {
            db.organizationRoleDao().upsertAll(DefaultJdmcHqSeed.initialOrganizationRoles.map(OrganizationRoleEntity::fromDomain))
        }
        if (db.announcementDao().count() == 0) {
            db.announcementDao().upsertAll(DefaultJdmcHqSeed.initialAnnouncements(now).map(AnnouncementEntity::fromDomain))
        }
        if (db.eventDao().count() == 0) {
            db.eventDao().upsertAll(DefaultJdmcHqSeed.initialEvents(now).map(EventEntity::fromDomain))
        }
        if (db.mediaGalleryDao().count() == 0) {
            db.mediaGalleryDao().upsertAll(DefaultJdmcHqSeed.initialMediaItems(now).map(MediaGalleryEntity::fromDomain))
            sessionStore.markBundledVideosV1Seeded()
        } else if (!sessionStore.isBundledVideosV1Seeded()) {
            db.mediaGalleryDao().upsertAll(DefaultJdmcHqSeed.bundledVideos(now).map(MediaGalleryEntity::fromDomain))
            sessionStore.markBundledVideosV1Seeded()
        }
        if (db.archiveDao().count() == 0) {
            db.archiveDao().upsertAll(DefaultJdmcHqSeed.initialArchiveEntries.map(ArchiveEntryEntity::fromDomain))
        }
        if (db.activityLogDao().count() == 0) {
            db.activityLogDao().insertAll(DefaultJdmcHqSeed.initialActivities(now).map(ActivityLogEntity::fromDomain))
        }
    }

    suspend fun importPickedImageUri(uri: Uri): Result<String> =
        mediaStorage.importFromPickerUri(uri).map { it.savedFileUri }

    suspend fun saveMember(member: MemberProfile) {
        db.memberDao().upsert(MemberEntity.fromDomain(member))
        logActivity("Updated member dossier: ${member.characterName} (${member.clubPosition})", "CLUB")
    }

    suspend fun deleteMember(member: MemberProfile) {
        db.memberDao().deleteById(member.id)
        mediaStorage.deleteSavedFiles(member.avatarUri)
        logActivity("Removed member profile: ${member.characterName}", "CLUB")
    }

    suspend fun saveOrganizationRole(role: ClubOrganizationRole) {
        db.organizationRoleDao().upsert(OrganizationRoleEntity.fromDomain(role))
        logActivity("Updated organization role: ${role.roleTitle}", "ORG")
    }

    suspend fun saveAnnouncement(announcement: ClubAnnouncement) {
        db.announcementDao().upsert(AnnouncementEntity.fromDomain(announcement))
        logActivity("Published dispatch: ${announcement.title}", "NEWS")
    }

    suspend fun toggleAnnouncementPin(announcement: ClubAnnouncement) {
        val updated = announcement.copy(isPinned = !announcement.isPinned)
        db.announcementDao().upsert(AnnouncementEntity.fromDomain(updated))
        logActivity("${if (updated.isPinned) "Pinned" else "Unpinned"} announcement: ${updated.title}", "NEWS")
    }

    suspend fun deleteAnnouncement(announcement: ClubAnnouncement) {
        db.announcementDao().deleteById(announcement.id)
        logActivity("Deleted announcement: ${announcement.title}", "NEWS")
    }

    suspend fun saveEvent(event: ClubEvent) {
        db.eventDao().upsert(EventEntity.fromDomain(event))
        logActivity("Saved event: ${event.title}", "EVENT")
    }

    suspend fun toggleEventAttendance(event: ClubEvent, memberName: String) {
        val trimmed = memberName.trim()
        if (trimmed.isBlank()) return
        val updatedList = if (event.attendees.any { it.equals(trimmed, ignoreCase = true) }) {
            event.attendees.filterNot { it.equals(trimmed, ignoreCase = true) }
        } else {
            event.attendees + trimmed
        }
        db.eventDao().upsert(EventEntity.fromDomain(event.copy(attendees = updatedList)))
        logActivity("$trimmed updated RSVP for ${event.title}", "EVENT")
    }

    suspend fun toggleEventStatus(event: ClubEvent) {
        val next = if (event.status == EventStatus.UPCOMING) EventStatus.COMPLETED else EventStatus.UPCOMING
        db.eventDao().upsert(EventEntity.fromDomain(event.copy(status = next)))
        logActivity("Marked ${event.title} as ${next.label}", "EVENT")
    }

    suspend fun deleteEvent(event: ClubEvent) {
        db.eventDao().deleteById(event.id)
        mediaStorage.deleteSavedFiles(event.posterUri)
        logActivity("Deleted event: ${event.title}", "EVENT")
    }

    suspend fun savePickedGalleryMedia(
        pickerUri: Uri,
        title: String,
        caption: String,
        album: MediaAlbum,
        addedBy: String
    ): Result<MediaGalleryItem> {
        val importedResult = mediaStorage.importFromPickerUri(pickerUri)
        return importedResult.map { imported ->
            val item = MediaGalleryItem(
                id = "media-${UUID.randomUUID()}",
                title = title.trim().ifBlank { "JDMC ${imported.mediaType.label}" },
                caption = caption.trim(),
                album = album,
                mediaType = imported.mediaType,
                mediaUri = imported.savedFileUri,
                thumbnailUri = imported.thumbnailUri,
                addedBy = addedBy.trim().ifBlank { "JDMC Rider" },
                createdAtMs = System.currentTimeMillis()
            )
            db.mediaGalleryDao().upsert(MediaGalleryEntity.fromDomain(item))
            logActivity("Saved ${imported.mediaType.label.lowercase()} to ${album.label}: ${item.title}", "MEDIA")
            item
        }
    }

    suspend fun updateMediaItem(item: MediaGalleryItem) {
        db.mediaGalleryDao().upsert(MediaGalleryEntity.fromDomain(item))
        logActivity("Updated media item: ${item.title}", "MEDIA")
    }

    suspend fun deleteMediaItem(item: MediaGalleryItem) {
        db.mediaGalleryDao().deleteById(item.id)
        mediaStorage.deleteSavedFiles(item.mediaUri, item.thumbnailUri)
        logActivity("Removed media item: ${item.title}", "MEDIA")
    }

    suspend fun saveArchiveEntry(entry: ArchiveEntry) {
        db.archiveDao().upsert(ArchiveEntryEntity.fromDomain(entry))
        logActivity("Updated digital archive: ${entry.title}", "ARCHIVE")
    }

    suspend fun deleteArchiveEntry(entry: ArchiveEntry) {
        db.archiveDao().deleteById(entry.id)
        mediaStorage.deleteSavedFiles(entry.imageUri)
        logActivity("Removed archive entry: ${entry.title}", "ARCHIVE")
    }

    suspend fun updateHqSettings(welcomeHeadline: String, clubStatusLabel: String, activeMemberAlias: String) {
        sessionStore.updateHqAppearanceSettings(welcomeHeadline, clubStatusLabel, activeMemberAlias)
        logActivity("Updated HQ club status & dashboard settings", "SETTINGS")
    }

    suspend fun setOfficerMode(active: Boolean) {
        sessionStore.setHqOfficerModeActive(active)
        logActivity(if (active) "Officer Administration session started" else "Officer Administration session ended", "ADMIN")
    }

    suspend fun setOfficerPin(pin: String?) {
        sessionStore.setHqOfficerPin(pin)
    }

    private suspend fun logActivity(summary: String, tag: String) {
        db.activityLogDao().insert(
            ActivityLogEntity(
                id = "act-${UUID.randomUUID()}",
                summary = summary,
                categoryTag = tag,
                timestampMs = System.currentTimeMillis()
            )
        )
    }
}
