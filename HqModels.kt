package com.example.domain.model

enum class MemberStatus(val label: String) {
    OFFICER("Officer"),
    ACTIVE("Active"),
    VETERAN("Veteran"),
    PROSPECT("Prospect");

    companion object {
        fun fromRaw(raw: String?): MemberStatus =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) || it.label.equals(raw, ignoreCase = true) }
                ?: ACTIVE
    }
}

data class MemberProfile(
    val id: String,
    val memberNumber: String,
    val characterName: String,
    val clubPosition: String,
    val status: MemberStatus,
    val biography: String,
    val joinDate: String,
    val customRankBadge: String,
    val avatarUri: String = "",
    val sortOrder: Int = 99
)

data class ClubOrganizationRole(
    val id: String,
    val roleTitle: String,
    val tierOrder: Int,
    val indonesianAlias: String,
    val mandate: String,
    val assignedHolders: String,
    val isOfficerTier: Boolean
)

enum class AnnouncementCategory(val label: String) {
    COMMAND("Command Dispatch"),
    RIDE_NOTICE("Ride & Convoy"),
    RP_LORE("Satu Mimpi RP"),
    GENERAL("Club Notice");

    companion object {
        fun fromRaw(raw: String?): AnnouncementCategory =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) || it.label.equals(raw, ignoreCase = true) }
                ?: GENERAL
    }
}

data class ClubAnnouncement(
    val id: String,
    val title: String,
    val content: String,
    val category: AnnouncementCategory,
    val isPinned: Boolean,
    val authorName: String,
    val dateLabel: String,
    val createdAtMs: Long
)

enum class EventStatus(val label: String) {
    UPCOMING("Upcoming"),
    COMPLETED("Completed");

    companion object {
        fun fromRaw(raw: String?): EventStatus =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) || it.label.equals(raw, ignoreCase = true) }
                ?: UPCOMING
    }
}

data class ClubEvent(
    val id: String,
    val title: String,
    val description: String,
    val dateTimeLabel: String,
    val location: String,
    val posterUri: String = "",
    val status: EventStatus,
    val attendees: List<String>,
    val createdAtMs: Long
)

enum class MediaAlbum(val label: String) {
    CLUB_ACTIVITIES("Club Activities"),
    GROUP_PHOTOS("Group Photographs"),
    MOTORCYCLE_COLLECTION("Motorcycle Collections"),
    FIVEM_RP("FiveM RP Screenshots"),
    SPECIAL_MEMORIES("Special Club Memories");

    companion object {
        fun fromRaw(raw: String?): MediaAlbum =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) || it.label.equals(raw, ignoreCase = true) }
                ?: CLUB_ACTIVITIES
    }
}

enum class LocalMediaType(val label: String) {
    PHOTO("Photo"),
    VIDEO("Video");

    companion object {
        fun fromRaw(raw: String?): LocalMediaType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) || it.label.equals(raw, ignoreCase = true) }
                ?: PHOTO
    }
}

data class MediaGalleryItem(
    val id: String,
    val title: String,
    val caption: String,
    val album: MediaAlbum,
    val mediaType: LocalMediaType,
    val mediaUri: String,
    val thumbnailUri: String,
    val addedBy: String,
    val createdAtMs: Long
)

enum class ArchiveSection(val label: String) {
    FOUNDING_HISTORY("Founding History"),
    MILESTONE("Important Milestones"),
    MEMBER_LEGACY("Member Legacy"),
    ACHIEVEMENT("Special Achievements"),
    HISTORICAL_PHOTO("Historical Photographs"),
    CLUB_MEMORY("Club Memories");

    companion object {
        fun fromRaw(raw: String?): ArchiveSection =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) || it.label.equals(raw, ignoreCase = true) }
                ?: FOUNDING_HISTORY
    }
}

data class ArchiveEntry(
    val id: String,
    val section: ArchiveSection,
    val yearEra: String,
    val title: String,
    val narrative: String,
    val imageUri: String = "",
    val sortOrder: Int = 99
)

data class ClubActivityEntry(
    val id: String,
    val summary: String,
    val categoryTag: String,
    val timestampMs: Long
)

object DefaultJdmcHqSeed {
    const val DRAWABLE_CLUB_LOGO = "res://jdmc_club_logo"
    const val DRAWABLE_CLIP_BANNER = "res://img_jdmc_clip_placeholder"

