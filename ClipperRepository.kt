package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.local.SessionPreferencesStore
import com.example.data.remote.AdminAuthRequest
import com.example.data.remote.ApiErrorResponse
import com.example.data.remote.CreateClipRequest
import com.example.data.remote.JabatanDto
import com.example.data.remote.JdmcApiService
import com.example.data.remote.ModerateClipRequest
import com.example.data.remote.RotateAccessCodeRequest
import com.example.data.remote.StreamerDto
import com.example.data.remote.UpdateClipRequest
import com.example.data.remote.UpdateJabatanRequest
import com.example.data.remote.UpdateStreamersRequest
import com.example.data.remote.VerifyAccessRequest
import com.example.domain.model.AdminSession
import com.example.domain.model.AuditLogEntry
import com.example.domain.model.BackendStatus
import com.example.domain.model.ClipSubmission
import com.example.domain.model.ClipperSession
import com.example.domain.model.JabatanPosition
import com.example.domain.model.ModerationAction
import com.example.domain.model.StreamerProfile
import com.example.domain.model.VideoPlatform
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class BackendUnavailable(val reason: String) : ApiResult<Nothing>
    data class Unauthorized(val message: String, val sessionRevokedOrExpired: Boolean = true) : ApiResult<Nothing>
    data class RateLimited(val retryAfterSeconds: Int, val message: String) : ApiResult<Nothing>
    data class Error(val message: String) : ApiResult<Nothing>
}

