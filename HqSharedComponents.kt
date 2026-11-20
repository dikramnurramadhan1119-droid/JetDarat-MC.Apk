package com.example.ui.components

import android.content.Context
import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.domain.model.DefaultJdmcHqSeed
import com.example.domain.model.LocalMediaType
import com.example.domain.model.MediaGalleryItem
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
import java.io.File

@Composable
fun HqMediaImage(
    uriOrResource: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    when {
        uriOrResource == DefaultJdmcHqSeed.DRAWABLE_CLUB_LOGO -> {
            Image(
                painter = painterResource(id = R.drawable.jdmc_club_logo),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
        uriOrResource == DefaultJdmcHqSeed.DRAWABLE_VID_THUMB_1 -> {
            Image(
                painter = painterResource(id = R.drawable.jdmc_vid_thumb_1),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
        uriOrResource == DefaultJdmcHqSeed.DRAWABLE_VID_THUMB_2 -> {
            Image(
                painter = painterResource(id = R.drawable.jdmc_vid_thumb_2),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
        uriOrResource == DefaultJdmcHqSeed.DRAWABLE_VID_THUMB_3 -> {
            Image(
                painter = painterResource(id = R.drawable.jdmc_vid_thumb_3),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
        uriOrResource == DefaultJdmcHqSeed.DRAWABLE_VID_THUMB_4 -> {
            Image(
                painter = painterResource(id = R.drawable.jdmc_vid_thumb_4),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
        uriOrResource == DefaultJdmcHqSeed.DRAWABLE_VID_THUMB_5 -> {
            Image(
                painter = painterResource(id = R.drawable.jdmc_vid_thumb_5),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
        uriOrResource == DefaultJdmcHqSeed.DRAWABLE_VID_THUMB_6 -> {
            Image(
                painter = painterResource(id = R.drawable.jdmc_vid_thumb_6),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
        uriOrResource == DefaultJdmcHqSeed.DRAWABLE_CLIP_BANNER || uriOrResource.isBlank() -> {
            Image(
                painter = painterResource(id = R.drawable.img_jdmc_clip_placeholder),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
        uriOrResource.startsWith("file://") -> {
            val file = Uri.parse(uriOrResource).path?.let { File(it) }
            AsyncImage(
                model = file ?: uriOrResource,
                contentDescription = contentDescription,
                placeholder = painterResource(id = R.drawable.img_jdmc_clip_placeholder),
                error = painterResource(id = R.drawable.img_jdmc_clip_placeholder),
                contentScale = contentScale,
                modifier = modifier
            )
        }
        else -> {
            AsyncImage(
                model = uriOrResource,
                contentDescription = contentDescription,
                placeholder = painterResource(id = R.drawable.img_jdmc_clip_placeholder),
                error = painterResource(id = R.drawable.img_jdmc_clip_placeholder),
                contentScale = contentScale,
                modifier = modifier
            )
        }
    }
}

@Composable
fun HqActionBanner(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (message.isNullOrBlank()) return
    Surface(
        color = JdmcCarbonElevated,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, JdmcLightYellow),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = JdmcMetallicSilver,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = JdmcLightYellow)
            }
        }
    }
}

@Composable
fun StatCounterTile(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = JdmcCarbonSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, JdmcSteelOutline),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = JdmcLightYellow,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = JdmcGunmetal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = JdmcLightYellow,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OfficerModeBadge(
    isOfficerActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isOfficerActive) JdmcGoldContainer else JdmcCarbonElevated,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, if (isOfficerActive) JdmcLightYellow else JdmcSteelOutline),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                tint = if (isOfficerActive) JdmcLightYellow else JdmcGunmetal,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = if (isOfficerActive) "OFFICER ADMIN" else "MEMBER VIEW",
                style = MaterialTheme.typography.labelSmall,
                color = if (isOfficerActive) JdmcLightYellow else JdmcBrushedSilver,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun MediaGalleryViewerDialog(
    item: MediaGalleryItem,
    canDelete: Boolean,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = JdmcObsidianBlack,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, JdmcGoldBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("media_preview_dialog")
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (item.mediaType == LocalMediaType.VIDEO) Icons.Filled.Movie else Icons.Filled.Photo,
                            contentDescription = null,
                            tint = JdmcLightYellow,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = JdmcLightYellow,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close preview",
                            tint = JdmcMetallicSilver
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp, max = 420.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(JdmcCarbonSurface),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.mediaType == LocalMediaType.VIDEO && item.mediaUri.isNotBlank()) {
                        AndroidView(
                            factory = { ctx ->
                                FrameLayout(ctx).apply {
                                    val videoView = VideoView(ctx).apply {
                                        val controller = MediaController(ctx)
                                        controller.setAnchorView(this)
                                        setMediaController(controller)
                                        setVideoURI(resolveVideoUri(ctx, item.mediaUri))
                                        setOnPreparedListener { mp ->
                                            mp.isLooping = false
                                            start()
                                        }
                                    }
                                    addView(
                                        videoView,
                                        FrameLayout.LayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            Gravity.CENTER
                                        )
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 240.dp, max = 420.dp)
                        )
                    } else {
                        HqMediaImage(
                            uriOrResource = item.mediaUri,
                            contentDescription = item.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 220.dp, max = 420.dp)
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
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "Added by ${item.addedBy}",
                        style = MaterialTheme.typography.labelSmall,
                        color = JdmcGunmetal
                    )
                }

                if (item.caption.isNotBlank()) {
                    Text(
                        text = item.caption,
                        style = MaterialTheme.typography.bodyMedium,
                        color = JdmcBrushedSilver
                    )
                }

                if (canDelete && onDelete != null) {
                    OutlinedButton(
                        onClick = onDelete,
                        border = BorderStroke(1.dp, JdmcGoldBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JdmcAmberAlert),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Remove from Media Gallery")
                    }
                }
            }
        }
    }
}

@Composable
fun VideoBadgeOverlay(modifier: Modifier = Modifier) {
    Surface(
        color = JdmcObsidianBlack.copy(alpha = 0.82f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, JdmcLightYellow.copy(alpha = 0.8f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PlayCircle,
                contentDescription = "Video",
                tint = JdmcLightYellow,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "VIDEO",
                style = MaterialTheme.typography.labelSmall,
                color = JdmcLightYellow,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

internal fun resolveVideoUri(context: Context, mediaUri: String): Uri {
    val rawResId = when (mediaUri) {
        DefaultJdmcHqSeed.RAW_VIDEO_1 -> R.raw.jdmc_vid_1
        DefaultJdmcHqSeed.RAW_VIDEO_2 -> R.raw.jdmc_vid_2
        DefaultJdmcHqSeed.RAW_VIDEO_3 -> R.raw.jdmc_vid_3
        DefaultJdmcHqSeed.RAW_VIDEO_4 -> R.raw.jdmc_vid_4
        DefaultJdmcHqSeed.RAW_VIDEO_5 -> R.raw.jdmc_vid_5
        DefaultJdmcHqSeed.RAW_VIDEO_6 -> R.raw.jdmc_vid_6
        else -> null
    }
    return if (rawResId != null) {
        Uri.parse("android.resource://${context.packageName}/$rawResId")
    } else {
        Uri.parse(mediaUri)
    }
}
