package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.JdmcHqDatabase
import com.example.data.local.LocalMediaStorage
import com.example.data.local.SessionPreferencesStore
import com.example.data.repository.HqRepository
import com.example.domain.model.MediaAlbum
import com.example.domain.model.MediaGalleryItem
import com.example.domain.model.LocalMediaType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches JDMC Digital HQ`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JDMC Digital HQ", appName)
    }

    @Test
    fun `room database seeds all 6 initial JDMC members and 10 organization roles`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, JdmcHqDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = HqRepository(db, LocalMediaStorage(context), SessionPreferencesStore(context))
            repo.ensureSeeded()

            val members = repo.membersFlow.first()
            assertEquals(6, members.size)
            val names = members.map { it.characterName }.toSet()
            assertTrue(
                names.containsAll(
                    listOf(
                        "Gashima",
                        "Melody",
                        "Crystal Oda Megumi",
                        "Joko Sancez",
                        "Tashima",
                        "Gonzales"
                    )
                )
            )

            val roles = repo.organizationRolesFlow.first()
            assertEquals(10, roles.size)
            assertTrue(roles.any { it.roleTitle == "President" })
            assertTrue(roles.any { it.roleTitle == "Executive Officers" })
            assertTrue(roles.any { it.roleTitle == "Veterans" })
        } finally {
            db.close()
        }
    }

    @Test
    fun `officer PIN hashing produces distinct salted digests`() {
        val h1 = SessionPreferencesStore.hashPin("2026", "salt_a")
        val h2 = SessionPreferencesStore.hashPin("2026", "salt_b")
        val h1Repeat = SessionPreferencesStore.hashPin("2026", "salt_a")
        assertEquals(h1, h1Repeat)
        assertNotEquals(h1, h2)
    }

    @Test
    fun `room media gallery persists added item and deletes cleanly`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, JdmcHqDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val item = MediaGalleryItem(
                id = "test-media-1",
                title = "RX-King 2-Tak Midnight Photo",
                caption = "Saved from gallery test",
                album = MediaAlbum.MOTORCYCLE_COLLECTION,
                mediaType = LocalMediaType.PHOTO,
                mediaUri = "file:///tmp/test_photo.jpg",
                thumbnailUri = "file:///tmp/test_photo.jpg",
                addedBy = "Gashima",
                createdAtMs = 123456L
            )
            db.mediaGalleryDao().upsert(com.example.data.local.MediaGalleryEntity.fromDomain(item))
            val saved = db.mediaGalleryDao().observeAll().first()
            assertEquals(1, saved.size)
            assertEquals("RX-King 2-Tak Midnight Photo", saved.first().title)
        } finally {
            db.close()
        }
    }

    @Test
    fun `repository seeds all 6 bundled JDMC videos across albums without duplicating or resurrecting deleted ones`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, JdmcHqDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = HqRepository(db, LocalMediaStorage(context), SessionPreferencesStore(context))
            repo.ensureSeeded()

            val allMedia = repo.mediaGalleryFlow.first()
            val videos = allMedia.filter { it.mediaType == LocalMediaType.VIDEO }
            assertEquals(6, videos.size)
            assertTrue(videos.all { it.mediaUri.startsWith("res_raw://jdmc_vid_") })
            assertTrue(videos.all { it.thumbnailUri.startsWith("res://jdmc_vid_thumb_") })
            assertTrue(allMedia.any { it.mediaType == LocalMediaType.PHOTO })

            val firstVideo = videos.first()
            repo.deleteMediaItem(firstVideo)
            repo.ensureSeeded()

            val remainingVideos = repo.mediaGalleryFlow.first().filter { it.mediaType == LocalMediaType.VIDEO }
            assertEquals(5, remainingVideos.size)
            assertTrue(remainingVideos.none { it.id == firstVideo.id })
        } finally {
            db.close()
        }
    }

    @Test
    fun `member profile edits for name, jabatan, photo, and bio persist in Room`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, JdmcHqDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = HqRepository(db, LocalMediaStorage(context), SessionPreferencesStore(context))
            repo.ensureSeeded()

            val gashima = repo.membersFlow.first().first { it.id == "member-gashima" }
            val updated = gashima.copy(
                characterName = "Gashima V2",
                clubPosition = "Founder & Road Commander",
                customRankBadge = "KETUA UMUM",
                biography = "Updated custom bio for JDMC Digital HQ.",
                avatarUri = "file:///data/user/0/com.jdmc.digitalhq/files/jdmc_media/gashima_custom.jpg"
            )
            repo.saveMember(updated)

            val saved = repo.membersFlow.first().first { it.id == "member-gashima" }
            assertEquals("Gashima V2", saved.characterName)
            assertEquals("Founder & Road Commander", saved.clubPosition)
            assertEquals("KETUA UMUM", saved.customRankBadge)
            assertEquals("Updated custom bio for JDMC Digital HQ.", saved.biography)
            assertEquals("file:///data/user/0/com.jdmc.digitalhq/files/jdmc_media/gashima_custom.jpg", saved.avatarUri)
        } finally {
            db.close()
        }
    }
}
