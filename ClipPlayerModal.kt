package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.ClipSubmission
import com.example.ui.theme.JdmcBrushedSilver
import com.example.ui.theme.JdmcCarbonElevated
import com.example.ui.theme.JdmcCarbonSurface
import com.example.ui.theme.JdmcGunmetal
import com.example.ui.theme.JdmcLightYellow
import com.example.ui.theme.JdmcMetallicSilver
import com.example.ui.theme.JdmcObsidianBlack
import com.example.ui.theme.JdmcSteelOutline
import com.example.util.VideoUrlHelper

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ClipPlayerDialog(
    clip: ClipSubmission,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val youtubeId = remember(clip.videoUrl) { VideoUrlHelper.extractYouTubeVideoId(clip.videoUrl) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = JdmcCarbonSurface,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, JdmcSteelOutline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = clip.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = JdmcLightYellow
                        )
                        Text(
                            text = "Clipper: ${clip.clipperDisplayName} (${clip.clipperJabatan}) • ${clip.streamerName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = JdmcGunmetal
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_player_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close player",
                            tint = JdmcMetallicSilver
                        )
                    }
                }

                if (youtubeId != null) {
                    val embedHtml = remember(youtubeId) { VideoUrlHelper.buildYouTubeEmbedHtml(youtubeId) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(JdmcObsidianBlack)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.mediaPlaybackRequiresUserGesture = false
                                    settings.allowFileAccess = false
                                    settings.allowContentAccess = false
                                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                                    webChromeClient = WebChromeClient()
                                    webViewClient = object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(
                                            view: WebView?,
                                            request: WebResourceRequest?
                                        ): Boolean {
                                            val target = request?.url ?: return true
                                            val host = target.host.orEmpty()
                                            return !(host.contains("youtube.com") || host.contains("youtu.be") || host.contains("googlevideo.com"))
                                        }
                                    }
                                    loadDataWithBaseURL(
                                        "https://www.youtube.com",
                                        embedHtml,
                                        "text/html",
                                        "utf-8",
                                        null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Surface(
                        color = JdmcCarbonElevated,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, JdmcSteelOutline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = JdmcLightYellow,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Embedded player is supported for YouTube & Shorts links. Open ${clip.platform.displayName} externally to watch.",
                                style = MaterialTheme.typography.bodySmall,
                                color = JdmcBrushedSilver
                            )
                        }
                    }
                }

                if (clip.description.isNotBlank()) {
                    Text(
                        text = clip.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = JdmcBrushedSilver
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlatformBadge(platform = clip.platform)
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(clip.videoUrl))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JdmcLightYellow,
                            contentColor = JdmcObsidianBlack
                        ),
                        modifier = Modifier.testTag("player_watch_external_button")
                    ) {
                        Text("Watch on ${clip.platform.displayName}")
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
}
