package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.AdminSession
import com.example.domain.model.AuditLogEntry
import com.example.domain.model.BackendStatus
import com.example.domain.model.ClipSubmission
import com.example.domain.model.JabatanPosition
import com.example.domain.model.ModerationAction
import com.example.domain.model.StreamerProfile
import com.example.domain.model.SubmissionStatus
import com.example.domain.model.UiState
import com.example.ui.components.BackendUnavailableBanner
import com.example.ui.components.ClipThumbnailView
import com.example.ui.components.FeaturedBadge
import com.example.ui.components.JabatanBadge
import com.example.ui.components.PlatformBadge
import com.example.ui.components.RateLimitBanner
import com.example.ui.components.SessionCountdownBanner
import com.example.ui.components.SubmissionStatusChip
import com.example.ui.theme.JdmcBrushedSilver
import com.example.ui.theme.JdmcCarbonElevated
import com.example.ui.theme.JdmcCarbonSurface
import com.example.ui.theme.JdmcAmberAlert
import com.example.ui.theme.JdmcGoldBorder
import com.example.ui.theme.JdmcGoldContainer
import com.example.ui.theme.JdmcGunmetal
import com.example.ui.theme.JdmcLightYellow
import com.example.ui.theme.JdmcMetallicSilver
import com.example.ui.theme.JdmcObsidianBlack
import com.example.ui.theme.JdmcSteelOutline
import com.example.ui.theme.JdmcWarmGold
import com.example.util.VideoUrlHelper
import java.util.Locale

enum class AdminConsoleSection(val label: String) {
    QUEUE("Moderation Queue"),
    HISTORY("Submission History"),
    JABATAN_ROSTER("Jabatan & Roster"),
    ACCESS_CONTROL("Access Code")
}

@Composable
fun AdminModerationScreen(
    backendStatus: BackendStatus,
    adminSession: AdminSession?,
    remainingSeconds: Long,
    rateLimitSeconds: Int,
    isAuthenticating: Boolean,
    authMessage: String?,
    authIsUnavailable: Boolean,
    submissionsState: UiState<List<ClipSubmission>>,
    auditState: UiState<List<AuditLogEntry>>,
    streamers: List<StreamerProfile>,
    jabatanPositions: List<JabatanPosition>,
    isActionBusy: Boolean,
    actionBanner: String?,
    onAuthenticateAdmin: (String) -> Unit,
    onLogoutAdmin: () -> Unit,
    onRefreshAdmin: () -> Unit,
    onModerateClip: (String, ModerationAction, String?, StreamerProfile?, Boolean?) -> Unit,
    onRotateAccessCode: (String, Boolean, () -> Unit) -> Unit,
    onRevokeAllClipperSessions: () -> Unit,
    onSaveStreamers: (List<StreamerProfile>) -> Unit,
    onSaveJabatanPositions: (List<JabatanPosition>) -> Unit,
    onClearBanner: () -> Unit,
    onConfigureBackend: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (adminSession == null) {
        AdminAuthGate(
            backendStatus = backendStatus,
            rateLimitSeconds = rateLimitSeconds,
            isAuthenticating = isAuthenticating,
            authMessage = authMessage,
            authIsUnavailable = authIsUnavailable,
            onAuthenticateAdmin = onAuthenticateAdmin,
            onConfigureBackend = onConfigureBackend,
            modifier = modifier
        )
    } else {
        AdminConsole(
            backendStatus = backendStatus,
            remainingSeconds = remainingSeconds,
            submissionsState = submissionsState,
            auditState = auditState,
            streamers = streamers,
            jabatanPositions = jabatanPositions,
            isActionBusy = isActionBusy,
            actionBanner = actionBanner,
            onLogoutAdmin = onLogoutAdmin,
            onRefreshAdmin = onRefreshAdmin,
            onModerateClip = onModerateClip,
            onRotateAccessCode = onRotateAccessCode,
            onRevokeAllClipperSessions = onRevokeAllClipperSessions,
            onSaveStreamers = onSaveStreamers,
            onSaveJabatanPositions = onSaveJabatanPositions,
            onClearBanner = onClearBanner,
            onConfigureBackend = onConfigureBackend,
            modifier = modifier
        )
    }
}

