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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
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
import kotlinx.coroutines.launch

@Composable
fun MemberDossierDialog(
    member: MemberProfile,
    isOfficerMode: Boolean,
    onPickAndImportImage: suspend (Uri) -> String?,
    onUpdateAvatar: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isImportingPhoto by remember { mutableStateOf(false) }

    val quickPhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                isImportingPhoto = true
                val saved = onPickAndImportImage(uri)
                isImportingPhoto = false
                if (saved != null) {
                    onUpdateAvatar(saved)
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JdmcCarbonSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = JdmcCarbonElevated,
                    shape = CircleShape,
                    border = BorderStroke(2.dp, JdmcLightYellow)
                ) {
                    HqMediaImage(
                        uriOrResource = member.avatarUri.ifBlank { DefaultJdmcHqSeed.DRAWABLE_CLUB_LOGO },
                        contentDescription = member.characterName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = member.characterName,
                        style = MaterialTheme.typography.titleLarge,
                        color = JdmcLightYellow
                    )
                    Text(
                        text = "${member.memberNumber} • ${member.clubPosition}",
                        style = MaterialTheme.typography.labelMedium,
                        color = JdmcBrushedSilver
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        quickPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    enabled = !isImportingPhoto,
                    border = BorderStroke(1.dp, JdmcGoldBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dossier_change_photo_button")
                ) {
                    if (isImportingPhoto) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = JdmcLightYellow
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Saving Photo...", color = JdmcLightYellow)
                    } else {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = JdmcLightYellow
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Change Photo from Gallery", color = JdmcLightYellow)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = JdmcGoldContainer,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, JdmcGoldBorder)
                    ) {
                        Text(
                            text = member.customRankBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = JdmcLightYellow,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                            color = JdmcMetallicSilver,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(
                    text = "Joined: ${member.joinDate}",
                    style = MaterialTheme.typography.labelMedium,
                    color = JdmcGunmetal
                )
                Text(
                    text = member.biography,
                    style = MaterialTheme.typography.bodyMedium,
                    color = JdmcMetallicSilver
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JdmcLightYellow,
                    contentColor = JdmcObsidianBlack
                ),
                modifier = Modifier.testTag("edit_member_profile_button")
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Edit Profile")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (isOfficerMode) {
                    TextButton(onClick = onDelete) {
                        Text("Remove", color = JdmcAmberAlert)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Close", color = JdmcGunmetal)
                }
            }
        }
    )
}

@Composable
fun MemberEditorDialog(
    initial: MemberProfile?,
    roles: List<ClubOrganizationRole>,
    onPickAndImportImage: suspend (Uri) -> String?,
    onSave: (
        memberNumber: String,
        characterName: String,
        clubPosition: String,
        status: MemberStatus,
        biography: String,
        joinDate: String,
        customRankBadge: String,
        avatarUri: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var memberNumber by remember { mutableStateOf(initial?.memberNumber ?: "JDMC-007") }
    var characterName by remember { mutableStateOf(initial?.characterName.orEmpty()) }
    var clubPosition by remember { mutableStateOf(initial?.clubPosition ?: (roles.firstOrNull()?.roleTitle ?: "Full Members")) }
    var status by remember { mutableStateOf(initial?.status ?: MemberStatus.ACTIVE) }
    var biography by remember { mutableStateOf(initial?.biography.orEmpty()) }
    var joinDate by remember { mutableStateOf(initial?.joinDate ?: "2026-11-19") }
    var customRankBadge by remember { mutableStateOf(initial?.customRankBadge.orEmpty()) }
    var avatarUri by remember { mutableStateOf(initial?.avatarUri ?: DefaultJdmcHqSeed.DRAWABLE_CLUB_LOGO) }
    var statusExpanded by remember { mutableStateOf(false) }
    var roleExpanded by remember { mutableStateOf(false) }
    var isImportingPhoto by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                isImportingPhoto = true
                val saved = onPickAndImportImage(uri)
                isImportingPhoto = false
                if (saved != null) {
                    avatarUri = saved
                } else {
                    errorMsg = "Could not load selected photo. Please try another image."
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JdmcCarbonSurface,
        title = {
            Text(
                text = if (initial == null) "Add Member Profile" else "Edit Profile — ${initial.characterName}",
                color = JdmcLightYellow
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = JdmcCarbonElevated,
                        shape = CircleShape,
                        border = BorderStroke(2.dp, JdmcGoldBorder)
                    ) {
                        HqMediaImage(
                            uriOrResource = avatarUri,
                            contentDescription = "Member Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedButton(
                            onClick = {
                                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            enabled = !isImportingPhoto,
                            border = BorderStroke(1.dp, JdmcGoldBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pick_member_photo_button")
                        ) {
                            if (isImportingPhoto) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = JdmcLightYellow
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Importing...", color = JdmcLightYellow)
                            } else {
                                Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp), tint = JdmcLightYellow)
                                Spacer(Modifier.width(6.dp))
                                Text("Add / Change Photo", color = JdmcLightYellow)
                            }
                        }
                        if (avatarUri != DefaultJdmcHqSeed.DRAWABLE_CLUB_LOGO) {
                            TextButton(
                                onClick = { avatarUri = DefaultJdmcHqSeed.DRAWABLE_CLUB_LOGO },
                                modifier = Modifier.align(Alignment.Start)
                            ) {
                                Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp), tint = JdmcGunmetal)
                                Spacer(Modifier.width(4.dp))
                                Text("Reset to JDMC Emblem", color = JdmcGunmetal, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = characterName,
                    onValueChange = { characterName = it; errorMsg = null },
                    label = { Text("Character Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_name_input")
                )
                OutlinedTextField(
                    value = clubPosition,
                    onValueChange = { clubPosition = it; errorMsg = null },
                    label = { Text("Jabatan / Club Position *") },
                    placeholder = { Text("e.g. President, Road Captain, Ketua Umum") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_position_input")
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { roleExpanded = true },
                        border = BorderStroke(1.dp, JdmcSteelOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Quick-Select Jabatan ($clubPosition)", color = JdmcMetallicSilver)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = JdmcLightYellow)
                        }
                    }
                    DropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text("${role.roleTitle} (${role.indonesianAlias})") },
                                onClick = {
                                    clubPosition = role.roleTitle
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = memberNumber,
                    onValueChange = { memberNumber = it },
                    label = { Text("Member ID Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { statusExpanded = true },
                        border = BorderStroke(1.dp, JdmcSteelOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Status: ${status.label}", color = JdmcMetallicSilver)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = JdmcLightYellow)
                        }
                    }
                    DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        MemberStatus.entries.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.label) },
                                onClick = { status = s; statusExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = customRankBadge,
                    onValueChange = { customRankBadge = it },
                    label = { Text("Custom Rank Badge") },
                    placeholder = { Text("e.g. DUO TACHIBANA") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = joinDate,
                    onValueChange = { joinDate = it },
                    label = { Text("Join Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = biography,
                    onValueChange = { biography = it },
                    label = { Text("Personal Biography") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Text(errorMsg!!, color = JdmcAmberAlert, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        characterName.isBlank() -> errorMsg = "Character name is required."
                        clubPosition.isBlank() -> errorMsg = "Club position / Jabatan is required."
                        else -> onSave(memberNumber, characterName, clubPosition, status, biography, joinDate, customRankBadge, avatarUri)
                    }
                },
                enabled = !isImportingPhoto,
                colors = ButtonDefaults.buttonColors(containerColor = JdmcLightYellow, contentColor = JdmcObsidianBlack),
                modifier = Modifier.testTag("save_member_button")
            ) {
                Text("Save Profile")
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
fun OrganizationRoleEditorDialog(
    role: ClubOrganizationRole,
    onSave: (ClubOrganizationRole) -> Unit,
    onDismiss: () -> Unit
) {
    var roleTitle by remember { mutableStateOf(role.roleTitle) }
    var indonesianAlias by remember { mutableStateOf(role.indonesianAlias) }
    var mandate by remember { mutableStateOf(role.mandate) }
    var assignedHolders by remember { mutableStateOf(role.assignedHolders) }
    var isOfficer by remember { mutableStateOf(role.isOfficerTier) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JdmcCarbonSurface,
        title = { Text("Edit Organization Role", color = JdmcLightYellow) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = roleTitle,
                    onValueChange = { roleTitle = it },
                    label = { Text("Position Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = indonesianAlias,
                    onValueChange = { indonesianAlias = it },
                    label = { Text("Indonesian Jabatan Alias") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = assignedHolders,
                    onValueChange = { assignedHolders = it },
                    label = { Text("Assigned Member(s)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mandate,
                    onValueChange = { mandate = it },
                    label = { Text("Mandate & Responsibilities") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isOfficer,
                        onCheckedChange = { isOfficer = it },
                        colors = CheckboxDefaults.colors(checkedColor = JdmcLightYellow, checkmarkColor = JdmcObsidianBlack)
                    )
                    Text("Command / Officer Tier", color = JdmcMetallicSilver)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        role.copy(
                            roleTitle = roleTitle.trim().ifBlank { role.roleTitle },
                            indonesianAlias = indonesianAlias.trim(),
                            assignedHolders = assignedHolders.trim(),
                            mandate = mandate.trim(),
                            isOfficerTier = isOfficer
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = JdmcLightYellow, contentColor = JdmcObsidianBlack)
            ) {
                Text("Update Role")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = JdmcGunmetal) }
        }
    )
}

@Composable
fun ArchiveEditorDialog(
    initial: ArchiveEntry?,
    onPickAndImportImage: suspend (Uri) -> String?,
    onSave: (ArchiveSection, String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var section by remember { mutableStateOf(initial?.section ?: ArchiveSection.MILESTONE) }
    var yearEra by remember { mutableStateOf(initial?.yearEra ?: "2026 • LEGACY") }
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var narrative by remember { mutableStateOf(initial?.narrative.orEmpty()) }
    var imageUri by remember { mutableStateOf(initial?.imageUri ?: DefaultJdmcHqSeed.DRAWABLE_CLUB_LOGO) }
    var sectionMenu by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val saved = onPickAndImportImage(uri)
                if (saved != null) imageUri = saved
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JdmcCarbonSurface,
        title = { Text(if (initial == null) "Add Archive Record" else "Edit Archive Record", color = JdmcLightYellow) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { sectionMenu = true },
                        border = BorderStroke(1.dp, JdmcSteelOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Section: ${section.label}", color = JdmcMetallicSilver)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = JdmcLightYellow)
                        }
                    }
                    DropdownMenu(expanded = sectionMenu, onDismissRequest = { sectionMenu = false }) {
                        ArchiveSection.entries.forEach { sec ->
                            DropdownMenuItem(
                                text = { Text(sec.label) },
                                onClick = { section = sec; sectionMenu = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = yearEra,
                    onValueChange = { yearEra = it },
                    label = { Text("Era / Milestone Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errorMsg = null },
                    label = { Text("Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = narrative,
                    onValueChange = { narrative = it; errorMsg = null },
                    label = { Text("History / Memory Story *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                HqMediaImage(
                    uriOrResource = imageUri,
                    contentDescription = "Archive preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                OutlinedButton(
                    onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    border = BorderStroke(1.dp, JdmcGoldBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp), tint = JdmcLightYellow)
                    Spacer(Modifier.width(6.dp))
                    Text("Attach Historical Photo", color = JdmcLightYellow)
                }
                if (errorMsg != null) {
                    Text(errorMsg!!, color = JdmcAmberAlert, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || narrative.isBlank()) {
                        errorMsg = "Title and story narrative are required."
                    } else {
                        onSave(section, yearEra, title, narrative, imageUri)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = JdmcLightYellow, contentColor = JdmcObsidianBlack)
            ) {
                Text("Save Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = JdmcGunmetal) }
        }
    )
}
