package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.domain.model.BackendStatus
import com.example.domain.model.ClipSubmission
import com.example.domain.model.ClipperSession
import com.example.domain.model.JabatanPosition
import com.example.domain.model.StreamerProfile
import com.example.domain.model.SubmissionStatus
import com.example.domain.model.UiState
import com.example.domain.model.VideoPlatform
import com.example.ui.components.BackendUnavailableBanner
import com.example.ui.components.ClipPlayerDialog
import com.example.ui.components.ClipThumbnailView
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
import com.example.ui.theme.StatusApprovedYellow
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StatusPublishedYellow
import com.example.ui.theme.StatusRejectedSilverAmber
import com.example.util.VideoUrlHelper

enum class ClipperDashboardSection(val label: String) {
    WELCOME("Welcome"),
    SUBMIT("Submit New Clip"),
    MY_SUBMISSIONS("My Submissions"),
    STATUS("Submission Status"),
    PUBLISHED("Published Clips"),
    GUIDELINES("Clipper Guidelines")
}

@Composable
fun ClipperRoomScreen(
    backendStatus: BackendStatus,
    clipperSession: ClipperSession?,
    remainingSeconds: Long,
    rateLimitSeconds: Int,
    isVerifyingCode: Boolean,
    authMessage: String?,
    authIsUnavailable: Boolean,
    mySubmissionsState: UiState<List<ClipSubmission>>,
    publishedClipsState: UiState<List<ClipSubmission>>,
    streamers: List<StreamerProfile>,
    jabatanPositions: List<JabatanPosition>,
    isSubmittingClip: Boolean,
    isUploadingThumbnail: Boolean,
    actionBanner: String?,
    onVerifyAccessCode: (String) -> Unit,
    onValidateSession: () -> Unit,
    onLogout: () -> Unit,
    onRefreshSubmissions: () -> Unit,
    onUploadThumbnail: (android.net.Uri, (String) -> Unit) -> Unit,
    onSubmitClip: (String, String, String, StreamerProfile, VideoPlatform, String, String, String, () -> Unit) -> Unit,
    onUpdateClip: (String, String, String, String, StreamerProfile, VideoPlatform, String, String, String, () -> Unit) -> Unit,
    onWithdrawClip: (String) -> Unit,
    onClearBanner: () -> Unit,
    onConfigureBackend: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (clipperSession == null) {
        ClipperAccessGate(
            backendStatus = backendStatus,
            rateLimitSeconds = rateLimitSeconds,
            isVerifying = isVerifyingCode,
            authMessage = authMessage,
            authIsUnavailable = authIsUnavailable,
            onVerifyAccessCode = onVerifyAccessCode,
            onConfigureBackend = onConfigureBackend,
            modifier = modifier
        )
    } else {
        ClipperDashboard(
            backendStatus = backendStatus,
            remainingSeconds = remainingSeconds,
            mySubmissionsState = mySubmissionsState,
            publishedClipsState = publishedClipsState,
            streamers = streamers,
            jabatanPositions = jabatanPositions,
            isSubmittingClip = isSubmittingClip,
            isUploadingThumbnail = isUploadingThumbnail,
            actionBanner = actionBanner,
            onValidateSession = onValidateSession,
            onLogout = onLogout,
            onRefreshSubmissions = onRefreshSubmissions,
            onUploadThumbnail = onUploadThumbnail,
            onSubmitClip = onSubmitClip,
            onUpdateClip = onUpdateClip,
            onWithdrawClip = onWithdrawClip,
            onClearBanner = onClearBanner,
            onConfigureBackend = onConfigureBackend,
            modifier = modifier
        )
    }
}

