package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.EventStatus
import com.example.ui.components.OfficerModeBadge
import com.example.ui.components.StatCounterTile
import com.example.ui.theme.JdmcBrushedSilver
import com.example.ui.theme.JdmcCarbonElevated
import com.example.ui.theme.JdmcCarbonSurface
import com.example.ui.theme.JdmcGoldBorder
import com.example.ui.theme.JdmcGoldContainer
import com.example.ui.theme.JdmcGunmetal
import com.example.ui.theme.JdmcLightYellow
import com.example.ui.theme.JdmcMetallicSilver
import com.example.ui.theme.JdmcObsidianBlack
import com.example.ui.theme.JdmcSteelOutline
import com.example.ui.theme.JdmcWarmGold
import com.example.ui.viewmodel.ClockSnapshot
import com.example.ui.viewmodel.JdmcHqUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DashboardQuickDestination {
    MEMBERS,
    ORGANIZATION,
    ARCHIVE,
    ANNOUNCEMENTS,
    EVENTS,
    MEDIA,
    CLIPPER_ROOM,
    ADMIN_PANEL,
    ROAD_AI
}

@Composable
fun DashboardScreen(
    hqState: JdmcHqUiState,
    clock: ClockSnapshot,
    onQuickNavigate: (DashboardQuickDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val upcomingEvents = hqState.events.filter { it.status == EventStatus.UPCOMING }
    val latestAnnouncements = hqState.announcements.take(3)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Official JDMC Crest + Welcome Banner + Club Status
        item {
            Surface(
                color = JdmcCarbonSurface,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, JdmcGoldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    JdmcGoldContainer.copy(alpha = 0.45f),
                                    JdmcCarbonSurface
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = JdmcObsidianBlack,
                                border = BorderStroke(1.5.dp, JdmcLightYellow),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.jdmc_club_logo),
                                    contentDescription = "JDMC Emblem",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "JET DARAT MOTORCYCLE CLUB",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = JdmcLightYellow,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = hqState.settings.clubWelcomeHeadline,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = JdmcMetallicSilver
                                )
                                Text(
                                    text = "\"Satu Jalan, Satu Mimpi, Satu Keluarga.\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = JdmcWarmGold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = JdmcCarbonElevated,
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(1.dp, JdmcGoldBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(JdmcLightYellow)
                                    )
                                    Text(
                                        text = hqState.settings.clubStatusLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = JdmcLightYellow,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            OfficerModeBadge(isOfficerActive = hqState.settings.officerModeActive)
                        }
                    }
                }
            }
        }

        // 2. Live Digital Clock & Telemetry
        item {
            Surface(
                color = JdmcCarbonElevated,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, JdmcSteelOutline),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_digital_clock")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = "Live Clock",
                                tint = JdmcLightYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "CHAPTER TIME • LOS SANTOS",
                                style = MaterialTheme.typography.labelSmall,
                                color = JdmcGunmetal
                            )
                        }
                        Text(
                            text = clock.dateText,
                            style = MaterialTheme.typography.labelMedium,
                            color = JdmcBrushedSilver
                        )
                    }
                    Text(
                        text = clock.timeText,
                        style = MaterialTheme.typography.headlineMedium,
                        color = JdmcLightYellow,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. Member & Club Statistics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCounterTile(
                    label = "Members",
                    value = hqState.members.size.toString(),
                    icon = Icons.Filled.Groups,
                    modifier = Modifier.weight(1f)
                )
                StatCounterTile(
                    label = "Upcoming",
                    value = upcomingEvents.size.toString(),
                    icon = Icons.Filled.CalendarMonth,
                    modifier = Modifier.weight(1f)
                )
                StatCounterTile(
                    label = "Media",
                    value = hqState.mediaItems.size.toString(),
                    icon = Icons.Filled.Collections,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Quick Navigation Menu
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "QUICK NAVIGATION",
                    style = MaterialTheme.typography.titleMedium,
                    color = JdmcLightYellow
                )
                val tiles = listOf(
                    QuickMenuItem("Members", "${hqState.members.size} Profiles", Icons.Filled.Groups, DashboardQuickDestination.MEMBERS),
                    QuickMenuItem("Organization", "${hqState.organizationRoles.size} Ranks", Icons.Filled.AccountTree, DashboardQuickDestination.ORGANIZATION),
                    QuickMenuItem("Announcements", "${hqState.announcements.size} Dispatches", Icons.AutoMirrored.Filled.Announcement, DashboardQuickDestination.ANNOUNCEMENTS),
                    QuickMenuItem("Events", "${upcomingEvents.size} Upcoming", Icons.Filled.CalendarMonth, DashboardQuickDestination.EVENTS),
                    QuickMenuItem("Media Vault", "Add Photo/Video", Icons.Filled.Collections, DashboardQuickDestination.MEDIA),
                    QuickMenuItem("Digital Archive", "${hqState.archiveEntries.size} Records", Icons.Filled.HistoryEdu, DashboardQuickDestination.ARCHIVE),
                    QuickMenuItem("Clipper Room", "Shared Access", Icons.Filled.Movie, DashboardQuickDestination.CLIPPER_ROOM),
                    QuickMenuItem("Admin Panel", "Officer & Mod", Icons.Filled.AdminPanelSettings, DashboardQuickDestination.ADMIN_PANEL)
                )
                tiles.chunked(2).forEach { rowTiles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowTiles.forEach { item ->
                            QuickNavTileCard(
                                item = item,
                                onClick = { onQuickNavigate(item.destination) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Surface(
                    color = JdmcGoldContainer,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, JdmcLightYellow),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onQuickNavigate(DashboardQuickDestination.ROAD_AI) }
                        .testTag("quick_nav_road_ai")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = JdmcObsidianBlack,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, JdmcGoldBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "JDMC Road AI",
                                tint = JdmcLightYellow,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "JDMC ROAD AI • GEMINI CONCIERGE",
                                style = MaterialTheme.typography.titleSmall,
                                color = JdmcLightYellow,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Draft announcements, plan Los Santos convoys & analyze bike photos",
                                style = MaterialTheme.typography.bodySmall,
                                color = JdmcMetallicSilver
                            )
                        }
                    }
                }
            }
        }

        // 5. Latest Announcements Preview
        item {
            SectionHeaderRow(
                title = "LATEST ANNOUNCEMENTS",
                actionLabel = "See All",
                onActionClick = { onQuickNavigate(DashboardQuickDestination.ANNOUNCEMENTS) }
            )
        }
        if (latestAnnouncements.isEmpty()) {
            item {
                EmptyDashboardCard("No announcements published yet.")
            }
        } else {
            items(latestAnnouncements, key = { "dash_ann_${it.id}" }) { ann ->
                Surface(
                    color = JdmcCarbonSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (ann.isPinned) JdmcGoldBorder else JdmcSteelOutline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onQuickNavigate(DashboardQuickDestination.ANNOUNCEMENTS) }
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (ann.isPinned) {
                                    Icon(
                                        imageVector = Icons.Filled.PushPin,
                                        contentDescription = "Pinned",
                                        tint = JdmcLightYellow,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = ann.category.label.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JdmcLightYellow
                                )
                            }
                            Text(
                                text = ann.dateLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = JdmcGunmetal
                            )
                        }
                        Text(
                            text = ann.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = JdmcMetallicSilver
                        )
                        Text(
                            text = ann.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = JdmcBrushedSilver,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 6. Upcoming Events Preview
        item {
            SectionHeaderRow(
                title = "UPCOMING EVENTS",
                actionLabel = "All Events",
                onActionClick = { onQuickNavigate(DashboardQuickDestination.EVENTS) }
            )
        }
        if (upcomingEvents.isEmpty()) {
            item { EmptyDashboardCard("No upcoming events scheduled.") }
        } else {
            items(upcomingEvents.take(2), key = { "dash_evt_${it.id}" }) { event ->
                Surface(
                    color = JdmcCarbonSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, JdmcSteelOutline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onQuickNavigate(DashboardQuickDestination.EVENTS) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = event.dateTimeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = JdmcLightYellow
                            )
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = JdmcMetallicSilver
                            )
                            Text(
                                text = "${event.location} • ${event.attendees.size} attending",
                                style = MaterialTheme.typography.bodySmall,
                                color = JdmcGunmetal
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = JdmcLightYellow,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 7. Latest Club Activity
        item {
            Text(
                text = "LATEST CLUB ACTIVITY",
                style = MaterialTheme.typography.titleMedium,
                color = JdmcLightYellow
            )
        }
        items(hqState.recentActivities.take(5), key = { it.id }) { act ->
            val timeLabel = SimpleDateFormat("HH:mm • dd MMM", Locale.US).format(Date(act.timestampMs))
            Surface(
                color = JdmcCarbonElevated,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, JdmcSteelOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Timeline,
                        contentDescription = null,
                        tint = JdmcLightYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = act.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = JdmcMetallicSilver
                        )
                        Text(
                            text = "${act.categoryTag} • $timeLabel",
                            style = MaterialTheme.typography.labelSmall,
                            color = JdmcGunmetal
                        )
                    }
                }
            }
        }
    }
}

private data class QuickMenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val destination: DashboardQuickDestination
)

@Composable
private fun QuickNavTileCard(
    item: QuickMenuItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = JdmcCarbonSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, JdmcSteelOutline),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("quick_nav_${item.destination.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = JdmcGoldContainer,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, JdmcGoldBorder)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = JdmcLightYellow,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(18.dp)
                )
            }
            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = JdmcMetallicSilver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = JdmcGunmetal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SectionHeaderRow(
    title: String,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = JdmcLightYellow
        )
        TextButton(onClick = onActionClick) {
            Text(actionLabel, color = JdmcLightYellow)
        }
    }
}

@Composable
private fun EmptyDashboardCard(message: String) {
    Surface(
        color = JdmcCarbonSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, JdmcSteelOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = JdmcGunmetal,
            modifier = Modifier.padding(16.dp)
        )
    }
}
