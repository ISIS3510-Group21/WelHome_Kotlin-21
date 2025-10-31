package com.team21.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.team21.myapplication.data.local.converters.MapLocationConverter
import com.team21.myapplication.data.local.converters.TimestampConverter
import com.team21.myapplication.data.local.dao.ForumPostDao
import com.team21.myapplication.data.local.dao.HousingDao
import com.team21.myapplication.data.local.dao.MapCacheDao
import com.team21.myapplication.data.local.dao.MyPostsDao
import com.team21.myapplication.data.local.dao.OwnerOfflinePreviewDao
import com.team21.myapplication.data.local.dao.ThreadForumDao
import com.team21.myapplication.data.local.entity.ForumPostEntity
import com.team21.myapplication.data.local.entity.HousingEntity
import com.team21.myapplication.data.local.entity.MyPostEntity
import com.team21.myapplication.data.local.entity.OwnerOfflinePreviewEntity
import com.team21.myapplication.data.local.entity.ThreadForumEntity
import com.team21.myapplication.data.model.MapCacheEntry

@Database(
    entities = [HousingEntity::class, MyPostEntity::class, OwnerOfflinePreviewEntity::class, MapCacheEntry::class, ThreadForumEntity::class, ForumPostEntity::class],
    version = 4, // Incremented version
    exportSchema = false
)
@TypeConverters(MapLocationConverter::class, TimestampConverter::class) // Added TimestampConverter
abstract class AppDatabase : RoomDatabase() {

    abstract fun housingDao(): HousingDao
    abstract fun myPostsDao(): MyPostsDao
    abstract fun mapCacheDao(): MapCacheDao
    abstract fun ownerOfflineDao(): OwnerOfflinePreviewDao
    abstract fun threadForumDao(): ThreadForumDao // Added Dao
    abstract fun forumPostDao(): ForumPostDao     // Added Dao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "welhome_database"
                )
                    .fallbackToDestructiveMigration() //todo: ajustar para migraciones
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
