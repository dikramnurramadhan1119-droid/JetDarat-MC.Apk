package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SessionPreferencesStore
import com.example.data.repository.ApiResult
import com.example.data.repository.ClipperRepository
import com.example.domain.model.DefaultJdmcHqSeed
import com.example.domain.model.DefaultJdmcJabatan
import com.example.domain.model.DefaultJdmcRoster
import com.example.domain.model.VideoPlatform
import com.example.util.VideoUrlHelper
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class JdmcClipperUnitTest {

    @Test
    fun `extractYouTubeVideoId parses watch shorts and youtu be links`() {
        assertEquals("dQw4w9WgXcQ", VideoUrlHelper.extractYouTubeVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", VideoUrlHelper.extractYouTubeVideoId("https://youtube.com/shorts/dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", VideoUrlHelper.extractYouTubeVideoId("https://youtu.be/dQw4w9WgXcQ"))
        assertNull(VideoUrlHelper.extractYouTubeVideoId("https://www.tiktok.com/@jdmc/video/123456"))
    }

    @Test
    fun `supportsInAppPlayback only true for valid YouTube platforms and IDs`() {
        assertTrue(VideoUrlHelper.supportsInAppPlayback(VideoPlatform.YOUTUBE, "https://youtu.be/dQw4w9WgXcQ"))
        assertTrue(VideoUrlHelper.supportsInAppPlayback(VideoPlatform.YOUTUBE_SHORTS, "https://youtube.com/shorts/dQw4w9WgXcQ"))
        assertFalse(VideoUrlHelper.supportsInAppPlayback(VideoPlatform.TIKTOK, "https://www.tiktok.com/@jdmc/video/123"))
        assertFalse(VideoUrlHelper.supportsInAppPlayback(VideoPlatform.YOUTUBE, "https://youtube.com/channel/abc"))
    }

    @Test
    fun `isValidHttpsUrl enforces https scheme`() {
        assertTrue(VideoUrlHelper.isValidHttpsUrl("https://api.jdmc-clippers.example.com/"))
        assertFalse(VideoUrlHelper.isValidHttpsUrl("http://insecure.example.com/"))
        assertFalse(VideoUrlHelper.isValidHttpsUrl("not-a-url"))
    }

    @Test
    fun `formatRemainingDuration formats minutes and hours cleanly`() {
        assertEquals("04:05", VideoUrlHelper.formatRemainingDuration(245))
        assertEquals("01:02:03", VideoUrlHelper.formatRemainingDuration(3723))
    }

    @Test
    fun `verifyAccessCode returns BackendUnavailable when no backend URL is configured`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ClipperRepository(context, SessionPreferencesStore(context))
        val result = repo.verifyAccessCode("TEST-CODE-123")
        assertTrue(result is ApiResult.BackendUnavailable)
    }

    @Test
    fun `authenticateAdmin returns BackendUnavailable when no backend URL is configured`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ClipperRepository(context, SessionPreferencesStore(context))
        val result = repo.authenticateAdmin("ADMIN-KEY-123")
        assertTrue(result is ApiResult.BackendUnavailable)
    }

    @Test
    fun `default JDMC Jabatan hierarchy starts with President and has 10 roles`() {
        val positions = DefaultJdmcJabatan.initialPositions
        val roster = DefaultJdmcRoster.initialStreamers
        assertEquals(10, positions.size)
        assertEquals("President", positions.first().rankTitle)
        assertTrue(positions.any { it.rankTitle == "Executive Officers" })
        assertTrue(positions.any { it.rankTitle == "Sergeant at Arms" })
        assertTrue(positions.any { it.rankTitle == "Road Captain" })
        assertTrue(positions.any { it.rankTitle == "Veterans" })
        assertEquals(6, roster.size)
        assertTrue(roster.all { it.jabatan.isNotBlank() })
    }

    @Test
    fun `default HQ seed contains all 6 core JDMC characters with exact positions`() {
        val members = DefaultJdmcHqSeed.initialMembers
        assertEquals("President", members.first { it.characterName == "Gashima" }.clubPosition)
        assertEquals("Vice President", members.first { it.characterName == "Melody" }.clubPosition)
        assertEquals("The Mother of JDMC", members.first { it.characterName == "Crystal Oda Megumi" }.clubPosition)
        assertEquals("Charter Veteran", members.first { it.characterName == "Joko Sancez" }.clubPosition)
        assertEquals("DUO TACHIBANA", members.first { it.characterName == "Tashima" }.customRankBadge)
        assertEquals("DUO TACHIBANA", members.first { it.characterName == "Gonzales" }.customRankBadge)
    }
}