    const val RAW_VIDEO_1 = "res_raw://jdmc_vid_1"
    const val RAW_VIDEO_2 = "res_raw://jdmc_vid_2"
    const val RAW_VIDEO_3 = "res_raw://jdmc_vid_3"
    const val RAW_VIDEO_4 = "res_raw://jdmc_vid_4"
    const val RAW_VIDEO_5 = "res_raw://jdmc_vid_5"
    const val RAW_VIDEO_6 = "res_raw://jdmc_vid_6"

    const val DRAWABLE_VID_THUMB_1 = "res://jdmc_vid_thumb_1"
    const val DRAWABLE_VID_THUMB_2 = "res://jdmc_vid_thumb_2"
    const val DRAWABLE_VID_THUMB_3 = "res://jdmc_vid_thumb_3"
    const val DRAWABLE_VID_THUMB_4 = "res://jdmc_vid_thumb_4"
    const val DRAWABLE_VID_THUMB_5 = "res://jdmc_vid_thumb_5"
    const val DRAWABLE_VID_THUMB_6 = "res://jdmc_vid_thumb_6"

    val initialMembers: List<MemberProfile> = listOf(
        MemberProfile(
            id = "member-gashima",
            memberNumber = "JDMC-001",
            characterName = "Gashima",
            clubPosition = "President",
            status = MemberStatus.OFFICER,
            biography = "Founder and President of Jet Darat Motorcycle Club in Los Santos. Leads club strategy, diplomatic alliances across Satu Mimpi RP, and high-speed 2-Tak night runs.",
            joinDate = "2026-01-10",
            customRankBadge = "CHARTER PRESIDENT",
            avatarUri = DRAWABLE_CLUB_LOGO,
            sortOrder = 1
        ),
        MemberProfile(
            id = "member-melody",
            memberNumber = "JDMC-002",
            characterName = "Melody",
            clubPosition = "Vice President",
            status = MemberStatus.OFFICER,
            biography = "Vice President overseeing daily chapter operations, convoy readiness, and inter-crew coordination throughout Los Santos and Blaine County.",
            joinDate = "2026-01-10",
            customRankBadge = "VICE PRESIDENT",
            avatarUri = DRAWABLE_CLUB_LOGO,
            sortOrder = 2
        ),
        MemberProfile(
            id = "member-crystal",
            memberNumber = "JDMC-003",
            characterName = "Crystal Oda Megumi",
            clubPosition = "The Mother of JDMC",
            status = MemberStatus.VETERAN,
            biography = "Matriarch and moral pillar of Jet Darat Motorcycle Club. Protects brotherhood values, mentors prospects, and anchors the JDMC family legacy.",
            joinDate = "2026-01-12",
            customRankBadge = "THE MOTHER OF JDMC",
            avatarUri = DRAWABLE_CLUB_LOGO,
            sortOrder = 3
        ),
        MemberProfile(
            id = "member-joko",
            memberNumber = "JDMC-004",
            characterName = "Joko Sancez",
            clubPosition = "Charter Veteran",
            status = MemberStatus.VETERAN,
            biography = "Day-one charter rider known across Satu Mimpi RP for tireless loyalty, mechanical mastery on classic 2-stroke machines, and steady leadership on long highways.",
            joinDate = "2026-01-14",
            customRankBadge = "CHARTER VETERAN",
            avatarUri = DRAWABLE_CLUB_LOGO,
            sortOrder = 4
        ),
        MemberProfile(
            id = "member-tashima",
            memberNumber = "JDMC-005",
            characterName = "Tashima",
            clubPosition = "JDMC Member",
            status = MemberStatus.ACTIVE,
            biography = "One half of the famed Duo Tachibana vanguard. Specializes in precision tandem riding, canyon scouting, and nighttime Los Santos escort formations.",
            joinDate = "2026-02-01",
            customRankBadge = "DUO TACHIBANA",
            avatarUri = DRAWABLE_CLUB_LOGO,
            sortOrder = 5
        ),
        MemberProfile(
            id = "member-gonzales",
            memberNumber = "JDMC-006",
            characterName = "Gonzales",
            clubPosition = "JDMC Member",
            status = MemberStatus.ACTIVE,
            biography = "Partner in Duo Tachibana and frontline Full Member of JDMC. Fearless road pace-setter and trusted brother on every Satu Mimpi RP chapter mission.",
            joinDate = "2026-02-01",
            customRankBadge = "DUO TACHIBANA",
            avatarUri = DRAWABLE_CLUB_LOGO,
            sortOrder = 6
        )
    )

