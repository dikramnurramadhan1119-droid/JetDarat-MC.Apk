package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.ClipSubmission
import com.example.domain.model.JabatanPosition
import com.example.domain.model.StreamerProfile
import com.example.domain.model.UiState
import com.example.domain.model.VideoPlatform
import com.example.ui.components.BackendUnavailableBanner
import com.example.ui.components.ClipPlayerDialog
import com.example.ui.components.ClipThumbnailView
import com.example.ui.components.FeaturedBadge
import com.example.ui.components.JabatanBadge
import com.example.ui.components.PlatformBadge
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

enum class GallerySubView(val label: String) {
    CLIPS("Published Clips"),
    JABATAN("Struktur & Jabatan")
}

@Composable
fun PublicGalleryScreen(
    publishedClipsState: UiState<List<ClipSubmission>>,
    filteredClips: List<ClipSubmission>,
    streamers: List<StreamerProfile>,
    jabatanPositions: List<JabatanPosition>,
    selectedPlatform: VideoPlatform?,
    selectedStreamerId: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onPlatformSelected: (VideoPlatform?) -> Unit,
    onStreamerSelected: (String?) -> Unit,
    onRefresh: () -> Unit,
    onConfigureBackend: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubView by rememberSaveable { mutableStateOf(GallerySubView.CLIPS) }
    var activePlayerClip by remember { mutableStateOf<ClipSubmission?>(null) }

    activePlayerClip?.let { clip ->
        ClipPlayerDialog(
            clip = clip,
            onDismiss = { activePlayerClip = null }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GalleryHeroBanner(onRefresh = onRefresh)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GallerySubView.entries.forEach { view ->
                    FilterChip(
                        selected = activeSubView == view,
                        onClick = { activeSubView = view },
                        leadingIcon = {
                            Icon(
                                imageVector = if (view == GallerySubView.CLIPS) Icons.Filled.Movie else Icons.Filled.MilitaryTech,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(view.label) },
                        colors = jdmcFilterChipColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gallery_subview_${view.name.lowercase()}")
                    )
                }
            }
        }

        if (activeSubView == GallerySubView.JABATAN) {
            item {
                JabatanHierarchyHeaderCard(totalPositions = jabatanPositions.size, totalMembers = streamers.size)
            }
            items(jabatanPositions.sortedBy { it.tierLevel }, key = { it.id }) { position ->
                JabatanPositionCard(
                    position = position,
                    assignedMembers = streamers.filter {
                        it.isActive && it.jabatan.equals(position.rankTitle, ignoreCase = true)
                    }
                )
            }
            item {
                Text(
                    text = "ROSTER ANGGOTA & STREAMER AKTIF",
                    style = MaterialTheme.typography.titleMedium,
                    color = JdmcLightYellow
                )
            }
            items(streamers.filter { it.isActive }, key = { "roster_${it.id}" }) { streamer ->
                RosterMemberCard(streamer = streamer)
            }
        } else {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search clips",
                            tint = JdmcGunmetal
                        )
                    },
                    placeholder = { Text("Search clip title, clipper, streamer, or jabatan...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gallery_search_input")
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "PLATFORM FILTER",
                        style = MaterialTheme.typography.labelSmall,
                        color = JdmcGunmetal
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedPlatform == null,
                                onClick = { onPlatformSelected(null) },
                                label = { Text("All Platforms") },
                                colors = jdmcFilterChipColors()
                            )
                        }
                        items(VideoPlatform.entries) { platform ->
                            FilterChip(
                                selected = selectedPlatform == platform,
                                onClick = {
                                    onPlatformSelected(if (selectedPlatform == platform) null else platform)
                                },
                                label = { Text(platform.displayName) },
                                colors = jdmcFilterChipColors()
                            )
                        }
                    }
                    Text(
                        text = "JDMC STREAMER & JABATAN",
                        style = MaterialTheme.typography.labelSmall,
                        color = JdmcGunmetal
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedStreamerId == null,
                                onClick = { onStreamerSelected(null) },
                                label = { Text("All Streamers") },
                                colors = jdmcFilterChipColors()
                            )
                        }
                        items(streamers.filter { it.isActive }, key = { it.id }) { streamer ->
                            FilterChip(
                                selected = selectedStreamerId == streamer.id,
                                onClick = {
                                    onStreamerSelected(if (selectedStreamerId == streamer.id) null else streamer.id)
                                },
                                label = { Text("${streamer.name} (${streamer.jabatan})") },
                                colors = jdmcFilterChipColors()
                            )
                        }
                    }
                }
            }

            when (publishedClipsState) {
                is UiState.Loading -> {
                    item {
                        Surface(
                            color = JdmcCarbonSurface,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, JdmcSteelOutline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(color = JdmcLightYellow)
                                Text(
                                    text = "Syncing published clips from JDMC backend...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = JdmcBrushedSilver
                                )
                            }
                        }
                    }
                }

                is UiState.BackendUnavailable -> {
                    item {
                        BackendUnavailableBanner(
                            reason = publishedClipsState.reason,
                            onConfigureClick = onConfigureBackend
                        )
                    }
                    item {
                        EmptyGalleryPlaceholderCard(
                            title = "Public Gallery Ready for Backend Feed",
                            description = "Approved clips published by JDMC administrators appear here openly for all viewers without requiring an access code."
                        )
                    }
                }

                is UiState.Error -> {
                    item {
                        Surface(
                            color = JdmcCarbonElevated,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, JdmcGoldBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Unable to load published clips",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = JdmcAmberAlert
                                )
                                Text(
                                    text = publishedClipsState.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = JdmcBrushedSilver
                                )
                                OutlinedButton(onClick = onRefresh) {
                                    Text("Retry", color = JdmcLightYellow)
                                }
                            }
                        }
                    }
                }

                is UiState.Success, UiState.Idle -> {
                    if (filteredClips.isEmpty()) {
                        item {
                            EmptyGalleryPlaceholderCard(
                                title = "No Published Clips Match Filter",
                                description = "No approved clips are currently published for the selected filters. Check back soon or clear active filters."
                            )
                        }
                    } else {
                        val featured = filteredClips.filter { it.isFeatured }
                        if (featured.isNotEmpty()) {
                            item {
                                Text(
                                    text = "FEATURED SPOTLIGHT",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = JdmcLightYellow
                                )
                            }
                            items(featured, key = { "featured_${it.id}" }) { clip ->
                                PublishedClipCard(
                                    clip = clip,
                                    isHighlighted = true,
                                    onPlayInApp = { activePlayerClip = clip }
                                )
                            }
                        }

                        val regular = filteredClips.filterNot { it.isFeatured }
                        if (regular.isNotEmpty()) {
                            item {
                                Text(
                                    text = "PUBLISHED CLIPS (${regular.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = JdmcMetallicSilver
                                )
                            }
                            items(regular, key = { it.id }) { clip ->
                                PublishedClipCard(
                                    clip = clip,
                                    isHighlighted = false,
                                    onPlayInApp = { activePlayerClip = clip }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GalleryHeroBanner(onRefresh: () -> Unit) {
    Surface(
        color = JdmcCarbonSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, JdmcGoldBorder.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            JdmcObsidianBlack,
                            JdmcGoldContainer.copy(alpha = 0.45f),
                            JdmcObsidianBlack
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = JdmcLightYellow,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "LOS SANTOS • EST. 2026",
                            style = MaterialTheme.typography.labelSmall,
                            color = JdmcObsidianBlack,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    OutlinedButton(
                        onClick = onRefresh,
                        border = BorderStroke(1.dp, JdmcGoldBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JdmcLightYellow),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("gallery_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh gallery",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Refresh", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.jdmc_club_logo),
                        contentDescription = "JDMC Jet Darat Motorcycle Club Emblem",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "JET DARAT MOTORCYCLE CLUB",
                            style = MaterialTheme.typography.titleLarge,
                            color = JdmcLightYellow
                        )
                        Text(
                            text = "RX-King 2-Tak • Official Clipper Showcase & Struktur Jabatan",
                            style = MaterialTheme.typography.bodySmall,
                            color = JdmcMetallicSilver
                        )
                        Text(
                            text = "Public gallery & club hierarchy open to everyone.",
                            style = MaterialTheme.typography.labelSmall,
                            color = JdmcGunmetal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JabatanHierarchyHeaderCard(
    totalPositions: Int,
    totalMembers: Int
) {
    Surface(
        color = JdmcCarbonSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, JdmcGoldBorder.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    tint = JdmcLightYellow,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "Struktur Jabatan JDMC",
                        style = MaterialTheme.typography.titleLarge,
                        color = JdmcLightYellow
                    )
                    Text(
                        text = "$totalPositions jabatan resmi • $totalMembers member/streamer aktif",
                        style = MaterialTheme.typography.bodySmall,
                        color = JdmcBrushedSilver
                    )
                }
            }
        }
    }
}

@Composable
private fun JabatanPositionCard(
    position: JabatanPosition,
    assignedMembers: List<StreamerProfile>
) {
    Surface(
        color = JdmcCarbonSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (position.isOfficerRole) JdmcGoldBorder else JdmcSteelOutline
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    Surface(
                        color = if (position.isOfficerRole) JdmcLightYellow else JdmcCarbonElevated,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "#${position.tierLevel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (position.isOfficerRole) JdmcObsidianBlack else JdmcBrushedSilver,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = position.rankTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = JdmcLightYellow
                    )
                }
                JabatanBadge(jabatan = position.indonesianTitle)
            }

            Text(
                text = position.responsibilities,
                style = MaterialTheme.typography.bodySmall,
                color = JdmcBrushedSilver
            )

            if (assignedMembers.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = JdmcWarmGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Pejabat: ${assignedMembers.joinToString(", ") { "${it.name} (${it.channelHandle})" }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = JdmcMetallicSilver
                    )
                }
            }
        }
    }
}

@Composable
private fun RosterMemberCard(streamer: StreamerProfile) {
    Surface(
        color = JdmcCarbonElevated,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, JdmcSteelOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
            JabatanBadge(jabatan = streamer.jabatan)
        }
    }
}