class ClipperRepository(
    private val appContext: Context,
    val sessionStore: SessionPreferencesStore
) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val errorAdapter = moshi.adapter(ApiErrorResponse::class.java)

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
                redactHeader("Authorization")
            }
        )
        .build()

    @Volatile
    private var cachedBaseUrl: String? = null

    @Volatile
    private var cachedService: JdmcApiService? = null

    val backendBaseUrlFlow: Flow<String> = sessionStore.backendBaseUrlFlow
    val clipperSessionFlow: Flow<ClipperSession?> = sessionStore.clipperSessionFlow
    val adminSessionFlow: Flow<AdminSession?> = sessionStore.adminSessionFlow

    private fun getOrCreateService(baseUrl: String): JdmcApiService? {
        val trimmed = baseUrl.trim()
        if (trimmed.isBlank() || !trimmed.startsWith("https://", ignoreCase = true)) {
            return null
        }
        val normalized = if (trimmed.endsWith("/")) trimmed else "$trimmed/"
        if (cachedBaseUrl == normalized && cachedService != null) {
            return cachedService
        }
        return try {
            val retrofit = Retrofit.Builder()
                .baseUrl(normalized)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            val service = retrofit.create(JdmcApiService::class.java)
            cachedBaseUrl = normalized
            cachedService = service
            service
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    suspend fun saveBackendBaseUrl(url: String) {
        cachedBaseUrl = null
        cachedService = null
        sessionStore.saveBackendBaseUrl(url)
    }

    suspend fun checkBackendHealth(): BackendStatus {
        val baseUrl = sessionStore.backendBaseUrlFlow.first().trim()
        if (baseUrl.isBlank()) {
            return BackendStatus.Unconfigured
        }
        val service = getOrCreateService(baseUrl)
            ?: return BackendStatus.Unreachable(baseUrl, "Backend URL must start with https:// and have a valid host.")
        return when (val result = executeApiCall { service.health() }) {
            is ApiResult.Success -> BackendStatus.Connected(baseUrl)
            is ApiResult.BackendUnavailable -> BackendStatus.Unreachable(baseUrl, result.reason)
            is ApiResult.Error -> BackendStatus.Unreachable(baseUrl, result.message)
            is ApiResult.RateLimited -> BackendStatus.Connected(baseUrl)
            is ApiResult.Unauthorized -> BackendStatus.Unreachable(baseUrl, result.message)
        }
    }

    suspend fun fetchPublishedClips(): ApiResult<List<ClipSubmission>> {
        return withService { service ->
            executeApiCall { service.getPublishedClips() }.mapSuccess { response ->
                response.clips.map { it.toDomain() }
            }
        }
    }

    suspend fun fetchStreamers(): ApiResult<List<StreamerProfile>> {
        return withService { service ->
            executeApiCall { service.getStreamers() }.mapSuccess { response ->
                response.streamers.map { it.toDomain() }
            }
        }
    }

    suspend fun fetchJabatanPositions(): ApiResult<List<JabatanPosition>> {
        return withService { service ->
            executeApiCall { service.getJabatanPositions() }.mapSuccess { response ->
                response.positions.map { it.toDomain() }.sortedBy { it.tierLevel }
            }
        }
    }

    suspend fun verifyAccessCode(accessCode: String): ApiResult<ClipperSession> {
        val trimmedCode = accessCode.trim()
        if (trimmedCode.isBlank()) {
            return ApiResult.Error("Please enter the JDMC Clipper Access Code.")
        }
        return withService { service ->
            when (val res = executeApiCall { service.verifyAccessCode(VerifyAccessRequest(trimmedCode)) }) {
                is ApiResult.Success -> {
                    val session = ClipperSession(
                        sessionToken = res.data.sessionToken,
                        expiresAtEpochMs = res.data.expiresAtEpochMs,
                        issuedAtEpochMs = res.data.issuedAtEpochMs,
                        ownershipReceipts = res.data.ownershipReceipts.toSet()
                    )
                    sessionStore.saveClipperSession(session)
                    ApiResult.Success(session)
                }
                is ApiResult.BackendUnavailable -> res
                is ApiResult.Unauthorized -> res
                is ApiResult.RateLimited -> res
                is ApiResult.Error -> res
            }
        }
    }

    suspend fun validateClipperSession(): ApiResult<Boolean> {
        val currentSession = sessionStore.clipperSessionFlow.first()
            ?: return ApiResult.Unauthorized("No active clipper session.")
        if (currentSession.expiresAtEpochMs <= System.currentTimeMillis()) {
            sessionStore.clearClipperSession()
            return ApiResult.Unauthorized("Clipper session has expired.")
        }
        return withService { service ->
            when (val res = executeApiCall { service.checkClipperSession("Bearer ${currentSession.sessionToken}") }) {
                is ApiResult.Success -> {
                    if (!res.data.valid || res.data.revoked) {
                        sessionStore.clearClipperSession()
                        ApiResult.Unauthorized("Access session was revoked or expired by an administrator.")
                    } else {
                        if (res.data.expiresAtEpochMs > 0L) {
                            sessionStore.saveClipperSession(
                                currentSession.copy(expiresAtEpochMs = res.data.expiresAtEpochMs)
                            )
                        }
                        ApiResult.Success(true)
                    }
                }
                is ApiResult.Unauthorized -> {
                    sessionStore.clearClipperSession()
                    res
                }
                is ApiResult.BackendUnavailable -> res
                is ApiResult.RateLimited -> res
                is ApiResult.Error -> res
            }
        }
    }

    suspend fun logoutClipper() {
        val session = sessionStore.clipperSessionFlow.first()
        if (session != null) {
            withService { service ->
                executeApiCall { service.logoutClipper("Bearer ${session.sessionToken}") }
            }
        }
        sessionStore.clearClipperSession()
    }

    suspend fun fetchMySubmissions(): ApiResult<List<ClipSubmission>> {
        val session = sessionStore.clipperSessionFlow.first()
            ?: return ApiResult.Unauthorized("Clipper session required.")
        return withService { service ->
            when (val res = executeApiCall { service.getMySubmissions("Bearer ${session.sessionToken}") }) {
                is ApiResult.Success -> ApiResult.Success(
                    res.data.clips.map { it.toDomain(session.ownershipReceipts) }
                )
                is ApiResult.Unauthorized -> {
                    sessionStore.clearClipperSession()
                    res
                }
                is ApiResult.BackendUnavailable -> res
                is ApiResult.RateLimited -> res
                is ApiResult.Error -> res
            }
        }
    }

    suspend fun uploadThumbnailUri(uri: Uri): ApiResult<String> = withContext(Dispatchers.IO) {
        val session = sessionStore.clipperSessionFlow.first()
            ?: return@withContext ApiResult.Unauthorized("Clipper session required.")
        withService { service ->
            try {
                val resolver = appContext.contentResolver
                val mimeType = resolver.getType(uri) ?: "image/jpeg"
                val fileName = queryFileName(uri) ?: "clip_thumbnail.jpg"
                val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withService ApiResult.Error("Unable to read selected thumbnail image.")
                if (bytes.size > 5 * 1024 * 1024) {
                    return@withService ApiResult.Error("Thumbnail image exceeds the 5 MB upload limit.")
                }
                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("thumbnail", fileName, requestBody)
                when (val res = executeApiCall { service.uploadThumbnail("Bearer ${session.sessionToken}", part) }) {
                    is ApiResult.Success -> ApiResult.Success(res.data.thumbnailUrl)
                    is ApiResult.Unauthorized -> {
                        sessionStore.clearClipperSession()
                        res
                    }
                    is ApiResult.BackendUnavailable -> res
                    is ApiResult.RateLimited -> res
                    is ApiResult.Error -> res
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ApiResult.Error(e.localizedMessage ?: "Thumbnail upload failed.")
            }
        }
    }

    suspend fun submitClip(
        clipperDisplayName: String,
        clipperJabatan: String,
        title: String,
        streamer: StreamerProfile,
        platform: VideoPlatform,
        videoUrl: String,
        thumbnailUrl: String,
        description: String,
        submittedAtIso: String
    ): ApiResult<ClipSubmission> {
        val session = sessionStore.clipperSessionFlow.first()
            ?: return ApiResult.Unauthorized("Active clipper session required.")
        return withService { service ->
            val req = CreateClipRequest(
                clipperDisplayName = clipperDisplayName.trim(),
                clipperJabatan = clipperJabatan.trim().ifBlank { "Official Clipper" },
                title = title.trim(),
                streamerId = streamer.id,
                streamerName = streamer.name,
                streamerJabatan = streamer.jabatan,
                platform = platform.apiValue,
                videoUrl = videoUrl.trim(),
                thumbnailUrl = thumbnailUrl.trim(),
                description = description.trim(),
                submittedAtIso = submittedAtIso
            )
            when (val res = executeApiCall { service.submitClip("Bearer ${session.sessionToken}", req) }) {
                is ApiResult.Success -> {
                    val receiptId = res.data.ownershipReceiptId ?: res.data.clip.id
                    sessionStore.addOwnershipReceipt(receiptId)
                    ApiResult.Success(res.data.clip.toDomain(session.ownershipReceipts + receiptId))
                }
                is ApiResult.Unauthorized -> {
                    sessionStore.clearClipperSession()
                    res
                }
                is ApiResult.BackendUnavailable -> res
                is ApiResult.RateLimited -> res
                is ApiResult.Error -> res
            }
        }
    }

    suspend fun updateOwnClip(
        clipId: String,
        clipperDisplayName: String,
        clipperJabatan: String,
        title: String,
        streamer: StreamerProfile,
        platform: VideoPlatform,
        videoUrl: String,
        thumbnailUrl: String,
        description: String
    ): ApiResult<ClipSubmission> {
        val session = sessionStore.clipperSessionFlow.first()
            ?: return ApiResult.Unauthorized("Active clipper session required.")
        return withService { service ->
            val req = UpdateClipRequest(
                clipperDisplayName = clipperDisplayName.trim(),
                clipperJabatan = clipperJabatan.trim().ifBlank { "Official Clipper" },
                title = title.trim(),
                streamerId = streamer.id,
                streamerName = streamer.name,
                streamerJabatan = streamer.jabatan,
                platform = platform.apiValue,
                videoUrl = videoUrl.trim(),
                thumbnailUrl = thumbnailUrl.trim(),
                description = description.trim()
            )
            when (val res = executeApiCall { service.updateClip("Bearer ${session.sessionToken}", clipId, req) }) {
                is ApiResult.Success -> ApiResult.Success(res.data.clip.toDomain(session.ownershipReceipts))
                is ApiResult.Unauthorized -> {
                    sessionStore.clearClipperSession()
                    res
                }
                is ApiResult.BackendUnavailable -> res
                is ApiResult.RateLimited -> res
                is ApiResult.Error -> res
            }
        }
    }

    suspend fun withdrawOwnClip(clipId: String): ApiResult<Unit> {
        val session = sessionStore.clipperSessionFlow.first()
            ?: return ApiResult.Unauthorized("Active clipper session required.")
        return withService { service ->
            when (val res = executeApiCall { service.withdrawClip("Bearer ${session.sessionToken}", clipId) }) {
                is ApiResult.Success -> {
                    sessionStore.removeOwnershipReceipt(clipId)
                    ApiResult.Success(Unit)
                }
                is ApiResult.Unauthorized -> {
                    sessionStore.clearClipperSession()
                    res
                }
                is ApiResult.BackendUnavailable -> res
                is ApiResult.RateLimited -> res
                is ApiResult.Error -> res
            }
        }
    }

    suspend fun authenticateAdmin(adminKey: String): ApiResult<AdminSession> {
        val trimmed = adminKey.trim()
        if (trimmed.isBlank()) {
            return ApiResult.Error("Enter the administrator authorization key.")
        }
        return withService { service ->
            when (val res = executeApiCall { service.authenticateAdmin(AdminAuthRequest(trimmed)) }) {
                is ApiResult.Success -> {
                    val session = AdminSession(
                        adminToken = res.data.adminToken,
                        expiresAtEpochMs = res.data.expiresAtEpochMs
                    )
                    sessionStore.saveAdminSession(session)
                    ApiResult.Success(session)
                }
                is ApiResult.BackendUnavailable -> res
                is ApiResult.Unauthorized -> res
                is ApiResult.RateLimited -> res
                is ApiResult.Error -> res
            }
        }
    }

    suspend fun logoutAdmin() {
        val session = sessionStore.adminSessionFlow.first()
        if (session != null) {
            withService { service ->
                executeApiCall { service.logoutAdmin("Bearer ${session.adminToken}") }
            }
        }
        sessionStore.clearAdminSession()
    }

    suspend fun fetchAdminSubmissions(): ApiResult<List<ClipSubmission>> {
        val session = sessionStore.adminSessionFlow.first()
            ?: return ApiResult.Unauthorized("Administrator authentication required.")
        return withService { service ->
            when (val res = executeApiCall { service.getAdminSubmissions("Bearer ${session.adminToken}") }) {
                is ApiResult.Success -> ApiResult.Success(res.data.clips.map { it.toDomain() })
                is ApiResult.Unauthorized -> {
                    sessionStore.clearAdminSession()
                    res
                }
                is ApiResult.BackendUnavailable -> res
                is ApiResult.RateLimited -> res
                is ApiResult.Error -> res
            }
        }
    }

    suspend fun moderateClip(
        clipId: String,
        action: ModerationAction,
        moderationNote: String? = null,
        streamer: StreamerProfile? = null,
        isFeatured: Boolean? = null
    ): ApiResult<ClipSubmission> {
        val session = sessionStore.adminSessionFlow.first()
            ?: return ApiResult.Unauthorized("Administrator authentication required.")
        return withService { service ->
            val req = ModerateClipRequest(
                action = action.apiValue,
                moderationNote = moderationNote?.trim()?.takeIf { it.isNotEmpty() },
                streamerId = streamer?.id,
                streamerName = streamer?.name,
                isFeatured = isFeatured
            )
            when (val res = executeApiCall { service.moderateClip("Bearer ${session.adminToken}", clipId, req) }) {
                is ApiResult.Success -> ApiResult.Success(res.data.clip.toDomain())
                is ApiResult.Unauthorized -> {
                    sessionStore.clearAdminSession()
                    res
                }
                is ApiResult.BackendUnavailable -> res
                is ApiResult.RateLimited -> res
                is ApiResult.Error -> res
            }
        }
    }

    suspend fun rotateSharedAccessCode(
        newAccessCode: String,
        revokeExistingSessions: Boolean
    ): ApiResult<Int> {
        val session = sessionStore.adminSessionFlow.first()
            ?: return ApiResult.Unauthorized("Administrator authentication required.")
        val trimmed = newAccessCode.trim()
        if (trimmed.length < 6) {
            return ApiResult.Error("New shared access code must be at least 6 characters.")
        }
        return withService { service ->
            val req = RotateAccessCodeRequest(
                newAccessCode = trimmed,
                revokeExistingSessions = revokeExistingSessions
            )
            when (val res = executeApiCall { service.rotateSharedAccessCode("Bearer ${session.adminToken}", req) }) {
                is ApiResult.Success -> {
                    if (revokeExistingSessions) {
                        sessionStore.clearClipperSession()
                    }
                    ApiResult.Success(res.data.revokedSessionsCount)
                }
                is ApiResult.Unauthorized -> {
                    sessionStore.clearAdminSession()
                    res
                }
                is ApiResult.BackendUnavailable -> res
                is ApiResult.RateLimited -> res
                is ApiResult.Error -> res
            }
        }
    }

    suspend fun revokeAllClipperSessions(): ApiResult<Int> {
        val session = sessionStore.adminSessionFlow.first()
            ?: return ApiResult.Unauthorized("Administrator authentication required.")
        return withService { service ->
            when (val res = executeApiCall { service.revokeAllClipperSessions("Bearer ${session.adminToken}") }) {
                is ApiResult.Success -> {
                    sessionStore.clearClipperSession()
                    ApiResult.Success(res.data.revokedCount)
                }
                is ApiResult.Unauthorized -> {
                    sessionStore.clearAdminSession()
                    res
                }
                is ApiResult.BackendUnavailable -> res
                is ApiResult.RateLimited -> res
                is ApiResult.Error -> res
            }
        }
    }

    suspend fun fetchAdminAuditLog(): ApiResult<List<AuditLogEntry>> {
        val session = sessionStore.adminSessionFlow.first()
            ?: return ApiResult.Unauthorized("Administrator authentication required.")
        return withService { service ->
            when (val res = executeApiCall { service.getAdminAuditLog("Bearer ${session.adminToken}") }) {
                is ApiResult.Success -> ApiResult.Success(res.data.entries.map { it.toDomain() })
                is ApiResult.Unauthorized -> {
                    sessionStore.clearAdminSession()
                    res
                }
                is ApiResult.BackendUnavailable -> res
                is ApiResult.RateLimited -> res
                is ApiResult.Error -> res
            }
        }
    }

    suspend fun updateStreamers(streamers: List<StreamerProfile>): ApiResult<List<StreamerProfile>> {
        val session = sessionStore.adminSessionFlow.first()
            ?: return ApiResult.Unauthorized("Administrator authentication required.")
        return withService { service ->
            val req = UpdateStreamersRequest(
                streamers = streamers.map {
                    StreamerDto(
                        id = it.id,
                        name = it.name,
                        channelHandle = it.channelHandle,
                        jabatan = it.jabatan,
                        specialty = it.specialty,
                        isActive = it.isActive
                    )
                }
            )
            when (val res = executeApiCall { service.updateStreamers("Bearer ${session.adminToken}", req) }) {
                is ApiResult.Success -> ApiResult.Success(res.data.streamers.map { it.toDomain() })
                is ApiResult.Unauthorized -> {
                    sessionStore.clearAdminSession()
                    res
                }
                is ApiResult.BackendUnavailable -> res
                is ApiResult.RateLimited -> res
                is ApiResult.Error -> res
            }
        }
    }

    suspend fun updateJabatanPositions(positions: List<JabatanPosition>): ApiResult<List<JabatanPosition>> {
        val session = sessionStore.adminSessionFlow.first()
            ?: return ApiResult.Unauthorized("Administrator authentication required.")
        return withService { service ->
            val req = UpdateJabatanRequest(
                positions = positions.map {
                    JabatanDto(
                        id = it.id,
                        rankTitle = it.rankTitle,
                        indonesianTitle = it.indonesianTitle,
                        tierLevel = it.tierLevel,
                        responsibilities = it.responsibilities,
                        isOfficerRole = it.isOfficerRole
                    )
                }
            )
            when (val res = executeApiCall { service.updateJabatanPositions("Bearer ${session.adminToken}", req) }) {
                is ApiResult.Success -> ApiResult.Success(res.data.positions.map { it.toDomain() }.sortedBy { it.tierLevel })
                is ApiResult.Unauthorized -> {
                    sessionStore.clearAdminSession()
                    res
                }
                is ApiResult.BackendUnavailable -> res
                is ApiResult.RateLimited -> res
                is ApiResult.Error -> res
            }
        }
    }

    private suspend fun <T> withService(block: suspend (JdmcApiService) -> ApiResult<T>): ApiResult<T> {
        val baseUrl = sessionStore.backendBaseUrlFlow.first().trim()
        if (baseUrl.isBlank()) {
            return ApiResult.BackendUnavailable(
                "No JDMC backend endpoint URL is configured. Configure your HTTPS server endpoint to validate access codes and sync clips."
            )
        }
        val service = getOrCreateService(baseUrl)
            ?: return ApiResult.BackendUnavailable(
                "Configured backend URL ($baseUrl) is invalid. It must use https:// with a valid host."
            )
        return block(service)
    }

    private suspend fun <T> executeApiCall(call: suspend () -> Response<T>): ApiResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = call()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        ApiResult.Success(body)
                    } else {
                        ApiResult.Error("Server returned an empty response (${response.code()}).")
                    }
                } else {
                    val rawError = response.errorBody()?.string()
                    val parsedError = rawError?.let {
                        try {
                            errorAdapter.fromJson(it)
                        } catch (_: Exception) {
                            null
                        }
                    }
                    val serverMessage = parsedError?.message ?: parsedError?.error
                    when (response.code()) {
                        401, 403 -> ApiResult.Unauthorized(
                            message = serverMessage ?: "Authentication failed, session expired, or access was revoked.",
                            sessionRevokedOrExpired = true
                        )
                        429 -> {
                            val headerRetry = response.headers()["Retry-After"]?.toIntOrNull()
                            val retrySeconds = (parsedError?.retryAfterSeconds ?: headerRetry ?: 60).coerceAtLeast(1)
                            ApiResult.RateLimited(
                                retryAfterSeconds = retrySeconds,
                                message = serverMessage ?: "Too many attempts. Please wait ${retrySeconds}s before trying again."
                            )
                        }
                        in 500..599 -> ApiResult.BackendUnavailable(
                            serverMessage ?: "Backend service error (${response.code()}). Please try again shortly."
                        )
                        else -> ApiResult.Error(
                            serverMessage ?: "Request failed with HTTP ${response.code()}."
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                ApiResult.BackendUnavailable(
                    "Unable to reach JDMC backend server: ${e.localizedMessage ?: "Network unreachable"}"
                )
            } catch (e: Exception) {
                ApiResult.Error(e.localizedMessage ?: "Unexpected network error.")
            }
        }
    }

    private fun <A, B> ApiResult<A>.mapSuccess(transform: (A) -> B): ApiResult<B> = when (this) {
        is ApiResult.Success -> ApiResult.Success(transform(data))
        is ApiResult.BackendUnavailable -> this
        is ApiResult.Unauthorized -> this
        is ApiResult.RateLimited -> this
        is ApiResult.Error -> this
    }

    private fun queryFileName(uri: Uri): String? {
        return appContext.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIdx >= 0 && cursor.moveToFirst()) cursor.getString(nameIdx) else null
        }
    }
}