@Composable
private fun AdminAuthGate(
    backendStatus: BackendStatus,
    rateLimitSeconds: Int,
    isAuthenticating: Boolean,
    authMessage: String?,
    authIsUnavailable: Boolean,
    onAuthenticateAdmin: (String) -> Unit,
    onConfigureBackend: () -> Unit,
    modifier: Modifier = Modifier
) {
    var adminKeyInput by remember { mutableStateOf("") }
    var keyVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                color = JdmcCarbonSurface,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, JdmcGoldBorder.copy(alpha = 0.65f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.jdmc_club_logo),
                            contentDescription = "JDMC Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                        Column {
                            Text(
                                text = "Administrator Moderation",
                                style = MaterialTheme.typography.headlineMedium,
                                color = JdmcLightYellow
                            )
                            Text(
                                text = "ISOLATED SERVER-VERIFIED STAFF GATE",
                                style = MaterialTheme.typography.labelSmall,
                                color = JdmcGunmetal
                            )
                        }
                    }

                    Surface(
                        color = JdmcCarbonElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, JdmcGoldBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                tint = JdmcLightYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Strict Privilege Separation: The shared JDMC Clipper Access Code grants submission access only and never unlocks administrator moderation or Jabatan management.",
                                style = MaterialTheme.typography.bodySmall,
                                color = JdmcBrushedSilver
                            )
                        }
                    }

                    if (!backendStatus.isAvailable || authIsUnavailable) {
                        val reason = if (authIsUnavailable && !authMessage.isNullOrBlank()) {
                            authMessage
                        } else if (backendStatus is BackendStatus.Unreachable) {
                            backendStatus.reason
                        } else null
                        BackendUnavailableBanner(
                            reason = reason,
                            onConfigureClick = onConfigureBackend
                        )
                    }

                    RateLimitBanner(remainingSeconds = rateLimitSeconds)

                    if (!authIsUnavailable && !authMessage.isNullOrBlank()) {
                        Surface(
                            color = JdmcGoldContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, JdmcGoldBorder)
                        ) {
                            Text(
                                text = authMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = JdmcMetallicSilver,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = adminKeyInput,
                        onValueChange = { adminKeyInput = it },
                        label = { Text("Administrator Authorization Key") },
                        placeholder = { Text("Enter server-issued admin key") },
                        singleLine = true,
                        visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { keyVisible = !keyVisible }) {
                                Icon(
                                    imageVector = if (keyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Toggle key visibility",
                                    tint = JdmcGunmetal
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_key_input")
                    )

                    Button(
                        onClick = {
                            val submitted = adminKeyInput
                            adminKeyInput = ""
                            onAuthenticateAdmin(submitted)
                        },
                        enabled = !isAuthenticating && rateLimitSeconds == 0 && adminKeyInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JdmcLightYellow,
                            contentColor = JdmcObsidianBlack
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("authenticate_admin_button")
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = JdmcObsidianBlack
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Verifying Admin Session...")
                        } else {
                            Text("Authenticate Admin")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminConsole(
    backendStatus: BackendStatus,
    remainingSeconds: Long,
    submissionsState: UiState<List<ClipSubmission>>,
    auditState: UiState<List<AuditLogEntry>>,
    streamers: List<StreamerProfile>,
    jabatanPositions: List<JabatanPosition>,
    isActionBusy: Boolean,
    actionBanner: String?,
    onLogoutAdmin: () -> Unit,
    onRefreshAdmin: () -> Unit,
    onModerateClip: (String, ModerationAction, String?, StreamerProfile?, Boolean?) -> Unit,
    onRotateAccessCode: (String, Boolean, () -> Unit) -> Unit,
    onRevokeAllClipperSessions: () -> Unit,
    onSaveStreamers: (List<StreamerProfile>) -> Unit,
    onSaveJabatanPositions: (List<JabatanPosition>) -> Unit,
    onClearBanner: () -> Unit,
    onConfigureBackend: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSection by rememberSaveable { mutableStateOf(AdminConsoleSection.QUEUE) }
    var statusFilter by rememberSaveable { mutableStateOf<SubmissionStatus?>(null) }

    var rejectTarget by remember { mutableStateOf<ClipSubmission?>(null) }
    var rejectNote by remember { mutableStateOf("") }

    var reassignTarget by remember { mutableStateOf<ClipSubmission?>(null) }
    var addStreamerDialogOpen by remember { mutableStateOf(false) }
    var addJabatanDialogOpen by remember { mutableStateOf(false) }

    rejectTarget?.let { clip ->
        AlertDialog(
            onDismissRequest = { rejectTarget = null },
            containerColor = JdmcCarbonSurface,
            title = { Text("Reject Submission", color = JdmcLightYellow) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Provide a moderation note explaining why \"${clip.title}\" is rejected.",
                        color = JdmcBrushedSilver,
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = rejectNote,
                        onValueChange = { rejectNote = it },
                        label = { Text("Moderation Note") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onModerateClip(clip.id, ModerationAction.REJECT, rejectNote.ifBlank { "Does not meet JDMC clip standards." }, null, null)
                        rejectNote = ""
                        rejectTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JdmcLightYellow,
                        contentColor = JdmcObsidianBlack
                    )
                ) {
                    Text("Confirm Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectTarget = null }) {
                    Text("Cancel", color = JdmcGunmetal)
                }
            }
        )
    }

    reassignTarget?.let { clip ->
        AlertDialog(
            onDismissRequest = { reassignTarget = null },
            containerColor = JdmcCarbonSurface,
            title = { Text("Reassign Streamer Attribution", color = JdmcLightYellow) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select the JDMC streamer for \"${clip.title}\":",
                        style = MaterialTheme.typography.bodySmall,
                        color = JdmcBrushedSilver
                    )
                    streamers.forEach { streamer ->
                        OutlinedButton(
                            onClick = {
                                onModerateClip(
                                    clip.id,
                                    ModerationAction.REASSIGN_STREAMER,
                                    "Streamer attribution updated to ${streamer.name} (${streamer.jabatan})",
                                    streamer,
                                    null
                                )
                                reassignTarget = null
                            },
                            border = BorderStroke(1.dp, JdmcSteelOutline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${streamer.name} • ${streamer.jabatan} (${streamer.channelHandle})", color = JdmcMetallicSilver)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { reassignTarget = null }) {
                    Text("Close", color = JdmcGunmetal)
                }
            }
        )
    }

    if (addStreamerDialogOpen) {
        AddStreamerDialog(
            jabatanPositions = jabatanPositions,
            onDismiss = { addStreamerDialogOpen = false },
            onAdd = { name, handle, jabatan, specialty ->
                val slug = name.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), "-").trim('-')
                val newStreamer = StreamerProfile(
                    id = slug.ifBlank { "streamer-${System.currentTimeMillis()}" },
                    name = name.trim(),
                    channelHandle = handle.trim(),
                    jabatan = jabatan.trim(),
                    specialty = specialty.trim(),
                    isActive = true
                )
                onSaveStreamers(streamers + newStreamer)
                addStreamerDialogOpen = false
            }
        )
    }

    if (addJabatanDialogOpen) {
        AddJabatanDialog(
            nextTierLevel = (jabatanPositions.maxOfOrNull { it.tierLevel } ?: 0) + 1,
            onDismiss = { addJabatanDialogOpen = false },
            onAdd = { rankTitle, indonesianTitle, tierLevel, responsibilities, isOfficer ->
                val slug = rankTitle.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), "-").trim('-')
                val newPos = JabatanPosition(
                    id = slug.ifBlank { "jabatan-${System.currentTimeMillis()}" },
                    rankTitle = rankTitle.trim(),
                    indonesianTitle = indonesianTitle.trim(),
                    tierLevel = tierLevel,
                    responsibilities = responsibilities.trim(),
                    isOfficerRole = isOfficer
                )
                onSaveJabatanPositions((jabatanPositions + newPos).sortedBy { it.tierLevel })
                addJabatanDialogOpen = false
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SessionCountdownBanner(
                roleLabel = "Administrator Session Active",
                remainingSeconds = remainingSeconds,
                onRefreshCheck = onRefreshAdmin,
                onLogout = onLogoutAdmin
            )
        }

        if (!backendStatus.isAvailable) {
            item {
                BackendUnavailableBanner(
                    reason = (backendStatus as? BackendStatus.Unreachable)?.reason,
                    onConfigureClick = onConfigureBackend
                )
            }
        }

        if (!actionBanner.isNullOrBlank()) {
            item {
                Surface(
                    color = JdmcCarbonElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, JdmcLightYellow)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = actionBanner,
                            style = MaterialTheme.typography.bodySmall,
                            color = JdmcMetallicSilver,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onClearBanner) {
                            Text("Dismiss", color = JdmcLightYellow)
                        }
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AdminConsoleSection.entries) { section ->
                    FilterChip(
                        selected = activeSection == section,
                        onClick = { activeSection = section },
                        label = { Text(section.label) },
                        colors = jdmcFilterChipColors(),
                        modifier = Modifier.testTag("admin_section_${section.name.lowercase()}")
                    )
                }
            }
        }

        when (activeSection) {
            AdminConsoleSection.QUEUE -> {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = statusFilter == null,
                                onClick = { statusFilter = null },
                                label = { Text("All") },
                                colors = jdmcFilterChipColors()
                            )
                        }
                        items(SubmissionStatus.entries) { status ->
                            FilterChip(
                                selected = statusFilter == status,
                                onClick = { statusFilter = if (statusFilter == status) null else status },
                                label = { Text(status.displayName) },
                                colors = jdmcFilterChipColors()
                            )
                        }
                    }
                }

                when (submissionsState) {
                    is UiState.Loading -> item {
                        CircularProgressIndicator(color = JdmcLightYellow)
                    }
                    is UiState.BackendUnavailable -> item {
                        BackendUnavailableBanner(
                            reason = submissionsState.reason,
                            onConfigureClick = onConfigureBackend
                        )
                    }
                    is UiState.Error -> item {
                        Text(submissionsState.message, color = JdmcAmberAlert)
                    }
                    is UiState.Success -> {
                        val filtered = submissionsState.data.filter {
                            statusFilter == null || it.status == statusFilter
                        }
                        if (filtered.isEmpty()) {
                            item {
                                Surface(
                                    color = JdmcCarbonSurface,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, JdmcSteelOutline),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "No submissions in this queue filter.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = JdmcGunmetal,
                                        modifier = Modifier.padding(18.dp)
                                    )
                                }
                            }
                        } else {
                            items(filtered, key = { it.id }) { clip ->
                                AdminModerationClipCard(
                                    clip = clip,
                                    isBusy = isActionBusy,
                                    onApprove = {
                                        onModerateClip(clip.id, ModerationAction.APPROVE, "Approved by JDMC Admin", null, null)
                                    },
                                    onPublish = {
                                        onModerateClip(clip.id, ModerationAction.PUBLISH, "Published to Public Gallery", null, null)
                                    },
                                    onReject = { rejectTarget = clip },
                                    onToggleFeature = {
                                        val action = if (clip.isFeatured) ModerationAction.UNFEATURE else ModerationAction.FEATURE
                                        onModerateClip(clip.id, action, null, null, !clip.isFeatured)
                                    },
                                    onRemovePublished = {
                                        onModerateClip(clip.id, ModerationAction.REMOVE_PUBLISHED, "Removed from Public Gallery", null, false)
                                    },
                                    onReassignStreamer = { reassignTarget = clip }
                                )
                            }
                        }
                    }
                    UiState.Idle -> Unit
                }
            }

            AdminConsoleSection.HISTORY -> {
                when (auditState) {
                    is UiState.Loading -> item { CircularProgressIndicator(color = JdmcLightYellow) }
                    is UiState.BackendUnavailable -> item {
                        BackendUnavailableBanner(auditState.reason, onConfigureBackend)
                    }
                    is UiState.Error -> item { Text(auditState.message, color = JdmcAmberAlert) }
                    is UiState.Success -> {
                        if (auditState.data.isEmpty()) {
                            item {
                                Surface(
                                    color = JdmcCarbonSurface,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, JdmcSteelOutline),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "No moderation history entries recorded yet.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = JdmcGunmetal,
                                        modifier = Modifier.padding(18.dp)
                                    )
                                }
                            }
                        } else {
                            items(auditState.data, key = { it.id }) { entry ->
                                Surface(
                                    color = JdmcCarbonSurface,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, JdmcSteelOutline),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.History,
                                            contentDescription = null,
                                            tint = JdmcLightYellow,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Text(
                                                text = "${entry.action} • ${entry.clipTitle}",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = JdmcMetallicSilver
                                            )
                                            if (!entry.note.isNullOrBlank()) {
                                                Text(
                                                    text = entry.note,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = JdmcBrushedSilver
                                                )
                                            }
                                            Text(
                                                text = "${entry.actorRole} • ${VideoUrlHelper.formatDisplayDate(entry.timestampIso)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = JdmcGunmetal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    UiState.Idle -> Unit
                }
            }

            AdminConsoleSection.JABATAN_ROSTER -> {
                item {
                    JabatanManagementCard(
                        positions = jabatanPositions,
                        isBusy = isActionBusy,
                        onAddJabatanClick = { addJabatanDialogOpen = true },
                        onToggleOfficerRole = { target ->
                            val updated = jabatanPositions.map {
                                if (it.id == target.id) it.copy(isOfficerRole = !it.isOfficerRole) else it
                            }
                            onSaveJabatanPositions(updated)
                        }
                    )
                }
                item {
                    StreamerRosterManagementCard(
                        streamers = streamers,
                        jabatanPositions = jabatanPositions,
                        isBusy = isActionBusy,
                        onAddStreamerClick = { addStreamerDialogOpen = true },
                        onChangeStreamerJabatan = { target, newJabatan ->
                            val updated = streamers.map {
                                if (it.id == target.id) it.copy(jabatan = newJabatan) else it
                            }
                            onSaveStreamers(updated)
                        },
                        onToggleStreamerActive = { target ->
                            val updated = streamers.map {
                                if (it.id == target.id) it.copy(isActive = !it.isActive) else it
                            }
                            onSaveStreamers(updated)
                        }
                    )
                }
            }

            AdminConsoleSection.ACCESS_CONTROL -> {
                item {
                    AccessCodeRotationCard(
                        isBusy = isActionBusy,
                        onRotateCode = onRotateAccessCode,
                        onRevokeAllSessions = onRevokeAllClipperSessions
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminModerationClipCard(
    clip: ClipSubmission,
    isBusy: Boolean,
    onApprove: () -> Unit,
    onPublish: () -> Unit,
    onReject: () -> Unit,
    onToggleFeature: () -> Unit,
    onRemovePublished: () -> Unit,
    onReassignStreamer: () -> Unit
) {
    Surface(
        color = JdmcCarbonSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, JdmcSteelOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ClipThumbnailView(thumbnailUrl = clip.thumbnailUrl, contentDescription = clip.title)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SubmissionStatusChip(status = clip.status)
                    if (clip.isFeatured) FeaturedBadge()
                }
                PlatformBadge(platform = clip.platform)
            }
            Text(
                text = clip.title,
                style = MaterialTheme.typography.titleLarge,
                color = JdmcLightYellow
            )
            Text(
                text = "Clipper: ${clip.clipperDisplayName} (${clip.clipperJabatan}) • Streamer: ${clip.streamerName} (${clip.streamerJabatan})",
                style = MaterialTheme.typography.bodySmall,
                color = JdmcBrushedSilver
            )
            Text(
                text = "Submitted: ${VideoUrlHelper.formatDisplayDate(clip.submittedAtIso)} • ${clip.videoUrl}",
                style = MaterialTheme.typography.labelSmall,
                color = JdmcGunmetal
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JdmcWarmGold,
                        contentColor = JdmcObsidianBlack
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Approve")
                }
                Button(
                    onClick = onPublish,
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JdmcLightYellow,
                        contentColor = JdmcObsidianBlack
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Publish, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Publish")
                }
                OutlinedButton(
                    onClick = onReject,
                    enabled = !isBusy,
                    border = BorderStroke(1.dp, JdmcGoldBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = JdmcAmberAlert),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reject")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onToggleFeature,
                    enabled = !isBusy,
                    border = BorderStroke(1.dp, JdmcSteelOutline),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (clip.isFeatured) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = null,
                        tint = JdmcLightYellow,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (clip.isFeatured) "Unfeature" else "Feature", color = JdmcLightYellow)
                }
                OutlinedButton(
                    onClick = onReassignStreamer,
                    enabled = !isBusy,
                    border = BorderStroke(1.dp, JdmcSteelOutline),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.SwitchAccount, contentDescription = null, modifier = Modifier.size(15.dp), tint = JdmcMetallicSilver)
                    Spacer(Modifier.width(4.dp))
                    Text("Streamer", color = JdmcMetallicSilver)
                }
            }
            if (clip.status == SubmissionStatus.PUBLISHED) {
                OutlinedButton(
                    onClick = onRemovePublished,
                    enabled = !isBusy,
                    border = BorderStroke(1.dp, JdmcGoldBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = JdmcAmberAlert),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Remove Published Clip")
                }
            }
        }
    }
}

