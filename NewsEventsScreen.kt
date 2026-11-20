package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.model.AnnouncementCategory
import com.example.domain.model.ClubAnnouncement
import com.example.domain.model.ClubEvent
import com.example.domain.model.DefaultJdmcHqSeed
import com.example.domain.model.EventStatus
import com.example.ui.components.HqMediaImage
import com.example.ui.theme.JdmcAmberAlert
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
import kotlinx.coroutines.launch

enum class NewsSubTab(val label: String) {
    ANNOUNCEMENTS("Announcements"),
    EVENTS("Events & Rides")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewsEventsScreen(
    activeSubTab: NewsSubTab,
    onSelectSubTab: (NewsSubTab) -> Unit,
    announcements: List<ClubAnnouncement>,
    events: List<ClubEvent>,
    activeMemberAlias: String,
    isOfficerMode: Boolean,
    onPickAndImportImage: suspend (Uri) -> String?,
    onSaveAnnouncement: (
        existingId: String?,
        title: String,
        content: String,
        category: AnnouncementCategory,
        isPinned: Boolean,
        authorName: String
    ) -> Unit,
    onTogglePinAnnouncement: (ClubAnnouncement) -> Unit,
    onDeleteAnnouncement: (ClubAnnouncement) -> Unit,
    onSaveEvent: (
        existingId: String?,
        title: String,
        description: String,
        dateTimeLabel: String,
        location: String,
        posterUri: String,
        status: EventStatus,
        attendees: List<String>
    ) -> Unit,
    onToggleEventAttendance: (ClubEvent) -> Unit,
    onToggleEventStatus: (ClubEvent) -> Unit,
    onDeleteEvent: (ClubEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryFilter by remember { mutableStateOf<AnnouncementCategory?>(null) }
    var editingAnnouncement by remember { mutableStateOf<ClubAnnouncement?>(null) }
    var showCreateAnnouncement by remember { mutableStateOf(false) }

    var eventStatusFilter by remember { mutableStateOf<EventStatus?>(null) }
    var editingEvent by remember { mutableStateOf<ClubEvent?>(null) }
    var showCreateEvent by remember { mutableStateOf(false) }

    if (showCreateAnnouncement || editingAnnouncement != null) {
        val target = editingAnnouncement
        AnnouncementEditorDialog(
            initial = target,
            defaultAuthor = activeMemberAlias,
            onSave = { title, content, cat, pinned, author ->
                onSaveAnnouncement(target?.id, title, content, cat, pinned, author)
                showCreateAnnouncement = false
                editingAnnouncement = null
            },
            onDismiss = {
                showCreateAnnouncement = false
                editingAnnouncement = null
            }
        )
    }

    if (showCreateEvent || editingEvent != null) {
        val target = editingEvent
        EventEditorDialog(
            initial = target,
            onPickAndImportImage = onPickAndImportImage,
            onSave = { title, desc, dateTime, loc, poster, status, attendees ->
                onSaveEvent(target?.id, title, desc, dateTime, loc, poster, status, attendees)
                showCreateEvent = false
                editingEvent = null
            },
            onDismiss = {
                showCreateEvent = false
                editingEvent = null
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("news_events_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NewsSubTab.entries.forEach { tab ->
                    FilterChip(
                        selected = activeSubTab == tab,
                        onClick = { onSelectSubTab(tab) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (tab == NewsSubTab.ANNOUNCEMENTS) {
                                    Icons.AutoMirrored.Filled.Announcement
                                } else {
                                    Icons.Filled.CalendarMonth
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(tab.label) },
                        colors = jdmcFilterChipColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("news_subtab_${tab.name.lowercase()}")
                    )
                }
            }
        }

        when (activeSubTab) {
            NewsSubTab.ANNOUNCEMENTS -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "JDMC ANNOUNCEMENTS",
                            style = MaterialTheme.typography.titleMedium,
                            color = JdmcLightYellow
                        )
                        if (isOfficerMode) {
                            Button(
                                onClick = { showCreateAnnouncement = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JdmcLightYellow,
                                    contentColor = JdmcObsidianBlack
                                )
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Publish")
                            }
                        }
                    }
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = categoryFilter == null,
                                onClick = { categoryFilter = null },
                                label = { Text("All Categories") },
                                colors = jdmcFilterChipColors()
                            )
                        }
                        items(AnnouncementCategory.entries) { cat ->
                            FilterChip(
                                selected = categoryFilter == cat,
                                onClick = { categoryFilter = if (categoryFilter == cat) null else cat },
                                label = { Text(cat.label) },
                                colors = jdmcFilterChipColors()
                            )
                        }
                    }
                }

                val filtered = announcements.filter { categoryFilter == null || it.category == categoryFilter }
                if (filtered.isEmpty()) {
                    item {
                        NewsEmptyCard("No announcements in this category yet.")
                    }
                } else {
                    items(filtered, key = { it.id }) { ann ->
                        Surface(
                            color = JdmcCarbonSurface,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (ann.isPinned) JdmcGoldBorder else JdmcSteelOutline),
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
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (ann.isPinned) {
                                            Surface(
                                                color = JdmcLightYellow,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "PINNED",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = JdmcObsidianBlack,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Surface(
                                            color = JdmcGoldContainer,
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, JdmcGoldBorder)
                                        ) {
                                            Text(
                                                text = ann.category.label.uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = JdmcLightYellow,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = ann.dateLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = JdmcGunmetal
                                    )
                                }
                                Text(
                                    text = ann.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = JdmcLightYellow
                                )
                                Text(
                                    text = ann.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = JdmcMetallicSilver
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Issued by ${ann.authorName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = JdmcGunmetal
                                    )
                                    if (isOfficerMode) {
                                        Row {
                                            IconButton(onClick = { onTogglePinAnnouncement(ann) }) {
                                                Icon(
                                                    imageVector = if (ann.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                                    contentDescription = "Pin toggle",
                                                    tint = JdmcLightYellow
                                                )
                                            }
                                            IconButton(onClick = { editingAnnouncement = ann }) {
                                                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = JdmcMetallicSilver)
                                            }
                                            IconButton(onClick = { onDeleteAnnouncement(ann) }) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = JdmcAmberAlert)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            NewsSubTab.EVENTS -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EVENTS & RIDES",
                            style = MaterialTheme.typography.titleMedium,
                            color = JdmcLightYellow
                        )
                        if (isOfficerMode) {
                            Button(
                                onClick = { showCreateEvent = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JdmcLightYellow,
                                    contentColor = JdmcObsidianBlack
                                )
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("New Event")
                            }
                        }
                    }
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = eventStatusFilter == null,
                                onClick = { eventStatusFilter = null },
                                label = { Text("All Events") },
                                colors = jdmcFilterChipColors()
                            )
                        }
                        items(EventStatus.entries) { st ->
                            FilterChip(
                                selected = eventStatusFilter == st,
                                onClick = { eventStatusFilter = if (eventStatusFilter == st) null else st },
                                label = { Text(st.label) },
                                colors = jdmcFilterChipColors()
                            )
                        }
                    }
                }

                val filteredEvents = events.filter { eventStatusFilter == null || it.status == eventStatusFilter }
                if (filteredEvents.isEmpty()) {
                    item { NewsEmptyCard("No events match this status filter.") }
                } else {
                    items(filteredEvents, key = { it.id }) { event ->
                        val isAttending = event.attendees.any { it.equals(activeMemberAlias, ignoreCase = true) }
                        Surface(
                            color = JdmcCarbonSurface,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                1.dp,
                                if (event.status == EventStatus.UPCOMING) JdmcGoldBorder.copy(alpha = 0.7f) else JdmcSteelOutline
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                HqMediaImage(
                                    uriOrResource = event.posterUri.ifBlank { DefaultJdmcHqSeed.DRAWABLE_CLIP_BANNER },
                                    contentDescription = event.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (event.status == EventStatus.UPCOMING) JdmcGoldContainer else JdmcCarbonElevated,
                                        shape = RoundedCornerShape(999.dp),
                                        border = BorderStroke(
                                            1.dp,
                                            if (event.status == EventStatus.UPCOMING) JdmcLightYellow else JdmcSteelOutline
                                        )
                                    ) {
                                        Text(
                                            text = event.status.label.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (event.status == EventStatus.UPCOMING) JdmcLightYellow else JdmcBrushedSilver,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Text(
                                        text = event.dateTimeLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = JdmcLightYellow
                                    )
                                }
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = JdmcLightYellow
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = JdmcBrushedSilver, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = event.location,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = JdmcBrushedSilver
                                    )
                                }
                                Text(
                                    text = event.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = JdmcMetallicSilver
                                )
                                Surface(
                                    color = JdmcCarbonElevated,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, JdmcSteelOutline),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "ATTENDANCE ROSTER (${event.attendees.size})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = JdmcLightYellow
                                        )
                                        if (event.attendees.isEmpty()) {
                                            Text("No riders checked in yet.", style = MaterialTheme.typography.bodySmall, color = JdmcGunmetal)
                                        } else {
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                event.attendees.forEach { name ->
                                                    Surface(
                                                        color = JdmcCarbonSurface,
                                                        shape = RoundedCornerShape(6.dp),
                                                        border = BorderStroke(1.dp, JdmcSteelOutline)
                                                    ) {
                                                        Text(
                                                            text = name,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = JdmcMetallicSilver,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onToggleEventAttendance(event) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isAttending) JdmcCarbonElevated else JdmcLightYellow,
                                            contentColor = if (isAttending) JdmcLightYellow else JdmcObsidianBlack
                                        ),
                                        border = if (isAttending) BorderStroke(1.dp, JdmcLightYellow) else null,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(if (isAttending) "Checked In ($activeMemberAlias)" else "RSVP as $activeMemberAlias")
                                    }
                                }
                                if (isOfficerMode) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onToggleEventStatus(event) },
                                            border = BorderStroke(1.dp, JdmcSteelOutline),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = if (event.status == EventStatus.UPCOMING) "Mark Completed" else "Reopen",
                                                color = JdmcBrushedSilver
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { editingEvent = event },
                                            border = BorderStroke(1.dp, JdmcGoldBorder)
                                        ) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = JdmcLightYellow)
                                        }
                                        OutlinedButton(
                                            onClick = { onDeleteEvent(event) },
                                            border = BorderStroke(1.dp, JdmcGoldBorder)
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = JdmcAmberAlert)
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