    val initialOrganizationRoles: List<ClubOrganizationRole> = listOf(
        ClubOrganizationRole("org-president", "President", 1, "Ketua Umum", "Supreme chapter leader of JDMC, sets club charter, diplomacy, and final rulings.", "Gashima", true),
        ClubOrganizationRole("org-vp", "Vice President", 2, "Wakil Ketua", "Second-in-command coordinating chapter operations and officer execution.", "Melody", true),
        ClubOrganizationRole("org-exec", "Executive Officers", 3, "Dewan Pengurus Inti", "Advisory council including founding elders and senior chapter strategists.", "Crystal Oda Megumi", true),
        ClubOrganizationRole("org-road-captain", "Road Captain", 4, "Komandan Touring", "Plans convoy routes, pit stops, and riding formations across Los Santos.", "Tashima (Interim)", true),
        ClubOrganizationRole("org-saa", "Sergeant at Arms", 5, "Keamanan & Disiplin", "Enforces club patch discipline, security, and convoy perimeter safety.", "Joko Sancez", true),
        ClubOrganizationRole("org-treasurer", "Treasurer", 6, "Bendahara", "Manages club treasury, event logistics, and garage asset budgets.", "Melody", true),
        ClubOrganizationRole("org-secretary", "Secretary", 7, "Sekretaris", "Maintains official rosters, digital archives, and chapter announcements.", "Crystal Oda Megumi", true),
        ClubOrganizationRole("org-full-member", "Full Members", 8, "Anggota Penuh", "Patched brothers and sisters representing JDMC across Satu Mimpi RP.", "Tashima, Gonzales", false),
        ClubOrganizationRole("org-prospects", "Prospects", 9, "Calon Anggota", "Probationary riders earning their patch through loyalty and chapter rides.", "Open Recruitment", false),
        ClubOrganizationRole("org-veterans", "Veterans", 10, "Veteran Charter", "Honored long-standing elders who forged JDMC history from day one.", "Crystal Oda Megumi, Joko Sancez", false)
    )

    fun initialAnnouncements(nowMs: Long = System.currentTimeMillis()): List<ClubAnnouncement> = listOf(
        ClubAnnouncement(
            id = "ann-charter-hq",
            title = "Welcome to JDMC DIGITAL HQ — Satu Mimpi RP",
            content = "All Jet Darat Motorcycle Club riders: check in before midnight convoys, keep RX-King 2-Tak builds tuned, and upload RP highlights directly in Media or Clipper Room. Satu Jalan, Satu Mimpi, Satu Keluarga.",
            category = AnnouncementCategory.COMMAND,
            isPinned = true,
            authorName = "Gashima • President",
            dateLabel = "2026-11-19",
            createdAtMs = nowMs
        ),
        ClubAnnouncement(
            id = "ann-convoy-brief",
            title = "Midnight Paleto Loop Escort Protocol",
            content = "Duo Tachibana (Tashima & Gonzales) will lead point formation starting from the Los Santos clubhouse at 22:00 WIB. Full patch gear and full tank mandatory.",
            category = AnnouncementCategory.RIDE_NOTICE,
            isPinned = true,
            authorName = "Melody • Vice President",
            dateLabel = "2026-11-18",
            createdAtMs = nowMs - 3_600_000L
        ),
        ClubAnnouncement(
            id = "ann-legacy-night",
            title = "Archive & Media Vault Open for Chapter Memories",
            content = "Members can now save FiveM roleplay screenshots and riding videos straight from their device gallery into the Media tab.",
            category = AnnouncementCategory.RP_LORE,
            isPinned = false,
            authorName = "Crystal Oda Megumi • Mother of JDMC",
            dateLabel = "2026-11-17",
            createdAtMs = nowMs - 7_200_000L
        )
    )

