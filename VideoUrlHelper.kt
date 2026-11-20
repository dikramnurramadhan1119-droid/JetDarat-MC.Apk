package com.example.util

import android.net.Uri
import com.example.domain.model.VideoPlatform
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object VideoUrlHelper {

    private val youtubeIdRegex = Regex("^[A-Za-z0-9_-]{11}$")

    fun isValidHttpsUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (!trimmed.startsWith("https://", ignoreCase = true)) return false
        return try {
            val uri = Uri.parse(trimmed)
            !uri.host.isNullOrBlank()
        } catch (_: Exception) {
            false
        }
    }

    fun extractYouTubeVideoId(url: String): String? {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return null
        return try {
            val uri = Uri.parse(trimmed)
            val host = uri.host?.lowercase(Locale.US) ?: return null
            val candidate = when {
                host == "youtu.be" || host.endsWith(".youtu.be") -> {
                    uri.pathSegments.firstOrNull()
                }
                host.contains("youtube.com") -> {
                    val segments = uri.pathSegments
                    when {
                        segments.firstOrNull() == "shorts" && segments.size >= 2 -> segments[1]
                        segments.firstOrNull() == "embed" && segments.size >= 2 -> segments[1]
                        segments.firstOrNull() == "live" && segments.size >= 2 -> segments[1]
                        else -> uri.getQueryParameter("v")
                    }
                }
                else -> null
            }
            candidate?.takeIf { youtubeIdRegex.matches(it) }
        } catch (_: Exception) {
            null
        }
    }

    fun buildYouTubeEmbedHtml(videoId: String): String {
        val safeId = videoId.filter { it.isLetterOrDigit() || it == '_' || it == '-' }
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
              <style>
                html, body { margin: 0; padding: 0; background-color: #09090B; height: 100%; width: 100%; overflow: hidden; }
                iframe { border: 0; width: 100%; height: 100%; }
              </style>
            </head>
            <body>
              <iframe
                src="https://www.youtube.com/embed/$safeId?autoplay=1&rel=0&playsinline=1"
                allow="autoplay; encrypted-media; picture-in-picture; fullscreen"
                allowfullscreen>
              </iframe>
            </body>
            </html>
        """.trimIndent()
    }

    fun supportsInAppPlayback(platform: VideoPlatform, url: String): Boolean {
        return (platform == VideoPlatform.YOUTUBE || platform == VideoPlatform.YOUTUBE_SHORTS) &&
            extractYouTubeVideoId(url) != null
    }

    fun currentIsoTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    fun formatDisplayDate(iso: String?): String {
        if (iso.isNullOrBlank()) return "Pending"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val clean = iso.substringBefore(".").removeSuffix("Z")
            val parsed = parser.parse(clean)
            if (parsed != null) {
                SimpleDateFormat("MMM dd, yyyy", Locale.US).format(parsed)
            } else {
                iso.take(10)
            }
        } catch (_: Exception) {
            iso.take(10)
        }
    }

    fun formatRemainingDuration(totalSeconds: Long): String {
        val safe = totalSeconds.coerceAtLeast(0L)
        val hours = safe / 3600
        val minutes = (safe % 3600) / 60
        val seconds = safe % 60
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }
}
