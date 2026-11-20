package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.domain.model.AnnouncementCategory
import com.example.domain.model.ArchiveEntry
import com.example.domain.model.ArchiveSection
import com.example.domain.model.ClubActivityEntry
import com.example.domain.model.ClubAnnouncement
import com.example.domain.model.ClubEvent
import com.example.domain.model.ClubOrganizationRole
import com.example.domain.model.EventStatus
import com.example.domain.model.LocalMediaType
import com.example.domain.model.MediaAlbum
import com.example.domain.model.MediaGalleryItem
import com.example.domain.model.MemberProfile
import com.example.domain.model.MemberStatus
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class HqTypeConverters {
    private val moshi = Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, String::class.java)
    private val adapter = moshi.adapter<List<String>>(listType)

    @TypeConverter
    fun fromStringList(value: List<String>?): String =
        adapter.toJson(value ?: emptyList())

    @TypeConverter
    fun toStringList(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { adapter.fromJson(raw) }.getOrNull() ?: emptyList()
    }
}

@Entity(tableName = "hq_members")
data class MemberEntity(
    @PrimaryKey val id: String,
    val memberNumber: String,
    val characterName: String,
    val clubPosition: String,
    val status: String,
    val biography: String,
    val joinDate: String,
    val customRankBadge: String,
    val avatarUri: String,
    val sortOrder: Int
) {
    fun toDomain() = MemberProfile(
        id = id,
        memberNumber = memberNumber,
        characterName = characterName,
        clubPosition = clubPosition,
        status = MemberStatus.fromRaw(status),
        biography = biography,
        joinDate = joinDate,
        customRankBadge = customRankBadge,
        avatarUri = avatarUri,
        sortOrder = sortOrder
    )

    companion object {
        fun fromDomain(m: MemberProfile) = MemberEntity(
            id = m.id,
            memberNumber = m.memberNumber,
            characterName = m.characterName,
            clubPosition = m.clubPosition,
            status = m.status.name,
            biography = m.biography,
            joinDate = m.joinDate,
            customRankBadge = m.customRankBadge,
            avatarUri = m.avatarUri,
            sortOrder = m.sortOrder
        )
    }
}

@Entity(tableName = "hq_organization_roles")
data class OrganizationRoleEntity(
    @PrimaryKey val id: String,
    val roleTitle: String,
    val tierOrder: Int,
    val indonesianAlias: String,
    val mandate: String,
    val assignedHolders: String,
    val isOfficerTier: Boolean
) {
    fun toDomain() = ClubOrganizationRole(
        id = id,
        roleTitle = roleTitle,
        tierOrder = tierOrder,
        indonesianAlias = indonesianAlias,
        mandate = mandate,
        assignedHolders = assignedHolders,
        isOfficerTier = isOfficerTier
    )

    companion object {
        fun fromDomain(r: ClubOrganizationRole) = OrganizationRoleEntity(
            id = r.id,
            roleTitle = r.roleTitle,
            tierOrder = r.tierOrder,
            indonesianAlias = r.indonesianAlias,
            mandate = r.mandate,
            assignedHolders = r.assignedHolders,
            isOfficerTier = r.isOfficerTier
        )
    }
}

@Entity(tableName = "hq_announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val category: String,
    val isPinned: Boolean,
    val authorName: String,
    val dateLabel: String,
    val createdAtMs: Long
) {
    fun toDomain() = ClubAnnouncement(
        id = id,
        title = title,
        content = content,
        category = AnnouncementCategory.fromRaw(category),
        isPinned = isPinned,
        authorName = authorName,
        dateLabel = dateLabel,
        createdAtMs = createdAtMs
    )

    companion object {
        fun fromDomain(a: ClubAnnouncement) = AnnouncementEntity(
            id = a.id,
            title = a.title,
            content = a.content,
            category = a.category.name,
            isPinned = a.isPinned,
            authorName = a.authorName,
            dateLabel = a.dateLabel,
            createdAtMs = a.createdAtMs
        )
    }
}

@Entity(tableName = "hq_events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val dateTimeLabel: String,
    val location: String,
    val posterUri: String,
    val status: String,
    val attendees: List<String>,
    val createdAtMs: Long
) {
    fun toDomain() = ClubEvent(
        id = id,
        title = title,
        description = description,
        dateTimeLabel = dateTimeLabel,
        location = location,
        posterUri = posterUri,
        status = EventStatus.fromRaw(status),
        attendees = attendees,
        createdAtMs = createdAtMs
    )

    companion object {
        fun fromDomain(e: ClubEvent) = EventEntity(
            id = e.id,
            title = e.title,
            description = e.description,
            dateTimeLabel = e.dateTimeLabel,
            location = e.location,
            posterUri = e.posterUri,
            status = e.status.name,
            attendees = e.attendees,
            createdAtMs = e.createdAtMs
        )
    }
}

@Entity(tableName = "hq_media_gallery")
data class MediaGalleryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val caption: String,
    val album: String,
    val mediaType: String,
    val mediaUri: String,
    val thumbnailUri: String,
    val addedBy: String,
    val createdAtMs: Long
) {
    fun toDomain() = MediaGalleryItem(
        id = id,
        title = title,
        caption = caption,
        album = MediaAlbum.fromRaw(album),
        mediaType = LocalMediaType.fromRaw(mediaType),
        mediaUri = mediaUri,
        thumbnailUri = thumbnailUri,
        addedBy = addedBy,
        createdAtMs = createdAtMs
    )

    companion object {
        fun fromDomain(m: MediaGalleryItem) = MediaGalleryEntity(
            id = m.id,
            title = m.title,
            caption = m.caption,
            album = m.album.name,
            mediaType = m.mediaType.name,
            mediaUri = m.mediaUri,
            thumbnailUri = m.thumbnailUri,
            addedBy = m.addedBy,
            createdAtMs = m.createdAtMs
        )
    }
}

@Entity(tableName = "hq_archive_entries")
data class ArchiveEntryEntity(
    @PrimaryKey val id: String,
    val section: String,
    val yearEra: String,
    val title: String,
    val narrative: String,
    val imageUri: String,
    val sortOrder: Int
) {
    fun toDomain() = ArchiveEntry(
        id = id,
        section = ArchiveSection.fromRaw(section),
        yearEra = yearEra,
        title = title,
        narrative = narrative,
        imageUri = imageUri,
        sortOrder = sortOrder
    )

    companion object {
        fun fromDomain(e: ArchiveEntry) = ArchiveEntryEntity(
            id = e.id,
            section = e.section.name,
            yearEra = e.yearEra,
            title = e.title,
            narrative = e.narrative,
            imageUri = e.imageUri,
            sortOrder = e.sortOrder
        )
    }
}

@Entity(tableName = "hq_activity_logs")
data class ActivityLogEntity(
    @PrimaryKey val id: String,
    val summary: String,
    val categoryTag: String,
    val timestampMs: Long
) {
    fun toDomain() = ClubActivityEntry(
        id = id,
        summary = summary,
        categoryTag = categoryTag,
        timestampMs = timestampMs
    )

    companion object {
        fun fromDomain(a: ClubActivityEntry) = ActivityLogEntity(
            id = a.id,
            summary = a.summary,
            categoryTag = a.categoryTag,
            timestampMs = a.timestampMs
        )
    }
}
