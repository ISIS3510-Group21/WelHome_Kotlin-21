package com.team21.myapplication.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.team21.myapplication.local.db.dao.DetailSnapshotDao
import com.team21.myapplication.local.db.entity.DetailSnapshotEntity

/**
 * [LOCAL STORAGE]
 * Room database including the Detail snapshot table.
 *
 * If you already have an AppDatabase in your project, add this entity & dao there and bump version.
 */
@Database(
    entities = [
        DetailSnapshotEntity::class
    ],
    version = 1, // bump if you add more entities or change schema
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun detailSnapshotDao(): DetailSnapshotDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(ctx: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    ctx.applicationContext,
                    AppDatabase::class.java,
                    "welhome.db"
                )
                    .fallbackToDestructiveMigration() // OK for coursework; use Migrations in production
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