    fun initialEvents(nowMs: Long = System.currentTimeMillis()): List<ClubEvent> = listOf(
        ClubEvent(
            id = "evt-night-run",
            title = "RX-King 2-Tak Midnight Thunder Run",
            description = "Full-chapter rolling convoy from Del Perro Pier through Route 68 to Sandy Shores Airfield, followed by brotherhood roundtable.",
            dateTimeLabel = "Sat • 22:00 WIB",
            location = "Los Santos Clubhouse -> Route 68",
            posterUri = DRAWABLE_CLUB_LOGO,
            status = EventStatus.UPCOMING,
            attendees = listOf("Gashima", "Melody", "Crystal Oda Megumi", "Joko Sancez", "Tashima", "Gonzales"),
            createdAtMs = nowMs
        ),
        ClubEvent(
            id = "evt-duo-sprint",
            title = "Duo Tachibana Tandem Precision Trial",
            description = "High-speed mountain canyon escort drill led by Tashima & Gonzales for Full Members and Prospects.",
            dateTimeLabel = "Sun • 20:30 WIB",
            location = "Vinewood Hills & Tongva Canyon",
            posterUri = DRAWABLE_CLIP_BANNER,
            status = EventStatus.UPCOMING,
            attendees = listOf("Tashima", "Gonzales", "Melody"),
            createdAtMs = nowMs - 1_800_000L
        ),
        ClubEvent(
            id = "evt-charter-night",
            title = "JDMC Los Santos Est. 2026 Charter Ride",
            description = "Historic inaugural patch ride cementing Jet Darat Motorcycle Club's brotherhood across Satu Mimpi RP.",
            dateTimeLabel = "2026-01-10 • Completed",
            location = "Mirror Park Clubhouse, Los Santos",
            posterUri = DRAWABLE_CLUB_LOGO,
            status = EventStatus.COMPLETED,
            attendees = listOf("Gashima", "Melody", "Crystal Oda Megumi", "Joko Sancez"),
            createdAtMs = nowMs - 86_400_000L
        )
    )

    fun bundledVideos(nowMs: Long = System.currentTimeMillis()): List<MediaGalleryItem> = listOf(
        MediaGalleryItem(
            id = "media-video-xero-lineup",
            title = "Xero Station Street Formation",
            caption = "Full JDMC crew street dance & bike lineup at Xero gas station — Satu Jalan, Satu Mimpi, Satu Keluarga.",
            album = MediaAlbum.GROUP_PHOTOS,
            mediaType = LocalMediaType.VIDEO,
            mediaUri = RAW_VIDEO_1,
            thumbnailUri = DRAWABLE_VID_THUMB_1,
            addedBy = "Gashima",
            createdAtMs = nowMs - 100_000L
        ),
        MediaGalleryItem(
            id = "media-video-ls-port-spotlight",
            title = "LS Port Spotlight: Gashima, Melody & Troy",
            caption = "Cinematic Satu Mimpi RP showcase featuring JDMC RX-King 2-Tak and midnight purple GTR at Los Santos Port.",
            album = MediaAlbum.FIVEM_RP,
            mediaType = LocalMediaType.VIDEO,
            mediaUri = RAW_VIDEO_2,
            thumbnailUri = DRAWABLE_VID_THUMB_2,
            addedBy = "Melody",
            createdAtMs = nowMs - 200_000L
        ),
        MediaGalleryItem(
            id = "media-video-rxking-anthem",
            title = "Los Santos 2026: RX-King on the Road",
            caption = "Official Jet Darat Motorcycle Club anthem edit through midnight canyon curves and full-chapter highway runs.",
            album = MediaAlbum.SPECIAL_MEMORIES,
            mediaType = LocalMediaType.VIDEO,
            mediaUri = RAW_VIDEO_3,
            thumbnailUri = DRAWABLE_VID_THUMB_3,
            addedBy = "Gashima",
            createdAtMs = nowMs - 300_000L
        ),
        MediaGalleryItem(
            id = "media-video-harbor-fleet",
            title = "Midnight Harbor Boat Convoy",
            caption = "JDMC chapter tactical night crossing over Los Santos harbor waters on Satu Mimpi RP.",
            album = MediaAlbum.CLUB_ACTIVITIES,
            mediaType = LocalMediaType.VIDEO,
            mediaUri = RAW_VIDEO_4,
            thumbnailUri = DRAWABLE_VID_THUMB_4,
            addedBy = "Joko Sancez",
            createdAtMs = nowMs - 400_000L
        ),
        MediaGalleryItem(
            id = "media-video-mural-crew",
            title = "JDMC Los Santos Mural Roll Call",
            caption = "Patched riders gathering in front of the RX-King 2-Tak JDMC Los Santos mural wall.",
            album = MediaAlbum.GROUP_PHOTOS,
            mediaType = LocalMediaType.VIDEO,
            mediaUri = RAW_VIDEO_5,
            thumbnailUri = DRAWABLE_VID_THUMB_5,
            addedBy = "Duo Tachibana",
            createdAtMs = nowMs - 500_000L
        ),
        MediaGalleryItem(
            id = "media-video-engine-rebuilds",
            title = "Engine Rebuilds RX-King Garage Session",
            caption = "2-Tak garage meetup inside Engine Rebuilds workshop with lined-up chapter bikes and celebration.",
            album = MediaAlbum.MOTORCYCLE_COLLECTION,
            mediaType = LocalMediaType.VIDEO,
            mediaUri = RAW_VIDEO_6,
            thumbnailUri = DRAWABLE_VID_THUMB_6,
            addedBy = "Tashima",
            createdAtMs = nowMs - 600_000L
        )
    )