@Composable
private fun AnnouncementEditorDialog(
    initial: ClubAnnouncement?,
    defaultAuthor: String,
    onSave: (String, String, AnnouncementCategory, Boolean, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var content by remember { mutableStateOf(initial?.content.orEmpty()) }
    var category by remember { mutableStateOf(initial?.category ?: AnnouncementCategory.COMMAND) }
    var isPinned by remember { mutableStateOf(initial?.isPinned ?: false) }
    var author by remember { mutableStateOf(initial?.authorName ?: defaultAuthor) }
    var catMenu by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JdmcCarbonSurface,
        title = { Text(if (initial == null) "Publish Announcement" else "Edit Announcement", color = JdmcLightYellow) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errorMsg = null },
                    label = { Text("Headline *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { catMenu = true },
                        border = BorderStroke(1.dp, JdmcSteelOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Category: ${category.label}", color = JdmcMetallicSilver)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = JdmcLightYellow)
                        }
                    }
                    DropdownMenu(expanded = catMenu, onDismissRequest = { catMenu = false }) {
                        AnnouncementCategory.entries.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat.label) }, onClick = { category = cat; catMenu = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author / Rank") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it; errorMsg = null },
                    label = { Text("Dispatch Content *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it },
                        colors = CheckboxDefaults.colors(checkedColor = JdmcLightYellow, checkmarkColor = JdmcObsidianBlack)
                    )
                    Text("Pin to top of announcements & dashboard", color = JdmcMetallicSilver)
                }
                if (errorMsg != null) {
                    Text(errorMsg!!, color = JdmcAmberAlert, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || content.isBlank()) {
                        errorMsg = "Headline and dispatch content are required."
                    } else {
                        onSave(title, content, category, isPinned, author)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = JdmcLightYellow, contentColor = JdmcObsidianBlack)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = JdmcGunmetal) } }
    )
}

