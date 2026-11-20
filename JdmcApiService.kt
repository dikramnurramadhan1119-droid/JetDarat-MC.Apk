package com.example.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface JdmcApiService {

    @GET("api/v1/health")
    suspend fun health(): Response<HealthResponse>

    @GET("api/v1/clips/published")
    suspend fun getPublishedClips(): Response<ClipsResponse>

    @GET("api/v1/streamers")
    suspend fun getStreamers(): Response<StreamersResponse>

    @GET("api/v1/jabatan")
    suspend fun getJabatanPositions(): Response<JabatanListResponse>

    @POST("api/v1/access/verify")
    suspend fun verifyAccessCode(
        @Body request: VerifyAccessRequest
    ): Response<VerifyAccessResponse>

    @GET("api/v1/access/session")
    suspend fun checkClipperSession(
        @Header("Authorization") bearerToken: String
    ): Response<SessionStatusResponse>

    @POST("api/v1/access/logout")
    suspend fun logoutClipper(
        @Header("Authorization") bearerToken: String
    ): Response<BasicSuccessResponse>

    @GET("api/v1/clips/mine")
    suspend fun getMySubmissions(
        @Header("Authorization") bearerToken: String
    ): Response<ClipsResponse>

    @Multipart
    @POST("api/v1/media/thumbnail")
    suspend fun uploadThumbnail(
        @Header("Authorization") bearerToken: String,
        @Part file: MultipartBody.Part
    ): Response<ThumbnailUploadResponse>

    @POST("api/v1/clips")
    suspend fun submitClip(
        @Header("Authorization") bearerToken: String,
        @Body request: CreateClipRequest
    ): Response<ClipResponse>

    @PATCH("api/v1/clips/{id}")
    suspend fun updateClip(
        @Header("Authorization") bearerToken: String,
        @Path("id") clipId: String,
        @Body request: UpdateClipRequest
    ): Response<ClipResponse>

    @DELETE("api/v1/clips/{id}")
    suspend fun withdrawClip(
        @Header("Authorization") bearerToken: String,
        @Path("id") clipId: String
    ): Response<BasicSuccessResponse>

    @POST("api/v1/admin/auth")
    suspend fun authenticateAdmin(
        @Body request: AdminAuthRequest
    ): Response<AdminAuthResponse>

    @POST("api/v1/admin/logout")
    suspend fun logoutAdmin(
        @Header("Authorization") bearerToken: String
    ): Response<BasicSuccessResponse>

    @GET("api/v1/admin/submissions")
    suspend fun getAdminSubmissions(
        @Header("Authorization") bearerToken: String
    ): Response<ClipsResponse>

    @POST("api/v1/admin/clips/{id}/moderate")
    suspend fun moderateClip(
        @Header("Authorization") bearerToken: String,
        @Path("id") clipId: String,
        @Body request: ModerateClipRequest
    ): Response<ClipResponse>

    @POST("api/v1/admin/access-code")
    suspend fun rotateSharedAccessCode(
        @Header("Authorization") bearerToken: String,
        @Body request: RotateAccessCodeRequest
    ): Response<RotateAccessCodeResponse>

    @POST("api/v1/admin/sessions/revoke")
    suspend fun revokeAllClipperSessions(
        @Header("Authorization") bearerToken: String
    ): Response<RevokeSessionsResponse>

    @GET("api/v1/admin/audit")
    suspend fun getAdminAuditLog(
        @Header("Authorization") bearerToken: String
    ): Response<AuditLogResponse>

    @PUT("api/v1/admin/streamers")
    suspend fun updateStreamers(
        @Header("Authorization") bearerToken: String,
        @Body request: UpdateStreamersRequest
    ): Response<StreamersResponse>

    @PUT("api/v1/admin/jabatan")
    suspend fun updateJabatanPositions(
        @Header("Authorization") bearerToken: String,
        @Body request: UpdateJabatanRequest
    ): Response<JabatanListResponse>
}
