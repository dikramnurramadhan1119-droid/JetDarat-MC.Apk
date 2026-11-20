package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SessionPreferencesStore
import com.example.data.repository.ApiResult
import com.example.data.repository.ClipperRepository
import com.example.domain.model.AdminSession
import com.example.domain.model.AuditLogEntry
import com.example.domain.model.BackendStatus
import com.example.domain.model.ClipSubmission
import com.example.domain.model.ClipperSession
import com.example.domain.model.DefaultJdmcJabatan
import com.example.domain.model.DefaultJdmcRoster
import com.example.domain.model.JabatanPosition
import com.example.domain.model.ModerationAction
import com.example.domain.model.StreamerProfile
import com.example.domain.model.UiState
import com.example.domain.model.VideoPlatform
import com.example.util.VideoUrlHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JdmcMainUiState(
    val backendBaseUrl: String = "",
    val backendStatus: BackendStatus = BackendStatus.Unconfigured,
    val publishedClipsState: UiState<List<ClipSubmission>> = UiState.Loading,
    val selectedPlatformFilter: VideoPlatform? = null,
    val selectedStreamerFilterId: String? = null,
    val gallerySearchQuery: String = "",
    val streamers: List<StreamerProfile> = DefaultJdmcRoster.initialStreamers,
    val jabatanPositions: List<JabatanPosition> = DefaultJdmcJabatan.initialPositions,
    val clipperSession: ClipperSession? = null,
    val clipperRemainingSeconds: Long = 0L,
    val clipperRateLimitSeconds: Int = 0,
    val isVerifyingClipperCode: Boolean = false,
    val clipperAuthMessage: String? = null,
    val clipperAuthIsUnavailable: Boolean = false,
    val mySubmissionsState: UiState<List<ClipSubmission>> = UiState.Idle,
    val isSubmittingClip: Boolean = false,
    val isUploadingThumbnail: Boolean = false,
    val clipperActionBanner: String? = null,
    val adminSession: AdminSession? = null,
    val adminRemainingSeconds: Long = 0L,
    val adminRateLimitSeconds: Int = 0,
    val isAuthenticatingAdmin: Boolean = false,
    val adminAuthMessage: String? = null,
    val adminAuthIsUnavailable: Boolean = false,
    val adminSubmissionsState: UiState<List<ClipSubmission>> = UiState.Idle,
    val adminAuditState: UiState<List<AuditLogEntry>> = UiState.Idle,
    val isAdminActionBusy: Boolean = false,
    val adminActionBanner: String? = null
)

class JdmcClipperViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionStore = SessionPreferencesStore(application.applicationContext)
    private val repository = ClipperRepository(application.applicationContext, sessionStore)

    private val _state = MutableStateFlow(JdmcMainUiState())
    val uiState: StateFlow<JdmcMainUiState> = _state.asStateFlow()

    val filteredPublishedClips: StateFlow<List<ClipSubmission>> = _state
        .combine(MutableStateFlow(Unit)) { current, _ ->
            val clips = (current.publishedClipsState as? UiState.Success)?.data.orEmpty()
            clips.filter { clip ->
                val matchesPlatform = current.selectedPlatformFilter == null || clip.platform == current.selectedPlatformFilter
                val matchesStreamer = current.selectedStreamerFilterId == null || clip.streamerId == current.selectedStreamerFilterId
                val q = current.gallerySearchQuery.trim()
                val matchesQuery = q.isEmpty() ||
                    clip.title.contains(q, ignoreCase = true) ||
                    clip.clipperDisplayName.contains(q, ignoreCase = true) ||
                    clip.streamerName.contains(q, ignoreCase = true) ||
                    clip.streamerJabatan.contains(q, ignoreCase = true)
                matchesPlatform && matchesStreamer && matchesQuery
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    init {
        observeStoredPreferences()
        startSessionAndCooldownTicker()
        refreshConnectionAndPublicData()
    }

    private fun observeStoredPreferences() {
        viewModelScope.launch {
            repository.backendBaseUrlFlow.collect { url ->
                _state.update { it.copy(backendBaseUrl = url) }
            }
        }
        viewModelScope.launch {
            repository.clipperSessionFlow.collect { session ->
                val remaining = session?.let {
                    ((it.expiresAtEpochMs - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
                } ?: 0L
                _state.update {
                    it.copy(
                        clipperSession = session,
                        clipperRemainingSeconds = remaining
                    )
                }
                if (session != null) {
                    refreshMySubmissions()
                } else {
                    _state.update { it.copy(mySubmissionsState = UiState.Idle) }
                }
            }
        }
        viewModelScope.launch {
            repository.adminSessionFlow.collect { session ->
                val remaining = session?.let {
                    ((it.expiresAtEpochMs - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
                } ?: 0L
                _state.update {
                    it.copy(
                        adminSession = session,
                        adminRemainingSeconds = remaining
                    )
                }
                if (session != null) {
                    refreshAdminWorkspace()
                } else {
                    _state.update {
                        it.copy(
                            adminSubmissionsState = UiState.Idle,
                            adminAuditState = UiState.Idle
                        )
                    }
                }
            }
        }
    }

    private fun startSessionAndCooldownTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val now = System.currentTimeMillis()
                val current = _state.value

                var expiredClipper = false
                val nextClipperRemaining = current.clipperSession?.let { s ->
                    val rem = ((s.expiresAtEpochMs - now) / 1000L)
                    if (rem <= 0L) {
                        expiredClipper = true
                        0L
                    } else rem
                } ?: 0L

                var expiredAdmin = false
                val nextAdminRemaining = current.adminSession?.let { s ->
                    val rem = ((s.expiresAtEpochMs - now) / 1000L)
                    if (rem <= 0L) {
                        expiredAdmin = true
                        0L
                    } else rem
                } ?: 0L

                if (expiredClipper) {
                    sessionStore.clearClipperSession()
                }
                if (expiredAdmin) {
                    sessionStore.clearAdminSession()
                }

                _state.update {
                    it.copy(
                        clipperRemainingSeconds = nextClipperRemaining,
                        adminRemainingSeconds = nextAdminRemaining,
                        clipperRateLimitSeconds = (it.clipperRateLimitSeconds - 1).coerceAtLeast(0),
                        adminRateLimitSeconds = (it.adminRateLimitSeconds - 1).coerceAtLeast(0),
                        clipperAuthMessage = if (expiredClipper) "Your Clipper Room session expired. Enter the shared access code again." else it.clipperAuthMessage,
                        adminAuthMessage = if (expiredAdmin) "Your Admin session expired. Re-authenticate to continue moderating." else it.adminAuthMessage
                    )
                }
            }
        }
    }

    fun updateBackendBaseUrl(newUrl: String) {
        viewModelScope.launch {
            repository.saveBackendBaseUrl(newUrl)
            refreshConnectionAndPublicData()
        }
    }

    fun refreshConnectionAndPublicData() {
        viewModelScope.launch {
            val baseUrl = _state.value.backendBaseUrl
            if (baseUrl.isNotBlank()) {
                _state.update { it.copy(backendStatus = BackendStatus.Checking(baseUrl)) }
            }
            val health = repository.checkBackendHealth()
            _state.update { it.copy(backendStatus = health) }
            refreshPublicGallery()
            refreshStreamers()
            refreshJabatanPositions()
            if (_state.value.clipperSession != null && health.isAvailable) {
                validateClipperSessionWithServer()
            }
        }
    }

    fun refreshPublicGallery() {
        viewModelScope.launch {
            _state.update { it.copy(publishedClipsState = UiState.Loading) }
            when (val res = repository.fetchPublishedClips()) {
                is ApiResult.Success -> _state.update {
                    it.copy(publishedClipsState = UiState.Success(res.data))
                }
                is ApiResult.BackendUnavailable -> _state.update {
                    it.copy(publishedClipsState = UiState.BackendUnavailable(res.reason))
                }
                is ApiResult.Unauthorized -> _state.update {
                    it.copy(publishedClipsState = UiState.Error(res.message))
                }
                is ApiResult.RateLimited -> _state.update {
                    it.copy(publishedClipsState = UiState.Error(res.message))
                }
                is ApiResult.Error -> _state.update {
                    it.copy(publishedClipsState = UiState.Error(res.message))
                }
            }
        }
    }

    fun refreshStreamers() {
        viewModelScope.launch {
            when (val res = repository.fetchStreamers()) {
                is ApiResult.Success -> {
                    if (res.data.isNotEmpty()) {
                        _state.update { it.copy(streamers = res.data) }
                    }
                }
                else -> Unit
            }
        }
    }

    fun refreshJabatanPositions() {
        viewModelScope.launch {
            when (val res = repository.fetchJabatanPositions()) {
                is ApiResult.Success -> {
                    if (res.data.isNotEmpty()) {
                        _state.update { it.copy(jabatanPositions = res.data) }
                    }
                }
                else -> Unit
            }
        }
    }

    fun setPlatformFilter(platform: VideoPlatform?) {
        _state.update { it.copy(selectedPlatformFilter = platform) }
    }

    fun setStreamerFilter(streamerId: String?) {
        _state.update { it.copy(selectedStreamerFilterId = streamerId) }
    }

    fun setGallerySearchQuery(query: String) {
        _state.update { it.copy(gallerySearchQuery = query) }
    }

    fun verifyClipperAccessCode(accessCode: String, onVerified: () -> Unit = {}) {
        if (_state.value.clipperRateLimitSeconds > 0) return
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isVerifyingClipperCode = true,
                    clipperAuthMessage = null,
                    clipperAuthIsUnavailable = false
                )
            }
            when (val res = repository.verifyAccessCode(accessCode)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isVerifyingClipperCode = false,
                            clipperAuthMessage = null,
                            clipperAuthIsUnavailable = false,
                            backendStatus = BackendStatus.Connected(_state.value.backendBaseUrl)
                        )
                    }
                    refreshMySubmissions()
                    onVerified()
                }
                is ApiResult.BackendUnavailable -> _state.update {
                    it.copy(
                        isVerifyingClipperCode = false,
                        clipperAuthMessage = res.reason,
                        clipperAuthIsUnavailable = true
                    )
                }
                is ApiResult.RateLimited -> _state.update {
                    it.copy(
                        isVerifyingClipperCode = false,
                        clipperRateLimitSeconds = res.retryAfterSeconds,
                        clipperAuthMessage = res.message,
                        clipperAuthIsUnavailable = false
                    )
                }
                is ApiResult.Unauthorized -> _state.update {
                    it.copy(
                        isVerifyingClipperCode = false,
                        clipperAuthMessage = res.message,
                        clipperAuthIsUnavailable = false
                    )
                }
                is ApiResult.Error -> _state.update {
                    it.copy(
                        isVerifyingClipperCode = false,
                        clipperAuthMessage = res.message,
                        clipperAuthIsUnavailable = false
                    )
                }
            }
        }
    }

    fun validateClipperSessionWithServer() {
        viewModelScope.launch {
            when (val res = repository.validateClipperSession()) {
                is ApiResult.Unauthorized -> {
                    _state.update {
                        it.copy(
                            clipperAuthMessage = res.message,
                            clipperAuthIsUnavailable = false
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    fun logoutClipper() {
        viewModelScope.launch {
            repository.logoutClipper()
            _state.update {
                it.copy(
                    clipperAuthMessage = "You have been signed out of the Clipper Room.",
                    clipperActionBanner = null
                )
            }
        }
    }

    fun refreshMySubmissions() {
        viewModelScope.launch {
            _state.update { it.copy(mySubmissionsState = UiState.Loading) }
            when (val res = repository.fetchMySubmissions()) {
                is ApiResult.Success -> _state.update {
                    it.copy(mySubmissionsState = UiState.Success(res.data))
                }
                is ApiResult.BackendUnavailable -> _state.update {
                    it.copy(mySubmissionsState = UiState.BackendUnavailable(res.reason))
                }
                is ApiResult.Unauthorized -> _state.update {
                    it.copy(
                        mySubmissionsState = UiState.Error(res.message),
                        clipperAuthMessage = res.message
                    )
                }
                is ApiResult.RateLimited -> _state.update {
                    it.copy(mySubmissionsState = UiState.Error(res.message))
                }
                is ApiResult.Error -> _state.update {
                    it.copy(mySubmissionsState = UiState.Error(res.message))
                }
            }
        }
    }

    fun uploadThumbnail(uri: Uri, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isUploadingThumbnail = true, clipperActionBanner = null) }
            when (val res = repository.uploadThumbnailUri(uri)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isUploadingThumbnail = false,
                            clipperActionBanner = "Thumbnail uploaded to JDMC media storage."
                        )
                    }
                    onSuccess(res.data)
                }
                is ApiResult.BackendUnavailable -> _state.update {
                    it.copy(isUploadingThumbnail = false, clipperActionBanner = res.reason)
                }
                is ApiResult.Unauthorized -> _state.update {
                    it.copy(isUploadingThumbnail = false, clipperAuthMessage = res.message)
                }
                is ApiResult.RateLimited -> _state.update {
                    it.copy(isUploadingThumbnail = false, clipperActionBanner = res.message)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isUploadingThumbnail = false, clipperActionBanner = res.message)
                }
            }
        }
    }

    fun submitNewClip(
        clipperDisplayName: String,
        clipperJabatan: String,
        title: String,
        streamer: StreamerProfile,
        platform: VideoPlatform,
        videoUrl: String,
        thumbnailUrl: String,
        description: String,
        onSubmitted: () -> Unit
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmittingClip = true, clipperActionBanner = null) }
            val submittedAtIso = VideoUrlHelper.currentIsoTimestamp()
            when (
                val res = repository.submitClip(
                    clipperDisplayName = clipperDisplayName,
                    clipperJabatan = clipperJabatan,
                    title = title,
                    streamer = streamer,
                    platform = platform,
                    videoUrl = videoUrl,
                    thumbnailUrl = thumbnailUrl,
                    description = description,
                    submittedAtIso = submittedAtIso
                )
            ) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isSubmittingClip = false,
                            clipperActionBanner = "Clip submitted for review (${res.data.title})."
                        )
                    }
                    refreshMySubmissions()
                    onSubmitted()
                }
                is ApiResult.BackendUnavailable -> _state.update {
                    it.copy(isSubmittingClip = false, clipperActionBanner = res.reason)
                }
                is ApiResult.Unauthorized -> _state.update {
                    it.copy(isSubmittingClip = false, clipperAuthMessage = res.message)
                }
                is ApiResult.RateLimited -> _state.update {
                    it.copy(isSubmittingClip = false, clipperActionBanner = res.message)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isSubmittingClip = false, clipperActionBanner = res.message)
                }
            }
        }
    }

    fun updateOwnClip(
        clipId: String,
        clipperDisplayName: String,
        clipperJabatan: String,
        title: String,
        streamer: StreamerProfile,
        platform: VideoPlatform,
        videoUrl: String,
        thumbnailUrl: String,
        description: String,
        onUpdated: () -> Unit
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmittingClip = true, clipperActionBanner = null) }
            when (
                val res = repository.updateOwnClip(
                    clipId = clipId,
                    clipperDisplayName = clipperDisplayName,
                    clipperJabatan = clipperJabatan,
                    title = title,
                    streamer = streamer,
                    platform = platform,
                    videoUrl = videoUrl,
                    thumbnailUrl = thumbnailUrl,
                    description = description
                )
            ) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isSubmittingClip = false,
                            clipperActionBanner = "Submission updated."
                        )
                    }
                    refreshMySubmissions()
                    onUpdated()
                }
                is ApiResult.BackendUnavailable -> _state.update {
                    it.copy(isSubmittingClip = false, clipperActionBanner = res.reason)
                }
                is ApiResult.Unauthorized -> _state.update {
                    it.copy(isSubmittingClip = false, clipperAuthMessage = res.message)
                }
                is ApiResult.RateLimited -> _state.update {
                    it.copy(isSubmittingClip = false, clipperActionBanner = res.message)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isSubmittingClip = false, clipperActionBanner = res.message)
                }
            }
        }
    }

    fun withdrawOwnClip(clipId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmittingClip = true, clipperActionBanner = null) }
            when (val res = repository.withdrawOwnClip(clipId)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isSubmittingClip = false,
                            clipperActionBanner = "Submission withdrawn."
                        )
                    }
                    refreshMySubmissions()
                }
                is ApiResult.BackendUnavailable -> _state.update {
                    it.copy(isSubmittingClip = false, clipperActionBanner = res.reason)
                }
                is ApiResult.Unauthorized -> _state.update {
                    it.copy(isSubmittingClip = false, clipperAuthMessage = res.message)
                }
                is ApiResult.RateLimited -> _state.update {
                    it.copy(isSubmittingClip = false, clipperActionBanner = res.message)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isSubmittingClip = false, clipperActionBanner = res.message)
                }
            }
        }
    }

    fun clearClipperBanner() {
        _state.update { it.copy(clipperActionBanner = null) }
    }

    fun authenticateAdmin(adminKey: String) {
        if (_state.value.adminRateLimitSeconds > 0) return
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isAuthenticatingAdmin = true,
                    adminAuthMessage = null,
                    adminAuthIsUnavailable = false
                )
            }
            when (val res = repository.authenticateAdmin(adminKey)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isAuthenticatingAdmin = false,
                            adminAuthMessage = null,
                            adminAuthIsUnavailable = false
                        )
                    }
                    refreshAdminWorkspace()
                }
                is ApiResult.BackendUnavailable -> _state.update {
                    it.copy(
                        isAuthenticatingAdmin = false,
                        adminAuthMessage = res.reason,
                        adminAuthIsUnavailable = true
                    )
                }
                is ApiResult.RateLimited -> _state.update {
                    it.copy(
                        isAuthenticatingAdmin = false,
                        adminRateLimitSeconds = res.retryAfterSeconds,
                        adminAuthMessage = res.message,
                        adminAuthIsUnavailable = false
                    )
                }
                is ApiResult.Unauthorized -> _state.update {
                    it.copy(
                        isAuthenticatingAdmin = false,
                        adminAuthMessage = res.message,
                        adminAuthIsUnavailable = false
                    )
                }
                is ApiResult.Error -> _state.update {
                    it.copy(
                        isAuthenticatingAdmin = false,
                        adminAuthMessage = res.message,
                        adminAuthIsUnavailable = false
                    )
                }
            }
        }
    }

    fun logoutAdmin() {
        viewModelScope.launch {
            repository.logoutAdmin()
            _state.update {
                it.copy(
                    adminAuthMessage = "Administrator session ended.",
                    adminActionBanner = null
                )
            }
        }
    }

    fun refreshAdminWorkspace() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    adminSubmissionsState = UiState.Loading,
                    adminAuditState = UiState.Loading
                )
            }
            when (val subRes = repository.fetchAdminSubmissions()) {
                is ApiResult.Success -> _state.update {
                    it.copy(adminSubmissionsState = UiState.Success(subRes.data))
                }
                is ApiResult.BackendUnavailable -> _state.update {
                    it.copy(adminSubmissionsState = UiState.BackendUnavailable(subRes.reason))
                }
                is ApiResult.Unauthorized -> _state.update {
                    it.copy(
                        adminSubmissionsState = UiState.Error(subRes.message),
                        adminAuthMessage = subRes.message
                    )
                }
                is ApiResult.RateLimited -> _state.update {
                    it.copy(adminSubmissionsState = UiState.Error(subRes.message))
                }
                is ApiResult.Error -> _state.update {
                    it.copy(adminSubmissionsState = UiState.Error(subRes.message))
                }
            }
            when (val auditRes = repository.fetchAdminAuditLog()) {
                is ApiResult.Success -> _state.update {
                    it.copy(adminAuditState = UiState.Success(auditRes.data))
                }
                is ApiResult.BackendUnavailable -> _state.update {
                    it.copy(adminAuditState = UiState.BackendUnavailable(auditRes.reason))
                }
                is ApiResult.Unauthorized -> _state.update {
                    it.copy(adminAuditState = UiState.Error(auditRes.message))
                }
                is ApiResult.RateLimited -> _state.update {
                    it.copy(adminAuditState = UiState.Error(auditRes.message))
                }
                is ApiResult.Error -> _state.update {
                    it.copy(adminAuditState = UiState.Error(auditRes.message))
                }
            }
        }
    }

    fun moderateClip(
        clipId: String,
        action: ModerationAction,
        note: String? = null,
        streamer: StreamerProfile? = null,
        isFeatured: Boolean? = null
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isAdminActionBusy = true, adminActionBanner = null) }
            when (val res = repository.moderateClip(clipId, action, note, streamer, isFeatured)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isAdminActionBusy = false,
                            adminActionBanner = "${action.displayLabel} applied to \"${res.data.title}\"."
                        )
                    }
                    refreshAdminWorkspace()
                    refreshPublicGallery()
                }
                is ApiResult.BackendUnavailable -> _state.update {
                    it.copy(isAdminActionBusy = false, adminActionBanner = res.reason)
                }
                is ApiResult.Unauthorized -> _state.update {
                    it.copy(isAdminActionBusy = false, adminAuthMessage = res.message)
                }
                is ApiResult.RateLimited -> _state.update {
                    it.copy(isAdminActionBusy = false, adminActionBanner = res.message)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isAdminActionBusy = false, adminActionBanner = res.message)
                }
            }
        }
    }

    fun rotateSharedAccessCode(newCode: String, revokeExisting: Boolean, onRotated: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isAdminActionBusy = true, adminActionBanner = null) }
            when (val res = repository.rotateSharedAccessCode(newCode, revokeExisting)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isAdminActionBusy = false,
                            adminActionBanner = "Shared Clipper Access Code updated. Revoked ${res.data} active session(s)."
                        )
                    }
                    refreshAdminWorkspace()
                    onRotated()
                }
                is ApiResult.BackendUnavailable -> _state.update {
                    it.copy(isAdminActionBusy = false, adminActionBanner = res.reason)
                }
                is ApiResult.Unauthorized -> _state.update {
                    it.copy(isAdminActionBusy = false, adminAuthMessage = res.message)
                }
                is ApiResult.RateLimited -> _state.update {
                    it.copy(isAdminActionBusy = false, adminActionBanner = res.message)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isAdminActionBusy = false, adminActionBanner = res.message)
                }
            }
        }
    }

    fun revokeAllClipperSessions() {
        viewModelScope.launch {
            _state.update { it.copy(isAdminActionBusy = true, adminActionBanner = null) }
            when (val res = repository.revokeAllClipperSessions()) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isAdminActionBusy = false,
                            adminActionBanner = "Revoked ${res.data} active clipper session(s)."
                        )
                    }
                    refreshAdminWorkspace()
                }
                is ApiResult.BackendUnavailable -> _state.update {
                    it.copy(isAdminActionBusy = false, adminActionBanner = res.reason)
                }
                is ApiResult.Unauthorized -> _state.update {
                    it.copy(isAdminActionBusy = false, adminAuthMessage = res.message)
                }
                is ApiResult.RateLimited -> _state.update {
                    it.copy(isAdminActionBusy = false, adminActionBanner = res.message)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isAdminActionBusy = false, adminActionBanner = res.message)
                }
            }
        }
    }

    fun saveStreamerRoster(streamers: List<StreamerProfile>) {
        viewModelScope.launch {
            _state.update { it.copy(isAdminActionBusy = true, adminActionBanner = null) }
            when (val res = repository.updateStreamers(streamers)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isAdminActionBusy = false,
                            streamers = res.data,
                            adminActionBanner = "JDMC streamer & member roster updated (${res.data.size} entries)."
                        )
                    }
                    refreshAdminWorkspace()
                }
                is ApiResult.BackendUnavailable -> _state.update {
                    it.copy(isAdminActionBusy = false, adminActionBanner = res.reason)
                }
                is ApiResult.Unauthorized -> _state.update {
                    it.copy(isAdminActionBusy = false, adminAuthMessage = res.message)
                }
                is ApiResult.RateLimited -> _state.update {
                    it.copy(isAdminActionBusy = false, adminActionBanner = res.message)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isAdminActionBusy = false, adminActionBanner = res.message)
                }
            }
        }
    }

    fun saveJabatanPositions(positions: List<JabatanPosition>) {
        viewModelScope.launch {
            _state.update { it.copy(isAdminActionBusy = true, adminActionBanner = null) }
            when (val res = repository.updateJabatanPositions(positions)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isAdminActionBusy = false,
                            jabatanPositions = res.data,
                            adminActionBanner = "Struktur Jabatan JDMC updated (${res.data.size} positions)."
                        )
                    }
                    refreshAdminWorkspace()
                }
                is ApiResult.BackendUnavailable -> _state.update {
                    it.copy(isAdminActionBusy = false, adminActionBanner = res.reason)
                }
                is ApiResult.Unauthorized -> _state.update {
                    it.copy(isAdminActionBusy = false, adminAuthMessage = res.message)
                }
                is ApiResult.RateLimited -> _state.update {
                    it.copy(isAdminActionBusy = false, adminActionBanner = res.message)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isAdminActionBusy = false, adminActionBanner = res.message)
                }
            }
        }
    }

    fun clearAdminBanner() {
        _state.update { it.copy(adminActionBanner = null) }
    }
}
