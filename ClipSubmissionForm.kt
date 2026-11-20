package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.model.ClipSubmission
import com.example.domain.model.JabatanPosition
import com.example.domain.model.StreamerProfile
import com.example.domain.model.VideoPlatform
import com.example.ui.components.ClipThumbnailView
import com.example.ui.theme.JdmcBrushedSilver
import com.example.ui.theme.JdmcCarbonElevated
import com.example.ui.theme.JdmcCarbonSurface
import com.example.ui.theme.JdmcAmberAlert
import com.example.ui.theme.JdmcGunmetal
import com.example.ui.theme.JdmcLightYellow
import com.example.ui.theme.JdmcMetallicSilver
import com.example.ui.theme.JdmcObsidianBlack
import com.example.ui.theme.JdmcSteelOutline
import com.example.util.VideoUrlHelper

@Composable
fun ClipSubmissionFormCard(
    streamers: List<StreamerProfile>,
    jabatanPositions: List<JabatanPosition>,
    initialClip: ClipSubmission? = null,
    isSubmitting: Boolean,
    isUploadingThumbnail: Boolean,
    backendAvailable: Boolean,
    onUploadThumbnail: (Uri, (String) -> Unit) -> Unit,
    onSubmit: (
        clipperDisplayName: String,
        clipperJabatan: String,
        title: String,
        streamer: StreamerProfile,
        platform: VideoPlatform,
        videoUrl: String,
        thumbnailUrl: String,
        description: String
    ) -> Unit,
    submitButtonLabel: String = "Submit Clip for Review",
    modifier: Modifier = Modifier
) {
    val activeStreamers = remember(streamers) {
        val list = streamers.filter { it.isActive }
        list.ifEmpty { streamers }
    }

    var clipperDisplayName by remember(initialClip) { mutableStateOf(initialClip?.clipperDisplayName.orEmpty()) }
    var clipperJabatan by remember(initialClip, jabatanPositions) {
        mutableStateOf(
            initialClip?.clipperJabatan?.takeIf { it.isNotBlank() }
                ?: jabatanPositions.firstOrNull { it.id == "official-clipper" }?.rankTitle
                ?: "Official Clipper"
        )
    }
    var title by remember(initialClip) { mutableStateOf(initialClip?.title.orEmpty()) }
    var selectedStreamer by remember(initialClip, activeStreamers) {
        mutableStateOf(
            activeStreamers.firstOrNull { it.id == initialClip?.streamerId }
                ?: activeStreamers.firstOrNull()
        )
    }
    var selectedPlatform by remember(initialClip) {
        mutableStateOf(initialClip?.platform ?: VideoPlatform.YOUTUBE)
    }
    var videoUrl by remember(initialClip) { mutableStateOf(initialClip?.videoUrl.orEmpty()) }
    var thumbnailUrl by remember(initialClip) { mutableStateOf(initialClip?.thumbnailUrl.orEmpty()) }
    var description by remember(initialClip) { mutableStateOf(initialClip?.description.orEmpty()) }
    val submissionDateDisplay = remember(initialClip) {
        VideoUrlHelper.formatDisplayDate(initialClip?.submittedAtIso ?: VideoUrlHelper.currentIsoTimestamp())
    }

    var jabatanMenuExpanded by remember { mutableStateOf(false) }
    var streamerMenuExpanded by remember { mutableStateOf(false) }
    var platformMenuExpanded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onUploadThumbnail(uri) { uploadedUrl ->
                thumbnailUrl = uploadedUrl
            }
        }
    }

    Surface(
        color = JdmcCarbonSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, JdmcSteelOutline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (initialClip == null) "Submit New Clip" else "Edit Submission",
                style = MaterialTheme.typography.headlineSmall,
                color = JdmcLightYellow
            )
            Text(
                text = "All fields below are required before moderation review.",
                style = MaterialTheme.typography.bodySmall,
                color = JdmcGunmetal
            )

            OutlinedTextField(
                value = clipperDisplayName,
                onValueChange = { clipperDisplayName = it; validationError = null },
                label = { Text("Clipper Display Name *") },
                placeholder = { Text("e.g. ApexKuroClips") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("clip_clipper_name_input")
            )

            // Clipper Jabatan Selector
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { jabatanMenuExpanded = true },
                    border = BorderStroke(1.dp, JdmcSteelOutline),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clip_jabatan_dropdown")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Jabatan Pengirim (Club Rank) *",
                                style = MaterialTheme.typography.labelSmall,
                                color = JdmcGunmetal
                            )
                            Text(
                                text = clipperJabatan,
                                style = MaterialTheme.typography.bodyMedium,
                                color = JdmcLightYellow
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Choose jabatan",
                            tint = JdmcBrushedSilver
                        )
                    }
                }
                DropdownMenu(
                    expanded = jabatanMenuExpanded,
                    onDismissRequest = { jabatanMenuExpanded = false }
                ) {
                    jabatanPositions.sortedBy { it.tierLevel }.forEach { pos ->
                        DropdownMenuItem(
                            text = { Text("${pos.rankTitle} (${pos.indonesianTitle})") },
                            onClick = {
                                clipperJabatan = pos.rankTitle
                                jabatanMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it; validationError = null },
                label = { Text("Video Title *") },
                placeholder = { Text("e.g. Konvoi RX-King 2-Tak Malam Los Santos") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("clip_title_input")
            )

            // Streamer Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { streamerMenuExpanded = true },
                    border = BorderStroke(1.dp, JdmcSteelOutline),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clip_streamer_dropdown")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Related JDMC Streamer & Jabatan *",
                                style = MaterialTheme.typography.labelSmall,
                                color = JdmcGunmetal
                            )
                            Text(
                                text = selectedStreamer?.let { "${it.name} — ${it.jabatan} (${it.channelHandle})" } ?: "Select JDMC streamer",
                                style = MaterialTheme.typography.bodyMedium,
                                color = JdmcMetallicSilver
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Choose streamer",
                            tint = JdmcBrushedSilver
                        )
                    }
                }
                DropdownMenu(
                    expanded = streamerMenuExpanded,
                    onDismissRequest = { streamerMenuExpanded = false }
                ) {
                    activeStreamers.forEach { streamer ->
                        DropdownMenuItem(
                            text = { Text("${streamer.name} • ${streamer.jabatan} (${streamer.channelHandle})") },
                            onClick = {
                                selectedStreamer = streamer
                                streamerMenuExpanded = false
                                validationError = null
                            }
                        )
                    }
                }
            }

            // Platform Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { platformMenuExpanded = true },
                    border = BorderStroke(1.dp, JdmcSteelOutline),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clip_platform_dropdown")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Video Platform *",
                                style = MaterialTheme.typography.labelSmall,
                                color = JdmcGunmetal
                            )
                            Text(
                                text = selectedPlatform.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = JdmcMetallicSilver
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Choose platform",
                            tint = JdmcBrushedSilver
                        )
                    }
                }
                DropdownMenu(
                    expanded = platformMenuExpanded,
                    onDismissRequest = { platformMenuExpanded = false }
                ) {
                    VideoPlatform.entries.forEach { platform ->
                        DropdownMenuItem(
                            text = { Text(platform.displayName) },
                            onClick = {
                                selectedPlatform = platform
                                platformMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = videoUrl,
                onValueChange = { videoUrl = it; validationError = null },
                label = { Text("Video URL (HTTPS) *") },
                placeholder = { Text("https://youtube.com/shorts/...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("clip_video_url_input")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = thumbnailUrl,
                    onValueChange = { thumbnailUrl = it; validationError = null },
                    label = { Text("Thumbnail URL or Upload *") },
                    placeholder = { Text("https://... or upload via Photo Picker") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clip_thumbnail_url_input")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = backendAvailable && !isUploadingThumbnail,
                        border = BorderStroke(1.dp, JdmcSteelOutline),
                        modifier = Modifier.testTag("pick_thumbnail_button")
                    ) {
                        if (isUploadingThumbnail) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = JdmcLightYellow)
                            Spacer(Modifier.width(8.dp))
                            Text("Uploading...")
                        } else {
                            Icon(
                                imageVector = Icons.Filled.PhotoLibrary,
                                contentDescription = "Pick thumbnail image",
                                modifier = Modifier.size(16.dp),
                                tint = JdmcLightYellow
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Upload Thumbnail", color = JdmcLightYellow)
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudUpload,
                            contentDescription = null,
                            tint = JdmcGunmetal,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Photo Picker",
                            style = MaterialTheme.typography.labelSmall,
                            color = JdmcGunmetal
                        )
                    }
                }
                if (thumbnailUrl.isNotBlank()) {
                    ClipThumbnailView(
                        thumbnailUrl = thumbnailUrl,
                        contentDescription = "Clip thumbnail preview"
                    )
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it; validationError = null },
                label = { Text("Video Description *") },
                placeholder = { Text("Describe the moment, event, and streamer context...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("clip_description_input")
            )

            Surface(
                color = JdmcCarbonElevated,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, JdmcSteelOutline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = null,
                            tint = JdmcBrushedSilver,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Submission Date",
                            style = MaterialTheme.typography.labelMedium,
                            color = JdmcBrushedSilver
                        )
                    }
                    Text(
                        text = submissionDateDisplay,
                        style = MaterialTheme.typography.labelMedium,
                        color = JdmcLightYellow
                    )
                }
            }

            if (validationError != null) {
                Text(
                    text = validationError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = JdmcAmberAlert
                )
            }

            Button(
                onClick = {
                    val streamer = selectedStreamer
                    when {
                        clipperDisplayName.isBlank() -> validationError = "Clipper display name is required."
                        title.isBlank() -> validationError = "Video title is required."
                        streamer == null -> validationError = "Select a related JDMC streamer."
                        !VideoUrlHelper.isValidHttpsUrl(videoUrl) -> validationError = "Enter a valid https:// video URL."
                        !VideoUrlHelper.isValidHttpsUrl(thumbnailUrl) -> validationError = "Provide a valid https:// thumbnail URL or upload an image."
                        description.isBlank() -> validationError = "Video description is required."
                        else -> onSubmit(
                            clipperDisplayName,
                            clipperJabatan,
                            title,
                            streamer,
                            selectedPlatform,
                            videoUrl,
                            thumbnailUrl,
                            description
                        )
                    }
                },
                enabled = backendAvailable && !isSubmitting && !isUploadingThumbnail,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JdmcLightYellow,
                    contentColor = JdmcObsidianBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_clip_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = JdmcObsidianBlack
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Submitting...")
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(submitButtonLabel)
                }
            }
        }
    }
}
