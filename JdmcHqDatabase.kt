package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        MemberEntity::class,
        OrganizationRoleEntity::class,
        AnnouncementEntity::class,
        EventEntity::class,
        MediaGalleryEntity::class,
        ArchiveEntryEntity::class,
        ActivityLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(HqTypeConverters::class)
abstract class JdmcHqDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun organizationRoleDao(): OrganizationRoleDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun eventDao(): EventDao
    abstract fun mediaGalleryDao(): MediaGalleryDao
    abstract fun archiveDao(): ArchiveDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile
        private var INSTANCE: JdmcHqDatabase? = null

        fun getInstance(context: Context): JdmcHqDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    JdmcHqDatabase::class.java,
                    "jdmc_digital_hq.db"
                ).build().also { INSTANCE = it }
            }
    }
}