@Composable
private fun ClipperAccessGate(
    backendStatus: BackendStatus,
    rateLimitSeconds: Int,
    isVerifying: Boolean,
    authMessage: String?,
    authIsUnavailable: Boolean,
    onVerifyAccessCode: (String) -> Unit,
    onConfigureBackend: () -> Unit,
    modifier: Modifier = Modifier
) {
    var accessCodeInput by remember { mutableStateOf("") }
    var codeVisible by remember { mutableStateOf(false) }

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
                                text = "Clipper Access",
                                style = MaterialTheme.typography.headlineMedium,
                                color = JdmcLightYellow
                            )
                            Text(
                                text = "JET DARAT MC • OFFICIAL SHARED ENTRY",
                                style = MaterialTheme.typography.labelSmall,
                                color = JdmcGunmetal
                            )
                        }
                    }

                    Text(
                        text = "Enter the single shared JDMC Clipper Access Code issued to official clippers. No user registration, personal accounts, email authentication, or personal passwords are required.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JdmcBrushedSilver
                    )

                    Surface(
                        color = JdmcCarbonElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, JdmcSteelOutline)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = JdmcLightYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Scope Notice: The shared access code unlocks the submission room only. It never grants administrator moderation privileges.",
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
                        value = accessCodeInput,
                        onValueChange = { accessCodeInput = it },
                        label = { Text("Shared JDMC Clipper Access Code") },
                        placeholder = { Text("Enter official access code") },
                        singleLine = true,
                        visualTransformation = if (codeVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { codeVisible = !codeVisible }) {
                                Icon(
                                    imageVector = if (codeVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (codeVisible) "Hide code" else "Show code",
                                    tint = JdmcGunmetal
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clipper_access_code_input")
                    )

                    Button(
                        onClick = {
                            val submittedCode = accessCodeInput
                            accessCodeInput = ""
                            onVerifyAccessCode(submittedCode)
                        },
                        enabled = !isVerifying && rateLimitSeconds == 0 && accessCodeInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JdmcLightYellow,
                            contentColor = JdmcObsidianBlack
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verify_access_code_button")
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = JdmcObsidianBlack
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Validating with Backend...")
                        } else {
                            Text("Verify Access Code")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClipperDashboard(
    backendStatus: BackendStatus,
    remainingSeconds: Long,
    mySubmissionsState: UiState<List<ClipSubmission>>,
    publishedClipsState: UiState<List<ClipSubmission>>,
    streamers: List<StreamerProfile>,
    jabatanPositions: List<JabatanPosition>,
    isSubmittingClip: Boolean,
    isUploadingThumbnail: Boolean,
    actionBanner: String?,
    onValidateSession: () -> Unit,
    onLogout: () -> Unit,
    onRefreshSubmissions: () -> Unit,
    onUploadThumbnail: (android.net.Uri, (String) -> Unit) -> Unit,
    onSubmitClip: (String, String, String, StreamerProfile, VideoPlatform, String, String, String, () -> Unit) -> Unit,
    onUpdateClip: (String, String, String, String, StreamerProfile, VideoPlatform, String, String, String, () -> Unit) -> Unit,
    onWithdrawClip: (String) -> Unit,
    onClearBanner: () -> Unit,
    onConfigureBackend: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSection by rememberSaveable { mutableStateOf(ClipperDashboardSection.WELCOME) }
    var editingClip by remember { mutableStateOf<ClipSubmission?>(null) }
    var withdrawingClip by remember { mutableStateOf<ClipSubmission?>(null) }
    var activePlayerClip by remember { mutableStateOf<ClipSubmission?>(null) }

    activePlayerClip?.let { clip ->
        ClipPlayerDialog(clip = clip, onDismiss = { activePlayerClip = null })
    }

    editingClip?.let { clip ->
        Dialog(
            onDismissRequest = { editingClip = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                item {
                    ClipSubmissionFormCard(
                        streamers = streamers,
                        jabatanPositions = jabatanPositions,
                        initialClip = clip,
                        isSubmitting = isSubmittingClip,
                        isUploadingThumbnail = isUploadingThumbnail,
                        backendAvailable = backendStatus.isAvailable,
                        onUploadThumbnail = onUploadThumbnail,
                        submitButtonLabel = "Save Submission Changes",
                        onSubmit = { name, jabatan, title, streamer, platform, url, thumb, desc ->
                            onUpdateClip(clip.id, name, jabatan, title, streamer, platform, url, thumb, desc) {
                                editingClip = null
                            }
                        }
                    )
                }
            }
        }
    }

    withdrawingClip?.let { clip ->
        AlertDialog(
            onDismissRequest = { withdrawingClip = null },
            containerColor = JdmcCarbonSurface,
            title = { Text("Withdraw Submission?", color = JdmcLightYellow) },
            text = {
                Text(
                    "This will permanently withdraw \"${clip.title}\" from JDMC moderation review.",
                    color = JdmcBrushedSilver
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onWithdrawClip(clip.id)
                        withdrawingClip = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JdmcLightYellow,
                        contentColor = JdmcObsidianBlack
                    )
                ) {
                    Text("Withdraw")
                }
            },
            dismissButton = {
                TextButton(onClick = { withdrawingClip = null }) {
                    Text("Cancel", color = JdmcGunmetal)
                }
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
                roleLabel = "Clipper Session Active",
                remainingSeconds = remainingSeconds,
                onRefreshCheck = onValidateSession,
                onLogout = onLogout
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
                items(ClipperDashboardSection.entries) { section ->
                    FilterChip(
                        selected = activeSection == section,
                        onClick = { activeSection = section },
                        label = { Text(section.label) },
                        colors = jdmcFilterChipColors(),
                        modifier = Modifier.testTag("clipper_section_${section.name.lowercase()}")
                    )
                }
            }
        }

        when (activeSection) {
            ClipperDashboardSection.WELCOME -> {
                item {
                    Surface(
                        color = JdmcCarbonSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, JdmcSteelOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Welcome to the JDMC Clipper Room",
                                style = MaterialTheme.typography.headlineMedium,
                                color = JdmcLightYellow
                            )
                            Text(
                                text = "Your shared clipper session is active. Submit highlights, tag club Jabatan attribution, monitor review notes, and follow official JDMC clipping standards.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = JdmcBrushedSilver
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { activeSection = ClipperDashboardSection.SUBMIT },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = JdmcLightYellow,
                                        contentColor = JdmcObsidianBlack
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Submit New Clip")
                                }
                                OutlinedButton(
                                    onClick = { activeSection = ClipperDashboardSection.MY_SUBMISSIONS },
                                    border = BorderStroke(1.dp, JdmcSteelOutline),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("My Submissions", color = JdmcMetallicSilver)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { activeSection = ClipperDashboardSection.STATUS },
                                    border = BorderStroke(1.dp, JdmcSteelOutline),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Submission Status", color = JdmcBrushedSilver)
                                }
                                OutlinedButton(
                                    onClick = { activeSection = ClipperDashboardSection.GUIDELINES },
                                    border = BorderStroke(1.dp, JdmcSteelOutline),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Guidelines", color = JdmcBrushedSilver)
                                }
                            }
                        }
                    }
                }
            }

            ClipperDashboardSection.SUBMIT -> {
                item {
                    ClipSubmissionFormCard(
                        streamers = streamers,
                        jabatanPositions = jabatanPositions,
                        isSubmitting = isSubmittingClip,
                        isUploadingThumbnail = isUploadingThumbnail,
                        backendAvailable = backendStatus.isAvailable,
                        onUploadThumbnail = onUploadThumbnail,
                        onSubmit = { name, jabatan, title, streamer, platform, url, thumb, desc ->
                            onSubmitClip(name, jabatan, title, streamer, platform, url, thumb, desc) {
                                activeSection = ClipperDashboardSection.MY_SUBMISSIONS
                            }
                        }
                    )
                }
            }

            ClipperDashboardSection.MY_SUBMISSIONS -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MY SUBMISSIONS",
                            style = MaterialTheme.typography.titleMedium,
                            color = JdmcLightYellow
                        )
                        OutlinedButton(onClick = onRefreshSubmissions) {
                            Text("Refresh", style = MaterialTheme.typography.labelMedium, color = JdmcLightYellow)
                        }
                    }
                }

                when (mySubmissionsState) {
                    is UiState.Loading -> item {
                        CircularProgressIndicator(color = JdmcLightYellow)
                    }
                    is UiState.BackendUnavailable -> item {
                        BackendUnavailableBanner(
                            reason = mySubmissionsState.reason,
                            onConfigureClick = onConfigureBackend
                        )
                    }
                    is UiState.Error -> item {
                        Text(mySubmissionsState.message, color = JdmcAmberAlert)
                    }
                    is UiState.Success -> {
                        val list = mySubmissionsState.data
                        if (list.isEmpty()) {
                            item {
                                Surface(
                                    color = JdmcCarbonSurface,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, JdmcSteelOutline),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "No submissions found for this session yet.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = JdmcGunmetal,
                                        modifier = Modifier.padding(18.dp)
                                    )
                                }
                            }
                        } else {
                            items(list, key = { it.id }) { clip ->
                                ClipperSubmissionManageCard(
                                    clip = clip,
                                    onEdit = { editingClip = clip },
                                    onWithdraw = { withdrawingClip = clip }
                                )
                            }
                        }
                    }
                    UiState.Idle -> Unit
                }
            }

            ClipperDashboardSection.STATUS -> {
                val itemsList = (mySubmissionsState as? UiState.Success)?.data.orEmpty()
                item {
                    StatusPipelineSummaryCard(submissions = itemsList)
                }
                items(itemsList, key = { "status_${it.id}" }) { clip ->
                    ClipperSubmissionManageCard(
                        clip = clip,
                        onEdit = { editingClip = clip },
                        onWithdraw = { withdrawingClip = clip }
                    )
                }
            }

            ClipperDashboardSection.PUBLISHED -> {
                val published = (publishedClipsState as? UiState.Success)?.data.orEmpty()
                if (published.isEmpty()) {
                    item {
                        Surface(
                            color = JdmcCarbonSurface,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, JdmcSteelOutline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No published clips available from the backend yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = JdmcGunmetal,
                                modifier = Modifier.padding(18.dp)
                            )
                        }
                    }
                } else {
                    items(published, key = { "room_pub_${it.id}" }) { clip ->
                        PublishedClipCard(
                            clip = clip,
                            isHighlighted = clip.isFeatured,
                            onPlayInApp = { activePlayerClip = clip }
                        )
                    }
                }
            }

            ClipperDashboardSection.GUIDELINES -> {
                item {
                    ClipperGuidelinesCard()
                }
            }
        }
    }
}

