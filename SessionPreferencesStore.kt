package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.BuildConfig
import com.example.domain.model.AdminSession
import com.example.domain.model.ClipperSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom

private val Context.jdmcSessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "jdmc_clipper_session_store"
)

data class HqSettingsPreferences(
    val clubWelcomeHeadline: String = "Welcome back to JDMC Digital HQ",
    val clubStatusLabel: String = "CHAPTER ACTIVE • SATU MIMPI RP",
    val activeMemberAlias: String = "Gashima",
    val officerModeActive: Boolean = false,
    val hasOfficerPin: Boolean = false,
    val officerPinHash: String = "",
    val officerPinSalt: String = ""
)

/**
 * Stores configured backend endpoint URL, opaque server-issued Clipper/Admin tokens,
 * and local HQ officer preferences (with salted SHA-256 PIN hashing if user sets an optional local PIN).
 * Raw shared Clipper access codes and remote admin keys are NEVER stored on-device.
 */
class SessionPreferencesStore(private val context: Context) {

    private object Keys {
        val BACKEND_BASE_URL = stringPreferencesKey("backend_base_url")
        val CLIPPER_TOKEN = stringPreferencesKey("clipper_session_token")
        val CLIPPER_EXPIRES_AT = longPreferencesKey("clipper_expires_at")
        val CLIPPER_ISSUED_AT = longPreferencesKey("clipper_issued_at")
        val CLIPPER_RECEIPTS = stringSetPreferencesKey("clipper_ownership_receipts")
        val ADMIN_TOKEN = stringPreferencesKey("admin_session_token")
        val ADMIN_EXPIRES_AT = longPreferencesKey("admin_expires_at")

        val HQ_WELCOME_HEADLINE = stringPreferencesKey("hq_welcome_headline")
        val HQ_CLUB_STATUS = stringPreferencesKey("hq_club_status")
        val HQ_ACTIVE_MEMBER = stringPreferencesKey("hq_active_member_alias")
        val HQ_OFFICER_MODE = booleanPreferencesKey("hq_officer_mode")
        val HQ_OFFICER_PIN_HASH = stringPreferencesKey("hq_officer_pin_hash")
        val HQ_OFFICER_PIN_SALT = stringPreferencesKey("hq_officer_pin_salt")
        val HQ_BUNDLED_VIDEOS_V1_SEEDED = booleanPreferencesKey("hq_bundled_videos_v1_seeded")
    }

    val backendBaseUrlFlow: Flow<String> = context.jdmcSessionDataStore.data.map { prefs ->
        val stored = prefs[Keys.BACKEND_BASE_URL]
        if (!stored.isNullOrBlank()) stored else BuildConfig.JDMC_API_BASE_URL
    }

    val clipperSessionFlow: Flow<ClipperSession?> = context.jdmcSessionDataStore.data.map { prefs ->
        val token = prefs[Keys.CLIPPER_TOKEN].orEmpty()
        val expiresAt = prefs[Keys.CLIPPER_EXPIRES_AT] ?: 0L
        val issuedAt = prefs[Keys.CLIPPER_ISSUED_AT] ?: 0L
        val receipts = prefs[Keys.CLIPPER_RECEIPTS] ?: emptySet()
        if (token.isBlank() || expiresAt <= System.currentTimeMillis()) {
            null
        } else {
            ClipperSession(
                sessionToken = token,
                expiresAtEpochMs = expiresAt,
                issuedAtEpochMs = issuedAt,
                ownershipReceipts = receipts
            )
        }
    }

    val adminSessionFlow: Flow<AdminSession?> = context.jdmcSessionDataStore.data.map { prefs ->
        val token = prefs[Keys.ADMIN_TOKEN].orEmpty()
        val expiresAt = prefs[Keys.ADMIN_EXPIRES_AT] ?: 0L
        if (token.isBlank() || expiresAt <= System.currentTimeMillis()) {
            null
        } else {
            AdminSession(adminToken = token, expiresAtEpochMs = expiresAt)
        }
    }

    val hqSettingsFlow: Flow<HqSettingsPreferences> = context.jdmcSessionDataStore.data.map { prefs ->
        val hash = prefs[Keys.HQ_OFFICER_PIN_HASH].orEmpty()
        val salt = prefs[Keys.HQ_OFFICER_PIN_SALT].orEmpty()
        HqSettingsPreferences(
            clubWelcomeHeadline = prefs[Keys.HQ_WELCOME_HEADLINE]?.takeIf { it.isNotBlank() }
                ?: "Welcome back to JDMC Digital HQ",
            clubStatusLabel = prefs[Keys.HQ_CLUB_STATUS]?.takeIf { it.isNotBlank() }
                ?: "CHAPTER ACTIVE • SATU MIMPI RP",
            activeMemberAlias = prefs[Keys.HQ_ACTIVE_MEMBER]?.takeIf { it.isNotBlank() }
                ?: "Gashima",
            officerModeActive = prefs[Keys.HQ_OFFICER_MODE] ?: false,
            hasOfficerPin = hash.isNotBlank() && salt.isNotBlank(),
            officerPinHash = hash,
            officerPinSalt = salt
        )
    }

