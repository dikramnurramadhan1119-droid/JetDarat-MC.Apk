package com.example.domain.model

enum class VideoPlatform(
    val apiValue: String,
    val displayName: String,
    val supportsEmbeddedPlayer: Boolean
) {
    TIKTOK("TIKTOK", "TikTok", false),
    YOUTUBE("YOUTUBE", "YouTube", true),
    YOUTUBE_SHORTS("YOUTUBE_SHORTS", "YouTube Shorts", true),
    OTHER("OTHER", "Other Approved", false);

    companion object {
        fun fromApi(raw: String?): VideoPlatform =
            entries.firstOrNull { it.apiValue.equals(raw, ignoreCase = true) || it.displayName.equals(raw, ignoreCase = true) }
                ?: OTHER
    }
}

enum class SubmissionStatus(
    val apiValue: String,
    val displayName: String
) {
    PENDING_REVIEW("PENDING_REVIEW", "Pending Review"),
    APPROVED("APPROVED", "Approved"),
    REJECTED("REJECTED", "Rejected"),
    PUBLISHED("PUBLISHED", "Published");

    companion object {
        fun fromApi(raw: String?): SubmissionStatus =
            entries.firstOrNull { it.apiValue.equals(raw, ignoreCase = true) || it.displayName.equals(raw, ignoreCase = true) }
                ?: PENDING_REVIEW
    }
}

enum class ModerationAction(val apiValue: String, val displayLabel: String) {
    APPROVE("APPROVE", "Approve"),
    REJECT("REJECT", "Reject"),
    PUBLISH("PUBLISH", "Publish"),
    FEATURE("FEATURE", "Feature"),
    UNFEATURE("UNFEATURE", "Unfeature"),
    REMOVE_PUBLISHED("REMOVE_PUBLISHED", "Remove Published"),
    REASSIGN_STREAMER("REASSIGN_STREAMER", "Reassign Streamer")
}

data class JabatanPosition(
    val id: String,
    val rankTitle: String,
    val indonesianTitle: String,
    val tierLevel: Int,
    val responsibilities: String,
    val isOfficerRole: Boolean = true
)

object DefaultJdmcJabatan {
    val initialPositions: List<JabatanPosition> = listOf(
        JabatanPosition("president", "President", "Ketua Umum", 1, "Supreme chapter leader of Jet Darat Motorcycle Club Los Santos.", true),
        JabatanPosition("vice-president", "Vice President", "Wakil Ketua", 2, "Second-in-command coordinating daily chapter operations.", true),
        JabatanPosition("executive-officers", "Executive Officers", "Pengurus Inti", 3, "Senior executive leadership and founding elders council.", true),
        JabatanPosition("road-captain", "Road Captain", "Komandan Touring", 4, "Plans convoy routes and Los Santos formation rides.", true),
        JabatanPosition("sergeant-at-arms", "Sergeant at Arms", "Keamanan & Disiplin", 5, "Protects club discipline, brotherhood rules, and escort security.", true),
        JabatanPosition("treasurer", "Treasurer", "Bendahara", 6, "Manages club treasury, event logistics, and attributes.", true),
        JabatanPosition("secretary", "Secretary", "Sekretaris", 7, "Maintains official rosters, archives, and chapter dispatches.", true),
        JabatanPosition("full-members", "Full Members", "Anggota Penuh", 8, "Patched riders & Official Clippers contributing to JDMC.", false),
        JabatanPosition("prospects", "Prospects", "Calon Anggota", 9, "Probationary riders proving loyalty on chapter runs.", false),
        JabatanPosition("veterans", "Veterans", "Veteran Charter", 10, "Honored founding elders and Charter Veterans of JDMC.", false)
    )
}

data class StreamerProfile(
    val id: String,
    val name: String,
    val channelHandle: String,
    val jabatan: String = "Full Members",
    val specialty: String = "",
    val isActive: Boolean = true
)

object DefaultJdmcRoster {
    val initialStreamers: List<StreamerProfile> = listOf(
        StreamerProfile("gashima", "Gashima", "@gashima.jdmc", "President", "President Broadcast & Club Summits", true),
        StreamerProfile("melody", "Melody", "@melody.jdmc", "Vice President", "Chapter Ops & Midnight Runs", true),
        StreamerProfile("crystal-oda-megumi", "Crystal Oda Megumi", "@crystal.jdmc", "The Mother of JDMC", "Heritage & Brotherhood Council", true),
        StreamerProfile("joko-sancez", "Joko Sancez", "@jokosancez.jdmc", "Charter Veteran", "2-Tak Builds & Veteran Escort", true),
        StreamerProfile("tashima", "Tashima", "@tashima.jdmc", "JDMC Member • Duo Tachibana", "Tandem Highway Vanguard", true),
        StreamerProfile("gonzales", "Gonzales", "@gonzales.jdmc", "JDMC Member • Duo Tachibana", "Tandem Highway Vanguard", true)
    )
}

data class ClipSubmission(
    val id: String,
    val clipperDisplayName: String,
    val clipperJabatan: String = "Official Clipper",
    val title: String,
    val streamerId: String,
    val streamerName: String,
    val streamerJabatan: String = "",
    val platform: VideoPlatform,
    val videoUrl: String,
    val thumbnailUrl: String,
    val description: String,
    val submittedAtIso: String,
    val publishedAtIso: String?,
    val status: SubmissionStatus,
    val isFeatured: Boolean,
    val moderationNote: String?,
    val canEditOrWithdraw: Boolean
)

data class ClipperSession(
    val sessionToken: String,
    val expiresAtEpochMs: Long,
    val issuedAtEpochMs: Long,
    val ownershipReceipts: Set<String> = emptySet()
)

data class AdminSession(
    val adminToken: String,
    val expiresAtEpochMs: Long
)

data class AuditLogEntry(
    val id: String,
    val clipId: String,
    val clipTitle: String,
    val action: String,
    val actorRole: String,
    val note: String?,
    val timestampIso: String
)

sealed interface BackendStatus {
    data object Unconfigured : BackendStatus
    data class Checking(val baseUrl: String) : BackendStatus
    data class Connected(val baseUrl: String) : BackendStatus
    data class Unreachable(val baseUrl: String, val reason: String) : BackendStatus

    val isAvailable: Boolean
        get() = this is Connected
}

sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class BackendUnavailable(val reason: String) : UiState<Nothing>
    data class Error(val message: String) : UiState<Nothing>
}
