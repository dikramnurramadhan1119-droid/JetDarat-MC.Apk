package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.HqSettingsPreferences
import com.example.data.local.JdmcHqDatabase
import com.example.data.local.LocalMediaStorage
import com.example.data.local.SessionPreferencesStore
import com.example.data.repository.HqRepository
import com.example.domain.model.AnnouncementCategory
import com.example.domain.model.ArchiveEntry
import com.example.domain.model.ArchiveSection
import com.example.domain.model.ClubActivityEntry
import com.example.domain.model.ClubAnnouncement
import com.example.domain.model.ClubEvent
import com.example.domain.model.ClubOrganizationRole
import com.example.domain.model.EventStatus
import com.example.domain.model.MediaAlbum
import com.example.domain.model.MediaGalleryItem
import com.example.domain.model.MemberProfile
import com.example.domain.model.MemberStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ClockSnapshot(
    val timeText: String = "00:00:00",
    val dateText: String = ""
)

data class JdmcHqUiState(
    val isSplashVisible: Boolean = true,
    val members: List<MemberProfile> = emptyList(),
    val organizationRoles: List<ClubOrganizationRole> = emptyList(),
    val announcements: List<ClubAnnouncement> = emptyList(),
    val events: List<ClubEvent> = emptyList(),
    val mediaItems: List<MediaGalleryItem> = emptyList(),
    val archiveEntries: List<ArchiveEntry> = emptyList(),
    val recentActivities: List<ClubActivityEntry> = emptyList(),
    val settings: HqSettingsPreferences = HqSettingsPreferences(),
    val isImportingMedia: Boolean = false,
    val bannerMessage: String? = null,
    val officerAuthError: String? = null
)

class JdmcHqViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionStore = SessionPreferencesStore(application.applicationContext)
    private val mediaStorage = LocalMediaStorage(application.applicationContext)
    private val database = JdmcHqDatabase.getInstance(application.applicationContext)
    private val repository = HqRepository(database, mediaStorage, sessionStore)

    private val _ephemeral = MutableStateFlow(EphemeralState())
    private val _clock = MutableStateFlow(currentClockSnapshot())
    val clockState: StateFlow<ClockSnapshot> = _clock.asStateFlow()

    private data class EphemeralState(
        val isSplashVisible: Boolean = true,
        val isImportingMedia: Boolean = false,
        val bannerMessage: String? = null,
        val officerAuthError: String? = null
    )

    private val coreDataFlow = combine(
        repository.membersFlow,
        repository.organizationRolesFlow,
        repository.announcementsFlow,
        repository.eventsFlow,
        repository.mediaGalleryFlow
    ) { members, roles, announcements, events, media ->
        CoreDataBundle(members, roles, announcements, events, media)
    }

    private data class CoreDataBundle(
        val members: List<MemberProfile>,
        val roles: List<ClubOrganizationRole>,
        val announcements: List<ClubAnnouncement>,
        val events: List<ClubEvent>,
        val media: List<MediaGalleryItem>
    )

    val uiState: StateFlow<JdmcHqUiState> = combine(
        coreDataFlow,
        repository.archiveEntriesFlow,
        repository.recentActivityFlow,
        repository.hqSettingsFlow,
        _ephemeral
    ) { core, archive, activities, settings, ephem ->
        JdmcHqUiState(
            isSplashVisible = ephem.isSplashVisible,
            members = core.members,
            organizationRoles = core.roles,
            announcements = core.announcements,
            events = core.events,
            mediaItems = core.media,
            archiveEntries = archive,
            recentActivities = activities,
            settings = settings,
            isImportingMedia = ephem.isImportingMedia,
            bannerMessage = ephem.bannerMessage,
            officerAuthError = ephem.officerAuthError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = JdmcHqUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
        viewModelScope.launch {
            delay(1_250L)
            _ephemeral.update { it.copy(isSplashVisible = false) }
        }
        viewModelScope.launch {
            while (true) {
                _clock.value = currentClockSnapshot()
                delay(1_000L)
            }
        }
    }

    fun dismissSplash() {
        _ephemeral.update { it.copy(isSplashVisible = false) }
    }

    fun clearBanner() {
        _ephemeral.update { it.copy(bannerMessage = null, officerAuthError = null) }
    }

    // --- Officer Admin Session (Clean Member vs Officer Separation) ---

    fun loginOfficerAdmin(pinInput: String) {
        val currentSettings = uiState.value.settings
        if (currentSettings.hasOfficerPin) {
            val expectedHash = currentSettings.officerPinHash
            val actualHash = SessionPreferencesStore.hashPin(pinInput.trim(), currentSettings.officerPinSalt)
            if (actualHash != expectedHash) {
                _ephemeral.update { it.copy(officerAuthError = "Incorrect local Officer PIN.") }
                return
            }
        }
        viewModelScope.launch {
            repository.setOfficerMode(true)
            _ephemeral.update {
                it.copy(
                    officerAuthError = null,
                    bannerMessage = "HQ Officer Admin Session active. Edit controls unlocked."
                )
            }
        }
    }

    fun logoutOfficerAdmin() {
        viewModelScope.launch {
            repository.setOfficerMode(false)
            _ephemeral.update {
                it.copy(
                    officerAuthError = null,
                    bannerMessage = "Logged out of Officer Admin Session."
                )
            }
        }
    }

    fun updateOfficerPin(newPin: String?) {
        viewModelScope.launch {
            repository.setOfficerPin(newPin)
            _ephemeral.update {
                it.copy(
                    bannerMessage = if (newPin.isNullOrBlank()) {
                        "Local Officer PIN lock removed."
                    } else {
                        "Local Officer PIN updated and salted on-device."
                    }
                )
            }
        }
    }

    fun updateClubSettings(welcomeHeadline: String, clubStatusLabel: String, activeMemberAlias: String) {
        viewModelScope.launch {
            repository.updateHqSettings(welcomeHeadline, clubStatusLabel, activeMemberAlias)
            _ephemeral.update { it.copy(bannerMessage = "Club dashboard settings saved.") }
        }
    }

    // --- Members ---

    fun saveMember(
        existingId: String?,
        memberNumber: String,
        characterName: String,
        clubPosition: String,
        status: MemberStatus,
        biography: String,
        joinDate: String,
        customRankBadge: String,
        avatarUri: String,
        sortOrder: Int
    ) {
        viewModelScope.launch {
            val member = MemberProfile(
                id = existingId ?: "member-${UUID.randomUUID()}",
                memberNumber = memberNumber.trim().ifBlank { "JDMC-${(10..99).random()}" },
                characterName = characterName.trim(),
                clubPosition = clubPosition.trim(),
                status = status,
                biography = biography.trim(),
                joinDate = joinDate.trim().ifBlank { todayDateLabel() },
                customRankBadge = customRankBadge.trim().ifBlank { clubPosition.trim().uppercase(Locale.US) },
                avatarUri = avatarUri,
                sortOrder = sortOrder
            )
            repository.saveMember(member)
            _ephemeral.update { it.copy(bannerMessage = "Member profile saved: ${member.characterName}") }
        }
    }

    fun deleteMember(member: MemberProfile) {
        viewModelScope.launch {
            repository.deleteMember(member)
            _ephemeral.update { it.copy(bannerMessage = "Member removed: ${member.characterName}") }
        }
    }

    suspend fun importAvatarOrPosterImage(uri: Uri): String? =
        repository.importPickedImageUri(uri).getOrNull()

    // --- Organization Roles ---

    fun saveOrganizationRole(role: ClubOrganizationRole) {
        viewModelScope.launch {
            repository.saveOrganizationRole(role)
            _ephemeral.update { it.copy(bannerMessage = "Updated role: ${role.roleTitle}") }
        }
    }

    // --- Announcements ---

    fun saveAnnouncement(
        existingId: String?,
        title: String,
        content: String,
        category: AnnouncementCategory,
        isPinned: Boolean,
        authorName: String
    ) {
        viewModelScope.launch {
            val existing = existingId?.let { id -> uiState.value.announcements.firstOrNull { it.id == id } }
            val announcement = ClubAnnouncement(
                id = existingId ?: "ann-${UUID.randomUUID()}",
                title = title.trim(),
                content = content.trim(),
                category = category,
                isPinned = isPinned,
                authorName = authorName.trim().ifBlank { uiState.value.settings.activeMemberAlias },
                dateLabel = existing?.dateLabel ?: todayDateLabel(),
                createdAtMs = existing?.createdAtMs ?: System.currentTimeMillis()
            )
            repository.saveAnnouncement(announcement)
            _ephemeral.update { it.copy(bannerMessage = "Announcement published: ${announcement.title}") }
        }
    }

    fun toggleAnnouncementPin(announcement: ClubAnnouncement) {
        viewModelScope.launch {
            repository.toggleAnnouncementPin(announcement)
        }
    }

    fun deleteAnnouncement(announcement: ClubAnnouncement) {
        viewModelScope.launch {
            repository.deleteAnnouncement(announcement)
            _ephemeral.update { it.copy(bannerMessage = "Announcement deleted.") }
        }
    }

    // --- Events & Activities ---

    fun saveEvent(
        existingId: String?,
        title: String,
        description: String,
        dateTimeLabel: String,
        location: String,
        posterUri: String,
        status: EventStatus,
        attendees: List<String>
    ) {
        viewModelScope.launch {
            val existing = existingId?.let { id -> uiState.value.events.firstOrNull { it.id == id } }
            val event = ClubEvent(
                id = existingId ?: "evt-${UUID.randomUUID()}",
                title = title.trim(),
                description = description.trim(),
                dateTimeLabel = dateTimeLabel.trim(),
                location = location.trim(),
                posterUri = posterUri,
                status = status,
                attendees = attendees,
                createdAtMs = existing?.createdAtMs ?: System.currentTimeMillis()
            )
            repository.saveEvent(event)
            _ephemeral.update { it.copy(bannerMessage = "Event saved: ${event.title}") }
        }
    }

    fun toggleEventAttendance(event: ClubEvent, memberName: String = uiState.value.settings.activeMemberAlias) {
        viewModelScope.launch {
            repository.toggleEventAttendance(event, memberName)
            _ephemeral.update { it.copy(bannerMessage = "Attendance updated for ${event.title}") }
        }
    }

    fun toggleEventStatus(event: ClubEvent) {
        viewModelScope.launch {
            repository.toggleEventStatus(event)
        }
    }

    fun deleteEvent(event: ClubEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            _ephemeral.update { it.copy(bannerMessage = "Event deleted.") }
        }
    }

    // --- Media Gallery (Direct Photo/Video Upload & Save) ---

    fun savePickedMediaToGallery(
        pickerUri: Uri,
        title: String,
        caption: String,
        album: MediaAlbum,
        addedBy: String = uiState.value.settings.activeMemberAlias,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _ephemeral.update { it.copy(isImportingMedia = true, bannerMessage = null) }
            val result = repository.savePickedGalleryMedia(
                pickerUri = pickerUri,
                title = title,
                caption = caption,
                album = album,
                addedBy = addedBy
            )
            result.onSuccess { saved ->
                _ephemeral.update {
                    it.copy(
                        isImportingMedia = false,
                        bannerMessage = "${saved.mediaType.label} saved to ${saved.album.label}: ${saved.title}"
                    )
                }
                onComplete()
            }.onFailure { err ->
                _ephemeral.update {
                    it.copy(
                        isImportingMedia = false,
                        bannerMessage = err.message ?: "Failed to save selected gallery file."
                    )
                }
            }
        }
    }

    fun updateMediaAlbum(item: MediaGalleryItem, newAlbum: MediaAlbum) {
        viewModelScope.launch {
            repository.updateMediaItem(item.copy(album = newAlbum))
            _ephemeral.update { it.copy(bannerMessage = "Moved to ${newAlbum.label}.") }
        }
    }

    fun deleteMediaItem(item: MediaGalleryItem) {
        viewModelScope.launch {
            repository.deleteMediaItem(item)
            _ephemeral.update { it.copy(bannerMessage = "Removed media from gallery.") }
        }
    }

    // --- Digital Archive ---

    fun saveArchiveEntry(
        existingId: String?,
        section: ArchiveSection,
        yearEra: String,
        title: String,
        narrative: String,
        imageUri: String,
        sortOrder: Int
    ) {
        viewModelScope.launch {
            val entry = ArchiveEntry(
                id = existingId ?: "arch-${UUID.randomUUID()}",
                section = section,
                yearEra = yearEra.trim().ifBlank { "2026 • ARCHIVE" },
                title = title.trim(),
                narrative = narrative.trim(),
                imageUri = imageUri,
                sortOrder = sortOrder
            )
            repository.saveArchiveEntry(entry)
            _ephemeral.update { it.copy(bannerMessage = "Archive record saved: ${entry.title}") }
        }
    }

    fun deleteArchiveEntry(entry: ArchiveEntry) {
        viewModelScope.launch {
            repository.deleteArchiveEntry(entry)
            _ephemeral.update { it.copy(bannerMessage = "Archive record deleted.") }
        }
    }

    private fun currentClockSnapshot(): ClockSnapshot {
        val now = Date()
        val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.US)
        val dateFmt = SimpleDateFormat("EEE, dd MMM yyyy", Locale.US)
        return ClockSnapshot(
            timeText = timeFmt.format(now),
            dateText = dateFmt.format(now).uppercase(Locale.US)
        )
    }

    private fun todayDateLabel(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}