@Composable
private fun JabatanManagementCard(
    positions: List<JabatanPosition>,
    isBusy: Boolean,
    onAddJabatanClick: () -> Unit,
    onToggleOfficerRole: (JabatanPosition) -> Unit
) {
    Surface(
        color = JdmcCarbonSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, JdmcGoldBorder.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    Icon(Icons.Filled.MilitaryTech, contentDescription = null, tint = JdmcLightYellow)
                    Text(
                        text = "Struktur Jabatan JDMC",
                        style = MaterialTheme.typography.titleMedium,
                        color = JdmcLightYellow
                    )
                }
                OutlinedButton(
                    onClick = onAddJabatanClick,
                    enabled = !isBusy,
                    border = BorderStroke(1.dp, JdmcGoldBorder)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = JdmcLightYellow)
                    Spacer(Modifier.width(4.dp))
                    Text("Add Jabatan", color = JdmcLightYellow)
                }
            }

            positions.sortedBy { it.tierLevel }.forEach { pos ->
                Surface(
                    color = JdmcCarbonElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, JdmcSteelOutline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "#${pos.tierLevel} ${pos.rankTitle} (${pos.indonesianTitle})",
                                style = MaterialTheme.typography.titleSmall,
                                color = JdmcLightYellow
                            )
                            Text(
                                text = pos.responsibilities,
                                style = MaterialTheme.typography.bodySmall,
                                color = JdmcGunmetal
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (pos.isOfficerRole) "Officer" else "Member",
                                style = MaterialTheme.typography.labelSmall,
                                color = JdmcBrushedSilver
                            )
                            Switch(
                                checked = pos.isOfficerRole,
                                onCheckedChange = { onToggleOfficerRole(pos) },
                                enabled = !isBusy
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessCodeRotationCard(
    isBusy: Boolean,
    onRotateCode: (String, Boolean, () -> Unit) -> Unit,
    onRevokeAllSessions: () -> Unit
) {
    var newCode by remember { mutableStateOf("") }
    var revokeExisting by remember { mutableStateOf(true) }
    var localError by remember { mutableStateOf<String?>(null) }

    Surface(
        color = JdmcCarbonSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, JdmcSteelOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Key, contentDescription = null, tint = JdmcLightYellow)
                Text(
                    text = "Shared Clipper Access Code Control",
                    style = MaterialTheme.typography.titleMedium,
                    color = JdmcLightYellow
                )
            }
            Text(
                text = "Change or reset the shared JDMC Clipper Access Code on the backend. The current code is never displayed or cached in the client.",
                style = MaterialTheme.typography.bodySmall,
                color = JdmcGunmetal
            )
            OutlinedTextField(
                value = newCode,
                onValueChange = { newCode = it; localError = null },
                label = { Text("New Shared Access Code (min 6 chars)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = localError != null,
                supportingText = localError?.let { msg -> { Text(msg) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_rotate_code_input")
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = revokeExisting,
                    onCheckedChange = { revokeExisting = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = JdmcLightYellow,
                        checkmarkColor = JdmcObsidianBlack
                    )
                )
                Text(
                    text = "Revoke all active clipper sessions immediately",
                    style = MaterialTheme.typography.bodySmall,
                    color = JdmcBrushedSilver
                )
            }
            Button(
                onClick = {
                    if (newCode.trim().length < 6) {
                        localError = "Code must be at least 6 characters."
                    } else {
                        val candidate = newCode.trim()
                        onRotateCode(candidate, revokeExisting) {
                            newCode = ""
                        }
                    }
                },
                enabled = !isBusy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JdmcLightYellow,
                    contentColor = JdmcObsidianBlack
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Shared Access Code")
            }
            OutlinedButton(
                onClick = onRevokeAllSessions,
                enabled = !isBusy,
                border = BorderStroke(1.dp, JdmcGoldBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = JdmcAmberAlert),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("revoke_all_sessions_button")
            ) {
                Text("Revoke All Active Clipper Sessions")
            }
        }
    }
}

@Composable
private fun StreamerRosterManagementCard(
    streamers: List<StreamerProfile>,
    jabatanPositions: List<JabatanPosition>,
    isBusy: Boolean,
    onAddStreamerClick: () -> Unit,
    onChangeStreamerJabatan: (StreamerProfile, String) -> Unit,
    onToggleStreamerActive: (StreamerProfile) -> Unit
) {
    Surface(
        color = JdmcCarbonSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, JdmcSteelOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "JDMC Member & Streamer Roster",
                    style = MaterialTheme.typography.titleMedium,
                    color = JdmcLightYellow
                )
                OutlinedButton(
                    onClick = onAddStreamerClick,
                    enabled = !isBusy,
                    border = BorderStroke(1.dp, JdmcGoldBorder)
                ) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = JdmcLightYellow)
                    Spacer(Modifier.width(6.dp))
                    Text("Add", color = JdmcLightYellow)
                }
            }
            streamers.forEach { streamer ->
                var menuOpen by remember { mutableStateOf(false) }
                Surface(
                    color = JdmcCarbonElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, JdmcSteelOutline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${streamer.name} (${streamer.channelHandle})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = JdmcMetallicSilver
                                )
                                if (streamer.specialty.isNotBlank()) {
                                    Text(
                                        text = streamer.specialty,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = JdmcGunmetal
                                    )
                                }
                            }
                            Switch(
                                checked = streamer.isActive,
                                onCheckedChange = { onToggleStreamerActive(streamer) },
                                enabled = !isBusy
                            )
                        }
                        Box {
                            OutlinedButton(
                                onClick = { menuOpen = true },
                                enabled = !isBusy,
                                border = BorderStroke(1.dp, JdmcGoldBorder),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Jabatan: ${streamer.jabatan}", style = MaterialTheme.typography.labelSmall, color = JdmcLightYellow)
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = JdmcLightYellow, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                jabatanPositions.sortedBy { it.tierLevel }.forEach { pos ->
                                    DropdownMenuItem(
                                        text = { Text("${pos.rankTitle} (${pos.indonesianTitle})") },
                                        onClick = {
                                            menuOpen = false
                                            onChangeStreamerJabatan(streamer, pos.rankTitle)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddStreamerDialog(
    jabatanPositions: List<JabatanPosition>,
    onDismiss: () -> Unit,
    onAdd: (name: String, handle: String, jabatan: String, specialty: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }
    var selectedJabatan by remember {
        mutableStateOf(jabatanPositions.firstOrNull()?.rankTitle ?: "Official Clipper")
    }
    var specialty by remember { mutableStateOf("") }
    var jabatanExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JdmcCarbonSurface,
        title = { Text("Add JDMC Member / Streamer", color = JdmcLightYellow) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Member / Streamer Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = handle,
                    onValueChange = { handle = it },
                    label = { Text("Channel Handle *") },
                    placeholder = { Text("@handle") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { jabatanExpanded = true },
                        border = BorderStroke(1.dp, JdmcSteelOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Jabatan: $selectedJabatan", color = JdmcLightYellow)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = JdmcBrushedSilver)
                        }
                    }
                    DropdownMenu(expanded = jabatanExpanded, onDismissRequest = { jabatanExpanded = false }) {
                        jabatanPositions.sortedBy { it.tierLevel }.forEach { pos ->
                            DropdownMenuItem(
                                text = { Text("${pos.rankTitle} (${pos.indonesianTitle})") },
                                onClick = {
                                    selectedJabatan = pos.rankTitle
                                    jabatanExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Division / Specialty") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && handle.isNotBlank()) {
                        onAdd(name, handle, selectedJabatan, specialty)
                    }
                },
                enabled = name.isNotBlank() && handle.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JdmcLightYellow,
                    contentColor = JdmcObsidianBlack
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Member")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = JdmcGunmetal)
            }
        }
    )
}

@Composable
private fun AddJabatanDialog(
    nextTierLevel: Int,
    onDismiss: () -> Unit,
    onAdd: (rankTitle: String, indonesianTitle: String, tierLevel: Int, responsibilities: String, isOfficer: Boolean) -> Unit
) {
    var rankTitle by remember { mutableStateOf("") }
    var indonesianTitle by remember { mutableStateOf("") }
    var tierInput by remember { mutableStateOf(nextTierLevel.toString()) }
    var responsibilities by remember { mutableStateOf("") }
    var isOfficer by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JdmcCarbonSurface,
        title = { Text("Add JDMC Jabatan", color = JdmcLightYellow) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = rankTitle,
                    onValueChange = { rankTitle = it },
                    label = { Text("Jabatan Title (English) *") },
                    placeholder = { Text("e.g. Tail Gunner") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = indonesianTitle,
                    onValueChange = { indonesianTitle = it },
                    label = { Text("Nama Jabatan (ID) *") },
                    placeholder = { Text("e.g. Pengawal Belakang") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tierInput,
                    onValueChange = { tierInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Tier Level (1 = highest)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = responsibilities,
                    onValueChange = { responsibilities = it },
                    label = { Text("Tugas & Wewenang *") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isOfficer,
                        onCheckedChange = { isOfficer = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = JdmcLightYellow,
                            checkmarkColor = JdmcObsidianBlack
                        )
                    )
                    Text("Officer / Pengurus Inti", color = JdmcBrushedSilver, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tier = tierInput.toIntOrNull()?.coerceAtLeast(1) ?: nextTierLevel
                    if (rankTitle.isNotBlank() && indonesianTitle.isNotBlank() && responsibilities.isNotBlank()) {
                        onAdd(rankTitle, indonesianTitle, tier, responsibilities, isOfficer)
                    }
                },
                enabled = rankTitle.isNotBlank() && indonesianTitle.isNotBlank() && responsibilities.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JdmcLightYellow,
                    contentColor = JdmcObsidianBlack
                )
            ) {
                Text("Save Jabatan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = JdmcGunmetal)
            }
        }
    )
}