@Composable
private fun ClipperSubmissionManageCard(
    clip: ClipSubmission,
    onEdit: () -> Unit,
    onWithdraw: () -> Unit
) {
    Surface(
        color = JdmcCarbonSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, JdmcSteelOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ClipThumbnailView(
                thumbnailUrl = clip.thumbnailUrl,
                contentDescription = clip.title
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubmissionStatusChip(status = clip.status)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    JabatanBadge(jabatan = clip.clipperJabatan)
                    PlatformBadge(platform = clip.platform)
                }
            }
            Text(
                text = clip.title,
                style = MaterialTheme.typography.titleMedium,
                color = JdmcLightYellow
            )
            Text(
                text = "Streamer: ${clip.streamerName} (${clip.streamerJabatan}) • Submitted ${VideoUrlHelper.formatDisplayDate(clip.submittedAtIso)}",
                style = MaterialTheme.typography.bodySmall,
                color = JdmcGunmetal
            )
            if (!clip.moderationNote.isNullOrBlank()) {
                Surface(
                    color = JdmcCarbonElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, JdmcSteelOutline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Gavel,
                            contentDescription = "Moderation Note",
                            tint = JdmcLightYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Moderation Note: ${clip.moderationNote}",
                            style = MaterialTheme.typography.bodySmall,
                            color = JdmcBrushedSilver
                        )
                    }
                }
            }

            if (clip.canEditOrWithdraw) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        border = BorderStroke(1.dp, JdmcSteelOutline),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(15.dp), tint = JdmcLightYellow)
                        Spacer(Modifier.width(6.dp))
                        Text("Edit", color = JdmcLightYellow)
                    }
                    OutlinedButton(
                        onClick = onWithdraw,
                        border = BorderStroke(1.dp, JdmcGoldBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JdmcAmberAlert),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Withdraw")
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = JdmcGunmetal,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Read-only: Only verified submission owners can edit or withdraw.",
                        style = MaterialTheme.typography.labelSmall,
                        color = JdmcGunmetal
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPipelineSummaryCard(submissions: List<ClipSubmission>) {
    val pending = submissions.count { it.status == SubmissionStatus.PENDING_REVIEW }
    val approved = submissions.count { it.status == SubmissionStatus.APPROVED }
    val rejected = submissions.count { it.status == SubmissionStatus.REJECTED }
    val published = submissions.count { it.status == SubmissionStatus.PUBLISHED }

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
            Text(
                text = "Submission Status Breakdown",
                style = MaterialTheme.typography.titleMedium,
                color = JdmcLightYellow
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PipelineCountTile("Pending", pending, StatusPendingAmber, Modifier.weight(1f))
                PipelineCountTile("Approved", approved, StatusApprovedYellow, Modifier.weight(1f))
                PipelineCountTile("Rejected", rejected, StatusRejectedSilverAmber, Modifier.weight(1f))
                PipelineCountTile("Published", published, StatusPublishedYellow, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PipelineCountTile(
    label: String,
    count: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = JdmcCarbonElevated,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, JdmcSteelOutline),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = accent
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = JdmcGunmetal
            )
        }
    }
}

