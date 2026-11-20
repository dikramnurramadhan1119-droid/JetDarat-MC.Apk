package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.ArchiveEntry
import com.example.domain.model.ArchiveSection
import com.example.domain.model.ClubOrganizationRole
import com.example.domain.model.DefaultJdmcHqSeed
import com.example.domain.model.MemberProfile
import com.example.domain.model.MemberStatus
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

enum class ClubSubTab(val label: String) {
    MEMBERS("Members"),
    ORGANIZATION("Organization"),
    ARCHIVE("Digital Archive")
}

@Composable
fun ClubHubScreen(
    activeSubTab: ClubSubTab,
    onSelectSubTab: (ClubSubTab) -> Unit,
    members: List<MemberProfile>,
    organizationRoles: List<ClubOrganizationRole>,
    archiveEntries: List<ArchiveEntry>,
    isOfficerMode: Boolean,
    onPickAndImportImage: suspend (Uri) -> String?,
    onSaveMember: (
        existingId: String?,
        memberNumber: String,
        characterName: String,
        clubPosition: String,
        status: MemberStatus,
        biography: String,
        joinDate: String,
        customRankBadge: String,
        avatarUri: String,
        sortOrder: Int
    ) -> Unit,
    onDeleteMember: (MemberProfile) -> Unit,
    onSaveOrganizationRole: (ClubOrganizationRole) -> Unit,
    onSaveArchiveEntry: (
        existingId: String?,
        section: ArchiveSection,
        yearEra: String,
        title: String,
        narrative: String,
        imageUri: String,
        sortOrder: Int
    ) -> Unit,
    onDeleteArchiveEntry: (ArchiveEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMemberId by remember { mutableStateOf<String?>(null) }
    var editingMember by remember { mutableStateOf<MemberProfile?>(null) }
    var showAddMember by remember { mutableStateOf(false) }

    var editingRole by remember { mutableStateOf<ClubOrganizationRole?>(null) }

    var archiveFilter by remember { mutableStateOf<ArchiveSection?>(null) }
    var editingArchive by remember { mutableStateOf<ArchiveEntry?>(null) }
    var showAddArchive by remember { mutableStateOf(false) }

    val selectedMember = selectedMemberId?.let { id -> members.firstOrNull { it.id == id } }

    selectedMember?.let { member ->
        MemberDossierDialog(
            member = member,
            isOfficerMode = isOfficerMode,
            onPickAndImportImage = onPickAndImportImage,
            onUpdateAvatar = { newAvatarUri ->
                onSaveMember(
                    member.id,
                    member.memberNumber,
                    member.characterName,
                    member.clubPosition,
                    member.status,
                    member.biography,
                    member.joinDate,
                    member.customRankBadge,
                    newAvatarUri,
                    member.sortOrder
                )
            },
            onEdit = {
                selectedMemberId = null
                editingMember = member
            },
            onDelete = {
                onDeleteMember(member)
                selectedMemberId = null
            },
            onDismiss = { selectedMemberId = null }
        )
    }

    if (showAddMember || editingMember != null) {
        val target = editingMember
        MemberEditorDialog(
            initial = target,
            roles = organizationRoles,
            onPickAndImportImage = onPickAndImportImage,
            onSave = { number, name, pos, status, bio, join, badge, avatar ->
                onSaveMember(
                    target?.id,
                    number,
                    name,
                    pos,
                    status,
                    bio,
                    join,
                    badge,
                    avatar,
                    target?.sortOrder ?: (members.size + 1)
                )
                showAddMember = false
                editingMember = null
            },
            onDismiss = {
                showAddMember = false
                editingMember = null
            }
        )
    }

    editingRole?.let { role ->
        OrganizationRoleEditorDialog(
            role = role,
            onSave = { updated ->
                onSaveOrganizationRole(updated)
                editingRole = null
            },
            onDismiss = { editingRole = null }
        )
    }

    if (showAddArchive || editingArchive != null) {
        val target = editingArchive
        ArchiveEditorDialog(
            initial = target,
            onPickAndImportImage = onPickAndImportImage,
            onSave = { sec, era, title, narrative, img ->
                onSaveArchiveEntry(
                    target?.id,
                    sec,
                    era,
                    title,
                    narrative,
                    img,
                    target?.sortOrder ?: (archiveEntries.size + 1)
                )
                showAddArchive = false
                editingArchive = null
            },
            onDismiss = {
                showAddArchive = false
                editingArchive = null
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("club_hub_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClubSubTab.entries.forEach { tab ->
                    val icon = when (tab) {
                        ClubSubTab.MEMBERS -> Icons.Filled.Groups
                        ClubSubTab.ORGANIZATION -> Icons.Filled.AccountTree
                        ClubSubTab.ARCHIVE -> Icons.Filled.HistoryEdu
                    }
                    FilterChip(
                        selected = activeSubTab == tab,
                        onClick = { onSelectSubTab(tab) },
                        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text(tab.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        colors = jdmcFilterChipColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("club_subtab_${tab.name.lowercase()}")
                    )
                }
            }
        }

        when (activeSubTab) {
            ClubSubTab.MEMBERS -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = JdmcGunmetal) },
                            placeholder = { Text("Search name, position, ID...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        if (isOfficerMode) {
                            Button(
                                onClick = { showAddMember = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JdmcLightYellow,
                                    contentColor = JdmcObsidianBlack
                                ),
                                modifier = Modifier.testTag("add_member_button")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add Member", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                val filtered = members.filter { m ->
                    val q = searchQuery.trim()
                    q.isEmpty() || m.characterName.contains(q, ignoreCase = true) ||
                        m.clubPosition.contains(q, ignoreCase = true) ||
                        m.customRankBadge.contains(q, ignoreCase = true) ||
                        m.memberNumber.contains(q, ignoreCase = true)
                }

                items(filtered, key = { it.id }) { member ->
                    MemberProfileCard(
                        member = member,
                        isOfficerMode = isOfficerMode,
                        onClick = { selectedMemberId = member.id },
                        onEdit = { editingMember = member },
                        onDelete = { onDeleteMember(member) }
                    )
                }
            }

            ClubSubTab.ORGANIZATION -> {
                item {
                    Surface(
                        color = JdmcCarbonElevated,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, JdmcGoldBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "JDMC COMMAND & ORGANIZATION HIERARCHY",
                                style = MaterialTheme.typography.titleMedium,
                                color = JdmcLightYellow
                            )
                            Text(
                                text = "Official Jet Darat Motorcycle Club rank chain. ${if (isOfficerMode) "Officer Mode active — tap Edit on any position." else "Switch to Officer Admin Mode to edit positions."}",
                                style = MaterialTheme.typography.bodySmall,
                                color = JdmcBrushedSilver
                            )
                        }
                    }
                }
                items(organizationRoles.sortedBy { it.tierOrder }, key = { it.id }) { role ->
                    Surface(
                        color = JdmcCarbonSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (role.isOfficerTier) JdmcGoldBorder.copy(alpha = 0.7f) else JdmcSteelOutline),
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        color = JdmcGoldContainer,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, JdmcGoldBorder)
                                    ) {
                                        Text(
                                            text = "TIER ${role.tierOrder}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = JdmcLightYellow,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = role.roleTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = JdmcLightYellow
                                        )
                                        if (role.indonesianAlias.isNotBlank()) {
                                            Text(
                                                text = role.indonesianAlias,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = JdmcGunmetal
                                            )
                                        }
                                    }
                                }
                                if (isOfficerMode) {
                                    OutlinedButton(
                                        onClick = { editingRole = role },
                                        border = BorderStroke(1.dp, JdmcGoldBorder)
                                    ) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit Role", modifier = Modifier.size(14.dp), tint = JdmcLightYellow)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Edit", color = JdmcLightYellow)
                                    }
                                }
                            }
                            Text(
                                text = role.mandate,
                                style = MaterialTheme.typography.bodySmall,
                                color = JdmcBrushedSilver
                            )
                            Surface(
                                color = JdmcCarbonElevated,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, JdmcSteelOutline)
                            ) {
                                Text(
                                    text = "Assigned: ${role.assignedHolders.ifBlank { "Vacant / Open" }}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = JdmcMetallicSilver,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            ClubSubTab.ARCHIVE -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "JDMC DIGITAL ARCHIVE",
                                style = MaterialTheme.typography.titleMedium,
                                color = JdmcLightYellow
                            )
                            Text(
                                text = "Founding history, milestones, legacy & memories",
                                style = MaterialTheme.typography.bodySmall,
                                color = JdmcGunmetal
                            )
                        }
                        if (isOfficerMode) {
                            Button(
                                onClick = { showAddArchive = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JdmcLightYellow,
                                    contentColor = JdmcObsidianBlack
                                )
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add")
                            }
                        }
                    }
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = archiveFilter == null,
                                onClick = { archiveFilter = null },
                                label = { Text("All Heritage") },
                                colors = jdmcFilterChipColors()
                            )
                        }
                        items(ArchiveSection.entries) { section ->
                            FilterChip(
                                selected = archiveFilter == section,
                                onClick = { archiveFilter = if (archiveFilter == section) null else section },
                                label = { Text(section.label) },
                                colors = jdmcFilterChipColors()
                            )
                        }
                    }
                }

                val filteredArchive = archiveEntries.filter { archiveFilter == null || it.section == archiveFilter }
                items(filteredArchive, key = { it.id }) { entry ->
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
                            if (entry.imageUri.isNotBlank()) {
                                HqMediaImage(
                                    uriOrResource = entry.imageUri,
                                    contentDescription = entry.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = JdmcGoldContainer,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, JdmcGoldBorder)
                                ) {
                                    Text(
                                        text = entry.section.label.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = JdmcLightYellow,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = entry.yearEra,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JdmcBrushedSilver
                                )
                            }
                            Text(
                                text = entry.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = JdmcLightYellow
                            )
                            Text(
                                text = entry.narrative,
                                style = MaterialTheme.typography.bodyMedium,
                                color = JdmcMetallicSilver
                            )
                            if (isOfficerMode) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { editingArchive = entry },
                                        border = BorderStroke(1.dp, JdmcSteelOutline),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Edit", color = JdmcLightYellow)
                                    }
                                    OutlinedButton(
                                        onClick = { onDeleteArchiveEntry(entry) },
                                        border = BorderStroke(1.dp, JdmcGoldBorder),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Delete", color = JdmcAmberAlert)
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
private fun MemberProfileCard(
    member: MemberProfile,
    isOfficerMode: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = JdmcCarbonSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (member.status == MemberStatus.OFFICER) JdmcGoldBorder.copy(alpha = 0.7f) else JdmcSteelOutline),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("member_card_${member.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HqMediaImage(
                    uriOrResource = member.avatarUri.ifBlank { DefaultJdmcHqSeed.DRAWABLE_CLUB_LOGO },
                    contentDescription = member.characterName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = member.characterName,
                            style = MaterialTheme.typography.titleMedium,
                            color = JdmcLightYellow,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = member.memberNumber,
                            style = MaterialTheme.typography.labelSmall,
                            color = JdmcGunmetal
                        )
                    }
                    Text(
                        text = member.clubPosition,
                        style = MaterialTheme.typography.bodyMedium,
                        color = JdmcMetallicSilver
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Surface(
                            color = JdmcGoldContainer,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, JdmcGoldBorder)
                        ) {
                            Text(
                                text = member.customRankBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = JdmcLightYellow,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            color = JdmcCarbonElevated,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, JdmcSteelOutline)
                        ) {
                            Text(
                                text = member.status.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = JdmcBrushedSilver,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Column {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_member_card_${member.id}")
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit profile", tint = JdmcLightYellow)
                    }
                    if (isOfficerMode) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove member", tint = JdmcAmberAlert)
                        }
                    }
                }
            }
            Text(
                text = member.biography,
                style = MaterialTheme.typography.bodySmall,
                color = JdmcBrushedSilver,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Joined ${member.joinDate} • Tap to view or edit profile",
                style = MaterialTheme.typography.labelSmall,
                color = JdmcGunmetal
            )
        }
    }
}
