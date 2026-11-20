package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.domain.model.ClipSubmission
import com.example.ui.components.OfficerModeBadge
import com.example.ui.theme.JdmcAmberAlert
import com.example.ui.theme.JdmcBrushedSilver
import com.example.ui.theme.JdmcCarbonElevated
import com.example.ui.theme.JdmcCarbonSurface
import com.example.ui.theme.JdmcGoldBorder
import com.example.ui.theme.JdmcGunmetal
import com.example.ui.theme.JdmcLightYellow
import com.example.ui.theme.JdmcMetallicSilver
import com.example.ui.theme.JdmcObsidianBlack
import com.example.ui.theme.JdmcSteelOutline
import com.example.ui.viewmodel.JdmcHqUiState
import com.example.ui.viewmodel.JdmcMainUiState

enum class ClipperHubSubTab(val label: String) {
    PUBLIC_CLIPS("Public Clips"),
    CLIPPER_ROOM("Clipper Room"),
    ADMIN_PANEL("Admin Panel")
}

enum class AdminZoneTab(val label: String) {
    HQ_OFFICER("HQ Officer Panel"),
    REMOTE_CLIPPER("Remote Clipper Mod")
}

@Composable
fun ClipperAdminHubScreen(
    activeSubTab: ClipperHubSubTab,
    onSelectSubTab: (ClipperHubSubTab) -> Unit,
    hqState: JdmcHqUiState,
    clipperState: JdmcMainUiState,
    filteredClips: List<ClipSubmission>,
    onConfigureBackend: () -> Unit,
    onQuickJumpFromAdmin: (DashboardQuickDestination) -> Unit,
    // HQ Officer Actions
    onOfficerLogin: (String) -> Unit,
    onOfficerLogout: () -> Unit,
    onSetOfficerPin: (String?) -> Unit,
    onSaveClubSettings: (welcome: String, status: String, alias: String) -> Unit,
    // Clipper Actions
    onSearchQueryChange: (String) -> Unit,
    onPlatformSelected: (com.example.domain.model.VideoPlatform?) -> Unit,
    onStreamerSelected: (String?) -> Unit,
    onRefreshPublicGallery: () -> Unit,
    onVerifyAccessCode: (String) -> Unit,
    onValidateSession: () -> Unit,
    onLogoutClipper: () -> Unit,
    onRefreshSubmissions: () -> Unit,
    onUploadThumbnail: (android.net.Uri, (String) -> Unit) -> Unit,
    onSubmitClip: (String, String, String, com.example.domain.model.StreamerProfile, com.example.domain.model.VideoPlatform, String, String, String, () -> Unit) -> Unit,
    onUpdateClip: (String, String, String, String, com.example.domain.model.StreamerProfile, com.example.domain.model.VideoPlatform, String, String, String, () -> Unit) -> Unit,
    onWithdrawClip: (String) -> Unit,
    onClearClipperBanner: () -> Unit,
    // Remote Clipper Admin Actions
    onAuthenticateRemoteAdmin: (String) -> Unit,
    onLogoutRemoteAdmin: () -> Unit,
    onRefreshRemoteAdmin: () -> Unit,
    onModerateClip: (String, com.example.domain.model.ModerationAction, String?, com.example.domain.model.StreamerProfile?, Boolean?) -> Unit,
    onRotateAccessCode: (String, Boolean, () -> Unit) -> Unit,
    onRevokeAllClipperSessions: () -> Unit,
    onSaveStreamers: (List<com.example.domain.model.StreamerProfile>) -> Unit,
    onSaveJabatanPositions: (List<com.example.domain.model.JabatanPosition>) -> Unit,
    onClearAdminBanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    var adminZone by rememberSaveable { mutableStateOf(AdminZoneTab.HQ_OFFICER) }

    Column(modifier = modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ClipperHubSubTab.entries) { sub ->
                val icon = when (sub) {
                    ClipperHubSubTab.PUBLIC_CLIPS -> Icons.Filled.Movie
                    ClipperHubSubTab.CLIPPER_ROOM -> Icons.Filled.Key
                    ClipperHubSubTab.ADMIN_PANEL -> Icons.Filled.AdminPanelSettings
                }
                FilterChip(
                    selected = activeSubTab == sub,
                    onClick = { onSelectSubTab(sub) },
                    leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text(sub.label) },
                    colors = jdmcFilterChipColors(),
                    modifier = Modifier.testTag("clipper_subtab_${sub.name.lowercase()}")
                )
            }
        }

        when (activeSubTab) {
            ClipperHubSubTab.PUBLIC_CLIPS -> {
                PublicGalleryScreen(
                    publishedClipsState = clipperState.publishedClipsState,
                    filteredClips = filteredClips,
                    streamers = clipperState.streamers,
                    jabatanPositions = clipperState.jabatanPositions,
                    selectedPlatform = clipperState.selectedPlatformFilter,
                    selectedStreamerId = clipperState.selectedStreamerFilterId,
                    searchQuery = clipperState.gallerySearchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onPlatformSelected = onPlatformSelected,
                    onStreamerSelected = onStreamerSelected,
                    onRefresh = onRefreshPublicGallery,
                    onConfigureBackend = onConfigureBackend,
                    modifier = Modifier.weight(1f)
                )
            }

            ClipperHubSubTab.CLIPPER_ROOM -> {
                ClipperRoomScreen(
                    backendStatus = clipperState.backendStatus,
                    clipperSession = clipperState.clipperSession,
                    remainingSeconds = clipperState.clipperRemainingSeconds,
                    rateLimitSeconds = clipperState.clipperRateLimitSeconds,
                    isVerifyingCode = clipperState.isVerifyingClipperCode,
                    authMessage = clipperState.clipperAuthMessage,
                    authIsUnavailable = clipperState.clipperAuthIsUnavailable,
                    mySubmissionsState = clipperState.mySubmissionsState,
                    publishedClipsState = clipperState.publishedClipsState,
                    streamers = clipperState.streamers,
                    jabatanPositions = clipperState.jabatanPositions,
                    isSubmittingClip = clipperState.isSubmittingClip,
                    isUploadingThumbnail = clipperState.isUploadingThumbnail,
                    actionBanner = clipperState.clipperActionBanner,
                    onVerifyAccessCode = onVerifyAccessCode,
                    onValidateSession = onValidateSession,
                    onLogout = onLogoutClipper,
                    onRefreshSubmissions = onRefreshSubmissions,
                    onUploadThumbnail = onUploadThumbnail,
                    onSubmitClip = onSubmitClip,
                    onUpdateClip = onUpdateClip,
                    onWithdrawClip = onWithdrawClip,
                    onClearBanner = onClearClipperBanner,
                    onConfigureBackend = onConfigureBackend,
                    modifier = Modifier.weight(1f)
                )
            }

            ClipperHubSubTab.ADMIN_PANEL -> {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdminZoneTab.entries.forEach { zone ->
                            FilterChip(
                                selected = adminZone == zone,
                                onClick = { adminZone = zone },
                                label = { Text(zone.label) },
                                colors = jdmcFilterChipColors(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_zone_${zone.name.lowercase()}")
                            )
                        }
                    }

                    when (adminZone) {
                        AdminZoneTab.HQ_OFFICER -> {
                            HqOfficerAdminPanel(
                                hqState = hqState,
                                onOfficerLogin = onOfficerLogin,
                                onOfficerLogout = onOfficerLogout,
                                onSetOfficerPin = onSetOfficerPin,
                                onSaveClubSettings = onSaveClubSettings,
                                onJumpToSection = onQuickJumpFromAdmin,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        AdminZoneTab.REMOTE_CLIPPER -> {
                            AdminModerationScreen(
                                backendStatus = clipperState.backendStatus,
                                adminSession = clipperState.adminSession,
                                remainingSeconds = clipperState.adminRemainingSeconds,
                                rateLimitSeconds = clipperState.adminRateLimitSeconds,
                                isAuthenticating = clipperState.isAuthenticatingAdmin,
                                authMessage = clipperState.adminAuthMessage,
                                authIsUnavailable = clipperState.adminAuthIsUnavailable,
                                submissionsState = clipperState.adminSubmissionsState,
                                auditState = clipperState.adminAuditState,
                                streamers = clipperState.streamers,
                                jabatanPositions = clipperState.jabatanPositions,
                                isActionBusy = clipperState.isAdminActionBusy,
                                actionBanner = clipperState.adminActionBanner,
                                onAuthenticateAdmin = onAuthenticateRemoteAdmin,
                                onLogoutAdmin = onLogoutRemoteAdmin,
                                onRefreshAdmin = onRefreshRemoteAdmin,
                                onModerateClip = onModerateClip,
                                onRotateAccessCode = onRotateAccessCode,
                                onRevokeAllClipperSessions = onRevokeAllClipperSessions,
                                onSaveStreamers = onSaveStreamers,
                                onSaveJabatanPositions = onSaveJabatanPositions,
                                onClearBanner = onClearAdminBanner,
                                onConfigureBackend = onConfigureBackend,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HqOfficerAdminPanel(
    hqState: JdmcHqUiState,
    onOfficerLogin: (String) -> Unit,
    onOfficerLogout: () -> Unit,
    onSetOfficerPin: (String?) -> Unit,
    onSaveClubSettings: (welcome: String, status: String, alias: String) -> Unit,
    onJumpToSection: (DashboardQuickDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = hqState.settings
    var pinEntry by remember { mutableStateOf("") }
    var welcomeInput by remember(settings.clubWelcomeHeadline) { mutableStateOf(settings.clubWelcomeHeadline) }
    var statusInput by remember(settings.clubStatusLabel) { mutableStateOf(settings.clubStatusLabel) }
    var aliasInput by remember(settings.activeMemberAlias) { mutableStateOf(settings.activeMemberAlias) }
    var newPinInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("hq_officer_admin_panel"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                color = JdmcCarbonSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, JdmcGoldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = JdmcLightYellow)
                            Text(
                                text = "HQ OFFICER ADMINISTRATION",
                                style = MaterialTheme.typography.titleMedium,
                                color = JdmcLightYellow
                            )
                        }
                        OfficerModeBadge(isOfficerActive = settings.officerModeActive)
                    }
                    Text(
                        text = "Separates officer editing privileges (Members, Roles, Announcements, Events, Gallery curation, Archive, App Settings) from standard member view.",
                        style = MaterialTheme.typography.bodySmall,
                        color = JdmcBrushedSilver
                    )

                    if (!settings.officerModeActive) {
                        if (settings.hasOfficerPin) {
                            OutlinedTextField(
                                value = pinEntry,
                                onValueChange = { pinEntry = it },
                                label = { Text("Officer Local PIN") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (!hqState.officerAuthError.isNullOrBlank()) {
                            Text(
                                text = hqState.officerAuthError,
                                style = MaterialTheme.typography.bodySmall,
                                color = JdmcAmberAlert
                            )
                        }
                        Button(
                            onClick = {
                                val entered = pinEntry
                                pinEntry = ""
                                onOfficerLogin(entered)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JdmcLightYellow,
                                contentColor = JdmcObsidianBlack
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("officer_login_button")
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (settings.hasOfficerPin) "Unlock Officer Admin Mode" else "Enter Officer Admin Session")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onOfficerLogout,
                            border = BorderStroke(1.dp, JdmcLightYellow),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("officer_logout_button")
                        ) {
                            Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp), tint = JdmcLightYellow)
                            Spacer(Modifier.width(8.dp))
                            Text("Logout Officer Session (Return to Member View)", color = JdmcLightYellow)
                        }
                    }
                }
            }
        }

        if (settings.officerModeActive) {
            item {
                Surface(
                    color = JdmcCarbonSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, JdmcSteelOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "MANAGEMENT WORKSPACES",
                            style = MaterialTheme.typography.titleMedium,
                            color = JdmcLightYellow
                        )
                        val shortcuts = listOf(
                            "Member Management (${hqState.members.size})" to DashboardQuickDestination.MEMBERS,
                            "Role Management (${hqState.organizationRoles.size})" to DashboardQuickDestination.ORGANIZATION,
                            "Announcement Management (${hqState.announcements.size})" to DashboardQuickDestination.ANNOUNCEMENTS,
                            "Event Management (${hqState.events.size})" to DashboardQuickDestination.EVENTS,
                            "Gallery Management (${hqState.mediaItems.size})" to DashboardQuickDestination.MEDIA,
                            "Digital Archive (${hqState.archiveEntries.size})" to DashboardQuickDestination.ARCHIVE
                        )
                        shortcuts.forEach { (label, dest) ->
                            OutlinedButton(
                                onClick = { onJumpToSection(dest) },
                                border = BorderStroke(1.dp, JdmcSteelOutline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Open $label", color = JdmcMetallicSilver)
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    color = JdmcCarbonSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, JdmcSteelOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Settings, contentDescription = null, tint = JdmcLightYellow)
                            Text("APPLICATION SETTINGS", style = MaterialTheme.typography.titleMedium, color = JdmcLightYellow)
                        }
                        OutlinedTextField(
                            value = welcomeInput,
                            onValueChange = { welcomeInput = it },
                            label = { Text("Dashboard Welcome Headline") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = statusInput,
                            onValueChange = { statusInput = it },
                            label = { Text("Club Status Indicator") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = aliasInput,
                            onValueChange = { aliasInput = it },
                            label = { Text("Active Rider Alias (RSVP / Gallery)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { onSaveClubSettings(welcomeInput, statusInput, aliasInput) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JdmcLightYellow,
                                contentColor = JdmcObsidianBlack
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Application Settings")
                        }

                        Surface(
                            color = JdmcCarbonElevated,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, JdmcSteelOutline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "OPTIONAL LOCAL OFFICER PIN LOCK",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = JdmcLightYellow
                                )
                                OutlinedTextField(
                                    value = newPinInput,
                                    onValueChange = { newPinInput = it },
                                    label = { Text("Set New Local Officer PIN") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            if (newPinInput.isNotBlank()) {
                                                onSetOfficerPin(newPinInput)
                                                newPinInput = ""
                                            }
                                        },
                                        border = BorderStroke(1.dp, JdmcGoldBorder),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Save PIN", color = JdmcLightYellow)
                                    }
                                    if (settings.hasOfficerPin) {
                                        OutlinedButton(
                                            onClick = { onSetOfficerPin(null) },
                                            border = BorderStroke(1.dp, JdmcSteelOutline),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Clear PIN", color = JdmcAmberAlert)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
