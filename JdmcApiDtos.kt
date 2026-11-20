package com.example.data.remote

import com.example.domain.model.AuditLogEntry
import com.example.domain.model.ClipSubmission
import com.example.domain.model.JabatanPosition
import com.example.domain.model.StreamerProfile
import com.example.domain.model.SubmissionStatus
import com.example.domain.model.VideoPlatform
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HealthResponse(
    val status: String = "ok",
    val version: String? = null
)

@JsonClass(generateAdapter = true)
data class VerifyAccessRequest(
    val accessCode: String
)

@JsonClass(generateAdapter = true)
data class VerifyAccessResponse(
    val sessionToken: String,
    val expiresAtEpochMs: Long,
    val issuedAtEpochMs: Long,
    val ownershipReceipts: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SessionStatusResponse(
    val valid: Boolean,
    val expiresAtEpochMs: Long = 0L,
    val revoked: Boolean = false
)

@JsonClass(generateAdapter = true)
data class BasicSuccessResponse(
    val success: Boolean = true,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class JabatanDto(
    val id: String,
    val rankTitle: String,
    val indonesianTitle: String = "",
    val tierLevel: Int = 9,
    val responsibilities: String = "",
    val isOfficerRole: Boolean = false
) {
    fun toDomain(): JabatanPosition = JabatanPosition(
        id = id,
        rankTitle = rankTitle,
        indonesianTitle = indonesianTitle,
        tierLevel = tierLevel,
        responsibilities = responsibilities,
        isOfficerRole = isOfficerRole
    )
}

@JsonClass(generateAdapter = true)
data class JabatanListResponse(
    val positions: List<JabatanDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UpdateJabatanRequest(
    val positions: List<JabatanDto>
)

@JsonClass(generateAdapter = true)
data class StreamerDto(
    val id: String,
    val name: String,
    val channelHandle: String = "",
    val jabatan: String = "Member",
    val specialty: String = "",
    val isActive: Boolean = true
) {
    fun toDomain(): StreamerProfile = StreamerProfile(
        id = id,
        name = name,
        channelHandle = channelHandle,
        jabatan = jabatan,
        specialty = specialty,
        isActive = isActive
    )
}

@JsonClass(generateAdapter = true)
data class StreamersResponse(
    val streamers: List<StreamerDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UpdateStreamersRequest(
    val streamers: List<StreamerDto>
)

@JsonClass(generateAdapter = true)
data class ClipDto(
    val id: String,
    val clipperDisplayName: String,
    val clipperJabatan: String = "Official Clipper",
    val title: String,
    val streamerId: String,
    val streamerName: String,
    val streamerJabatan: String = "",
    val platform: String,
    val videoUrl: String,
    val thumbnailUrl: String = "",
    val description: String = "",
    val submittedAtIso: String,
    val publishedAtIso: String? = null,
    val status: String = "PENDING_REVIEW",
    val isFeatured: Boolean = false,
    val moderationNote: String? = null,
    val canEditOrWithdraw: Boolean = false
) {
    fun toDomain(ownershipReceipts: Set<String> = emptySet()): ClipSubmission = ClipSubmission(
        id = id,
        clipperDisplayName = clipperDisplayName,
        clipperJabatan = clipperJabatan,
        title = title,
        streamerId = streamerId,
        streamerName = streamerName,
        streamerJabatan = streamerJabatan,
        platform = VideoPlatform.fromApi(platform),
        videoUrl = videoUrl,
        thumbnailUrl = thumbnailUrl,
        description = description,
        submittedAtIso = submittedAtIso,
        publishedAtIso = publishedAtIso,
        status = SubmissionStatus.fromApi(status),
        isFeatured = isFeatured,
        moderationNote = moderationNote,
        canEditOrWithdraw = canEditOrWithdraw || ownershipReceipts.contains(id)
    )
}

@JsonClass(generateAdapter = true)
data class ClipsResponse(
    val clips: List<ClipDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ClipResponse(
    val clip: ClipDto,
    val ownershipReceiptId: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateClipRequest(
    val clipperDisplayName: String,
    val clipperJabatan: String,
    val title: String,
    val streamerId: String,
    val streamerName: String,
    val streamerJabatan: String,
    val platform: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val description: String,
    val submittedAtIso: String
)

@JsonClass(generateAdapter = true)
data class UpdateClipRequest(
    val clipperDisplayName: String,
    val clipperJabatan: String,
    val title: String,
    val streamerId: String,
    val streamerName: String,
    val streamerJabatan: String,
    val platform: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val description: String
)

@JsonClass(generateAdapter = true)
data class ThumbnailUploadResponse(
    val thumbnailUrl: String
)

@JsonClass(generateAdapter = true)
data class AdminAuthRequest(
    val adminKey: String
)

@JsonClass(generateAdapter = true)
data class AdminAuthResponse(
    val adminToken: String,
    val expiresAtEpochMs: Long
)

@JsonClass(generateAdapter = true)
data class ModerateClipRequest(
    val action: String,
    val moderationNote: String? = null,
    val streamerId: String? = null,
    val streamerName: String? = null,
    val isFeatured: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class RotateAccessCodeRequest(
    val newAccessCode: String,
    val revokeExistingSessions: Boolean = true
)

@JsonClass(generateAdapter = true)
data class RotateAccessCodeResponse(
    val updatedAtIso: String,
    val revokedSessionsCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class RevokeSessionsResponse(
    val revokedCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class AuditLogDto(
    val id: String,
    val clipId: String,
    val clipTitle: String,
    val action: String,
    val actorRole: String,
    val note: String? = null,
    val timestampIso: String
) {
    fun toDomain(): AuditLogEntry = AuditLogEntry(
        id = id,
        clipId = clipId,
        clipTitle = clipTitle,
        action = action,
        actorRole = actorRole,
        note = note,
        timestampIso = timestampIso
    )
}

@JsonClass(generateAdapter = true)
data class AuditLogResponse(
    val entries: List<AuditLogDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ApiErrorResponse(
    val error: String? = null,
    val message: String? = null,
    val retryAfterSeconds: Int? = null
)