    suspend fun saveBackendBaseUrl(url: String) {
        context.jdmcSessionDataStore.edit { prefs ->
            val trimmed = url.trim()
            if (trimmed.isEmpty()) {
                prefs.remove(Keys.BACKEND_BASE_URL)
            } else {
                prefs[Keys.BACKEND_BASE_URL] = if (trimmed.endsWith("/")) trimmed else "$trimmed/"
            }
        }
    }

    suspend fun saveClipperSession(session: ClipperSession) {
        context.jdmcSessionDataStore.edit { prefs ->
            prefs[Keys.CLIPPER_TOKEN] = session.sessionToken
            prefs[Keys.CLIPPER_EXPIRES_AT] = session.expiresAtEpochMs
            prefs[Keys.CLIPPER_ISSUED_AT] = session.issuedAtEpochMs
            prefs[Keys.CLIPPER_RECEIPTS] = session.ownershipReceipts
        }
    }

    suspend fun addOwnershipReceipt(clipId: String) {
        context.jdmcSessionDataStore.edit { prefs ->
            val current = prefs[Keys.CLIPPER_RECEIPTS] ?: emptySet()
            prefs[Keys.CLIPPER_RECEIPTS] = current + clipId
        }
    }

    suspend fun removeOwnershipReceipt(clipId: String) {
        context.jdmcSessionDataStore.edit { prefs ->
            val current = prefs[Keys.CLIPPER_RECEIPTS] ?: emptySet()
            prefs[Keys.CLIPPER_RECEIPTS] = current - clipId
        }
    }

    suspend fun clearClipperSession() {
        context.jdmcSessionDataStore.edit { prefs ->
            prefs.remove(Keys.CLIPPER_TOKEN)
            prefs.remove(Keys.CLIPPER_EXPIRES_AT)
            prefs.remove(Keys.CLIPPER_ISSUED_AT)
            prefs.remove(Keys.CLIPPER_RECEIPTS)
        }
    }

    suspend fun saveAdminSession(session: AdminSession) {
        context.jdmcSessionDataStore.edit { prefs ->
            prefs[Keys.ADMIN_TOKEN] = session.adminToken
            prefs[Keys.ADMIN_EXPIRES_AT] = session.expiresAtEpochMs
        }
    }

    suspend fun clearAdminSession() {
        context.jdmcSessionDataStore.edit { prefs ->
            prefs.remove(Keys.ADMIN_TOKEN)
            prefs.remove(Keys.ADMIN_EXPIRES_AT)
        }
    }

    suspend fun updateHqAppearanceSettings(
        welcomeHeadline: String,
        clubStatusLabel: String,
        activeMemberAlias: String
    ) {
        context.jdmcSessionDataStore.edit { prefs ->
            prefs[Keys.HQ_WELCOME_HEADLINE] = welcomeHeadline.trim()
            prefs[Keys.HQ_CLUB_STATUS] = clubStatusLabel.trim()
            prefs[Keys.HQ_ACTIVE_MEMBER] = activeMemberAlias.trim()
        }
    }

    suspend fun setHqOfficerModeActive(active: Boolean) {
        context.jdmcSessionDataStore.edit { prefs ->
            prefs[Keys.HQ_OFFICER_MODE] = active
        }
    }

    suspend fun setHqOfficerPin(pin: String?) {
        context.jdmcSessionDataStore.edit { prefs ->
            if (pin.isNullOrBlank()) {
                prefs.remove(Keys.HQ_OFFICER_PIN_HASH)
                prefs.remove(Keys.HQ_OFFICER_PIN_SALT)
            } else {
                val saltBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
                val saltHex = saltBytes.joinToString("") { "%02x".format(it) }
                prefs[Keys.HQ_OFFICER_PIN_SALT] = saltHex
                prefs[Keys.HQ_OFFICER_PIN_HASH] = hashPin(pin.trim(), saltHex)
            }
        }
    }

    suspend fun isBundledVideosV1Seeded(): Boolean =
        context.jdmcSessionDataStore.data.first()[Keys.HQ_BUNDLED_VIDEOS_V1_SEEDED] ?: false

    suspend fun markBundledVideosV1Seeded() {
        context.jdmcSessionDataStore.edit { prefs ->
            prefs[Keys.HQ_BUNDLED_VIDEOS_V1_SEEDED] = true
        }
    }

    companion object {
        fun hashPin(pin: String, saltHex: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest("$saltHex:$pin".toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