    fun initialMediaItems(nowMs: Long = System.currentTimeMillis()): List<MediaGalleryItem> =
        bundledVideos(nowMs) + listOf(
            MediaGalleryItem(
                id = "media-emblem-official",
                title = "JDMC Official Crest — Est. 2026",
                caption = "Jet Darat Motorcycle Club Los Santos RX-King 2-Tak official patch insignia.",
                album = MediaAlbum.SPECIAL_MEMORIES,
                mediaType = LocalMediaType.PHOTO,
                mediaUri = DRAWABLE_CLUB_LOGO,
                thumbnailUri = DRAWABLE_CLUB_LOGO,
                addedBy = "Gashima",
                createdAtMs = nowMs - 1_800_000L
            ),
            MediaGalleryItem(
                id = "media-night-highway",
                title = "Midnight Highway Patrol Formation",
                caption = "Satu Mimpi RP night run silhouette along the Los Santos freeway.",
                album = MediaAlbum.FIVEM_RP,
                mediaType = LocalMediaType.PHOTO,
                mediaUri = DRAWABLE_CLIP_BANNER,
                thumbnailUri = DRAWABLE_CLIP_BANNER,
                addedBy = "Duo Tachibana",
                createdAtMs = nowMs - 3_600_000L
            )
        )

    val initialArchiveEntries: List<ArchiveEntry> = listOf(
        ArchiveEntry(
            id = "arch-founding",
            section = ArchiveSection.FOUNDING_HISTORY,
            yearEra = "EST. 2026 • CHAPTER I",
            title = "Birth of Jet Darat Motorcycle Club",
            narrative = "Forged on the asphalt of Los Santos within Satu Mimpi RP, JDMC united riders dedicated to classic 2-stroke grit, unbreakable loyalty, and the creed: Satu Jalan, Satu Mimpi, Satu Keluarga.",
            imageUri = DRAWABLE_CLUB_LOGO,
            sortOrder = 1
        ),
        ArchiveEntry(
            id = "arch-milestone-patch",
            section = ArchiveSection.MILESTONE,
            yearEra = "2026 • CHARTER MILESTONE",
            title = "First Full-Chapter Convoy Across Los Santos",
            narrative = "Led by President Gashima and Vice President Melody, the inaugural formation ride established JDMC's presence from Mirror Park to Paleto Bay.",
            imageUri = DRAWABLE_CLIP_BANNER,
            sortOrder = 2
        ),
        ArchiveEntry(
            id = "arch-legacy-matriarch",
            section = ArchiveSection.MEMBER_LEGACY,
            yearEra = "LEGACY • PILLARS",
            title = "Crystal Oda Megumi & Joko Sancez",
            narrative = "Remembered as The Mother of JDMC and Charter Veteran respectively, their mentorship turned individual street riders into a disciplined family.",
            imageUri = DRAWABLE_CLUB_LOGO,
            sortOrder = 3
        ),
        ArchiveEntry(
            id = "arch-achievement-duo",
            section = ArchiveSection.ACHIEVEMENT,
            yearEra = "2026 • ROAD HONORS",
            title = "Duo Tachibana Escort Distinction",
            narrative = "Tashima and Gonzales completed ten consecutive zero-incident night highway escorts, earning the honorary Duo Tachibana tandem designation.",
            imageUri = DRAWABLE_CLIP_BANNER,
            sortOrder = 4
        )
    )

    fun initialActivities(nowMs: Long = System.currentTimeMillis()): List<ClubActivityEntry> = listOf(
        ClubActivityEntry("act-1", "President Gashima pinned the Official Command Dispatch.", "COMMAND", nowMs),
        ClubActivityEntry("act-2", "Duo Tachibana confirmed point formation for Midnight Thunder Run.", "EVENT", nowMs - 1_800_000L),
        ClubActivityEntry("act-3", "Crystal Oda Megumi updated the JDMC Digital Archive.", "ARCHIVE", nowMs - 3_600_000L)
    )
}
