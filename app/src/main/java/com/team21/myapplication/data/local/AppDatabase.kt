package com.team21.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.team21.myapplication.data.local.dao.HousingDao
import com.team21.myapplication.data.local.dao.MyPostsDao
import com.team21.myapplication.data.local.entity.HousingEntity
import com.team21.myapplication.data.local.entity.MyPostEntity
import com.team21.myapplication.data.local.dao.OwnerOfflinePreviewDao
import com.team21.myapplication.data.local.entity.OwnerOfflinePreviewEntity
@Database(entities = [HousingEntity::class, MyPostEntity::class, OwnerOfflinePreviewEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun housingDao(): HousingDao
    abstract fun myPostsDao(): MyPostsDao
    abstract fun ownerOfflineDao(): OwnerOfflinePreviewDao

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