package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalMediaType
import com.example.domain.model.MediaAlbum
import com.example.domain.model.MediaGalleryItem
import com.example.ui.components.HqMediaImage
import com.example.ui.components.MediaGalleryViewerDialog
import com.example.ui.components.VideoBadgeOverlay
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

@Composable
fun MediaGalleryScreen(
    mediaItems: List<MediaGalleryItem>,
    isImporting: Boolean,
    activeMemberAlias: String,
    isOfficerMode: Boolean,
    onSavePickedMedia: (uri: Uri, title: String, caption: String, album: MediaAlbum, onDone: () -> Unit) -> Unit,
    onDeleteMediaItem: (MediaGalleryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAlbum by remember { mutableStateOf<MediaAlbum?>(null) }
    var selectedType by remember { mutableStateOf<LocalMediaType?>(null) }
    var pendingPickedUri by remember { mutableStateOf<Uri?>(null) }
    var previewItem by remember { mutableStateOf<MediaGalleryItem?>(null) }

    val visualPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingPickedUri = uri
        }
    }

    previewItem?.let { item ->
        MediaGalleryViewerDialog(
            item = item,
            canDelete = isOfficerMode || item.id.startsWith("media-"),
            onDelete = {
                onDeleteMediaItem(item)
                previewItem = null
            },
            onDismiss = { previewItem = null }
        )
    }

    pendingPickedUri?.let { uri ->
        SavePickedMediaDialog(
            pickedUri = uri,
            defaultAlbum = selectedAlbum ?: MediaAlbum.CLUB_ACTIVITIES,
            addedBy = activeMemberAlias,
            isSaving = isImporting,
            onSave = { title, caption, album ->
                onSavePickedMedia(uri, title, caption, album) {
                    pendingPickedUri = null
                }
            },
            onDismiss = { if (!isImporting) pendingPickedUri = null }
        )
    }

    val filtered = mediaItems.filter { item ->
        (selectedAlbum == null || item.album == selectedAlbum) &&
            (selectedType == null || item.mediaType == selectedType)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("media_gallery_screen"),
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Collections, contentDescription = null, tint = JdmcLightYellow)
                        Column {
                            Text(
                                text = "JDMC MEDIA VAULT",
                                style = MaterialTheme.typography.titleLarge,
                                color = JdmcLightYellow
                            )
                            Text(
                                text = "Add photos or videos directly from your phone gallery and save to club albums.",
                                style = MaterialTheme.typography.bodySmall,
                                color = JdmcBrushedSilver
                            )
                        }
                    }

                    Button(
                        onClick = {
                            visualPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        enabled = !isImporting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JdmcLightYellow,
                            contentColor = JdmcObsidianBlack
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_media_gallery_button")
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = JdmcObsidianBlack)
                            Spacer(Modifier.width(8.dp))
                            Text("Saving File to Vault...")
                        } else {
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Add Photo / Video from Gallery")
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedAlbum == null,
                            onClick = { selectedAlbum = null },
                            label = { Text("All Albums (${mediaItems.size})") },
                            colors = jdmcFilterChipColors()
                        )
                    }
                    items(MediaAlbum.entries) { album ->
                        FilterChip(
                            selected = selectedAlbum == album,
                            onClick = { selectedAlbum = if (selectedAlbum == album) null else album },
                            label = { Text(album.label) },
                            colors = jdmcFilterChipColors()
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == null,
                        onClick = { selectedType = null },
                        label = { Text("Photos & Videos") },
                        colors = jdmcFilterChipColors()
                    )
                    FilterChip(
                        selected = selectedType == LocalMediaType.PHOTO,
                        onClick = { selectedType = if (selectedType == LocalMediaType.PHOTO) null else LocalMediaType.PHOTO },
                        leadingIcon = { Icon(Icons.Filled.Photo, contentDescription = null, modifier = Modifier.size(15.dp)) },
                        label = { Text("Photos") },
                        colors = jdmcFilterChipColors()
                    )
                    FilterChip(
                        selected = selectedType == LocalMediaType.VIDEO,
                        onClick = { selectedType = if (selectedType == LocalMediaType.VIDEO) null else LocalMediaType.VIDEO },
                        leadingIcon = { Icon(Icons.Filled.Movie, contentDescription = null, modifier = Modifier.size(15.dp)) },
                        label = { Text("Videos") },
                        colors = jdmcFilterChipColors()
                    )
                }
            }
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
                        text = "No saved photos or videos in this album yet. Tap \"Add Photo / Video from Gallery\" above to save your first file.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JdmcGunmetal,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(filtered, key = { it.id }) { item ->
                MediaVaultCard(
                    item = item,
                    canDelete = isOfficerMode || item.mediaUri.startsWith("file://"),
                    onOpen = { previewItem = item },
                    onDelete = { onDeleteMediaItem(item) }
                )
            }
        }
    }
}

@Composable
private fun MediaVaultCard(
    item: MediaGalleryItem,
    canDelete: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = JdmcCarbonSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, JdmcSteelOutline),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen)
            .testTag("media_card_${item.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                HqMediaImage(
                    uriOrResource = item.thumbnailUri.ifBlank { item.mediaUri },
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (item.mediaType == LocalMediaType.VIDEO) {
                    VideoBadgeOverlay(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    )
                }
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
                        text = item.album.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = JdmcLightYellow,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = "${item.mediaType.label} • ${item.addedBy}",
                    style = MaterialTheme.typography.labelSmall,
                    color = JdmcGunmetal
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = JdmcLightYellow,
                        fontWeight = FontWeight.Bold
                    )
                    if (item.caption.isNotBlank()) {
                        Text(
                            text = item.caption,
                            style = MaterialTheme.typography.bodySmall,
                            color = JdmcMetallicSilver
                        )
                    }
                }
                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete media",
                            tint = JdmcAmberAlert
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavePickedMediaDialog(
    pickedUri: Uri,
    defaultAlbum: MediaAlbum,
    addedBy: String,
    isSaving: Boolean,
    onSave: (title: String, caption: String, album: MediaAlbum) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var album by remember { mutableStateOf(defaultAlbum) }
    var albumExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JdmcCarbonSurface,
        title = { Text("Save to JDMC Media Vault", color = JdmcLightYellow) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HqMediaImage(
                    uriOrResource = pickedUri.toString(),
                    contentDescription = "Picked file preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(10.dp))
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Media Title") },
                    placeholder = { Text("e.g. RX-King Convoy Night Shot") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_media_title_input")
                )
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Description / Caption") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { albumExpanded = true },
                        border = BorderStroke(1.dp, JdmcSteelOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Album: ${album.label}", color = JdmcMetallicSilver)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = JdmcLightYellow)
                        }
                    }
                    DropdownMenu(expanded = albumExpanded, onDismissRequest = { albumExpanded = false }) {
                        MediaAlbum.entries.forEach { entry ->
                            DropdownMenuItem(
                                text = { Text(entry.label) },
                                onClick = { album = entry; albumExpanded = false }
                            )
                        }
                    }
                }
                Text(
                    text = "Saved locally on device under $addedBy.",
                    style = MaterialTheme.typography.labelSmall,
                    color = JdmcGunmetal
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, caption, album) },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JdmcLightYellow,
                    contentColor = JdmcObsidianBlack
                ),
                modifier = Modifier.testTag("confirm_save_media_button")
            ) {
                Text(if (isSaving) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancel", color = JdmcGunmetal)
            }
        }
    )
}