@Composable
fun PublishedClipCard(
    clip: ClipSubmission,
    isHighlighted: Boolean,
    onPlayInApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val supportsEmbed = VideoUrlHelper.supportsInAppPlayback(clip.platform, clip.videoUrl)

    Surface(
        color = JdmcCarbonSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isHighlighted || clip.isFeatured) JdmcLightYellow else JdmcSteelOutline
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("published_clip_card_${clip.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box {
                ClipThumbnailView(
                    thumbnailUrl = clip.thumbnailUrl,
                    contentDescription = "Thumbnail for ${clip.title}"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    PlatformBadge(platform = clip.platform)
                    if (clip.isFeatured) {
                        FeaturedBadge()
                    }
                }
            }

            Text(
                text = clip.title,
                style = MaterialTheme.typography.titleLarge,
                color = JdmcLightYellow,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = JdmcBrushedSilver,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Clipper: ${clip.clipperDisplayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = JdmcBrushedSilver
                    )
                    JabatanBadge(jabatan = clip.clipperJabatan)
                }
                Text(
                    text = VideoUrlHelper.formatDisplayDate(clip.publishedAtIso ?: clip.submittedAtIso),
                    style = MaterialTheme.typography.labelSmall,
                    color = JdmcGunmetal
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Videocam,
                    contentDescription = null,
                    tint = JdmcWarmGold,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Streamer: ${clip.streamerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = JdmcMetallicSilver
                )
                if (clip.streamerJabatan.isNotBlank()) {
                    JabatanBadge(jabatan = clip.streamerJabatan)
                }
            }

            if (clip.description.isNotBlank()) {
                Text(
                    text = clip.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = JdmcGunmetal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (supportsEmbed) {
                    Button(
                        onClick = onPlayInApp,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JdmcLightYellow,
                            contentColor = JdmcObsidianBlack
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("play_in_app_button_${clip.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Play Clip")
                    }
                }
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(clip.videoUrl))
                        context.startActivity(intent)
                    },
                    border = BorderStroke(1.dp, JdmcGoldBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = JdmcLightYellow),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("watch_external_button_${clip.id}")
                ) {
                    Text("Watch")
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyGalleryPlaceholderCard(
    title: String,
    description: String
) {
    Surface(
        color = JdmcCarbonSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, JdmcSteelOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Movie,
                contentDescription = null,
                tint = JdmcWarmGold,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = JdmcLightYellow
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = JdmcGunmetal
            )
        }
    }
}

@Composable
fun jdmcFilterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = JdmcCarbonElevated,
    labelColor = JdmcBrushedSilver,
    iconColor = JdmcBrushedSilver,
    selectedContainerColor = JdmcLightYellow,
    selectedLabelColor = JdmcObsidianBlack,
    selectedLeadingIconColor = JdmcObsidianBlack
)