@Composable
private fun EventEditorDialog(
    initial: ClubEvent?,
    onPickAndImportImage: suspend (Uri) -> String?,
    onSave: (String, String, String, String, String, EventStatus, List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var description by remember { mutableStateOf(initial?.description.orEmpty()) }
    var dateTime by remember { mutableStateOf(initial?.dateTimeLabel ?: "Sat • 22:00 WIB") }
    var location by remember { mutableStateOf(initial?.location ?: "Los Santos Clubhouse") }
    var posterUri by remember { mutableStateOf(initial?.posterUri ?: DefaultJdmcHqSeed.DRAWABLE_CLUB_LOGO) }
    var status by remember { mutableStateOf(initial?.status ?: EventStatus.UPCOMING) }
    var attendeesRaw by remember { mutableStateOf(initial?.attendees?.joinToString(", ").orEmpty()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val saved = onPickAndImportImage(uri)
                if (saved != null) posterUri = saved
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JdmcCarbonSurface,
        title = { Text(if (initial == null) "Create Club Event" else "Edit Club Event", color = JdmcLightYellow) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errorMsg = null },
                    label = { Text("Event Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dateTime,
                    onValueChange = { dateTime = it },
                    label = { Text("Date & Time *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it; errorMsg = null },
                    label = { Text("Event Description *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = attendeesRaw,
                    onValueChange = { attendeesRaw = it },
                    label = { Text("Attendance List (comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
                HqMediaImage(
                    uriOrResource = posterUri,
                    contentDescription = "Event poster",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                OutlinedButton(
                    onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    border = BorderStroke(1.dp, JdmcGoldBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp), tint = JdmcLightYellow)
                    Spacer(Modifier.width(6.dp))
                    Text("Pick Event Poster", color = JdmcLightYellow)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EventStatus.entries.forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st.label) },
                            colors = jdmcFilterChipColors()
                        )
                    }
                }
                if (errorMsg != null) {
                    Text(errorMsg!!, color = JdmcAmberAlert, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank()) {
                        errorMsg = "Title and description are required."
                    } else {
                        val attendees = attendeesRaw.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        onSave(title, description, dateTime, location, posterUri, status, attendees)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = JdmcLightYellow, contentColor = JdmcObsidianBlack)
            ) { Text("Save Event") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = JdmcGunmetal) } }
    )
}

@Composable
private fun NewsEmptyCard(text: String) {
    Surface(
        color = JdmcCarbonSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, JdmcSteelOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = JdmcGunmetal,
            modifier = Modifier.padding(18.dp)
        )
    }
}