@Composable
private fun ClipperGuidelinesCard() {
    val guidelines = listOf(
        "1. Confidential Shared Access Code" to "Never share, screenshot, or post the official JDMC Clipper Access Code publicly. Admins rotate the code and revoke sessions on leaks.",
        "2. Approved Platforms Only" to "Submit valid HTTPS links from TikTok, YouTube, YouTube Shorts, or other JDMC-approved video hosts.",
        "3. Accurate Streamer & Jabatan Attribution" to "Always tag the original JDMC streamer and Jabatan rank featured in the clip. Misattributed clips are reassigned or rejected.",
        "4. Broadcast-Ready Quality" to "Maintain clean 1080p/60fps captures where possible, readable thumbnails, and concise titles without spam tags.",
        "5. Ownership & Moderation Notes" to "You may edit or withdraw submissions verified under your session receipt. Check moderation notes before resubmitting rejected clips."
    )
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
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = JdmcLightYellow
                )
                Text(
                    text = "Official JDMC Clipper Guidelines",
                    style = MaterialTheme.typography.headlineSmall,
                    color = JdmcLightYellow
                )
            }
            guidelines.forEach { (heading, body) ->
                Surface(
                    color = JdmcCarbonElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, JdmcSteelOutline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = heading,
                            style = MaterialTheme.typography.titleSmall,
                            color = JdmcLightYellow
                        )
                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodySmall,
                            color = JdmcBrushedSilver
                        )
                    }
                }
            }
        }
    }
}
