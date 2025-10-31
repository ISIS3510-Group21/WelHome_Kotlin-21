package com.team21.myapplication.data.local.dao

import androidx.room.*
import com.team21.myapplication.data.local.entity.DraftPostEntity
import com.team21.myapplication.data.local.entity.DraftImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DraftPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDraft(draft: DraftPostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<DraftImageEntity>)

    @Transaction
    suspend fun upsertWithImages(draft: DraftPostEntity, images: List<DraftImageEntity>) {
        upsertDraft(draft)
        deleteImagesFor(draft.id)
        insertImages(images)
    }

    @Query("SELECT * FROM draft_posts")
    suspend fun getAllDraftsOnce(): List<DraftPostEntity>

    @Query("SELECT * FROM draft_posts")
    fun observeAllDrafts(): Flow<List<DraftPostEntity>>

    @Query("SELECT * FROM draft_posts WHERE id = :draftId LIMIT 1")
    suspend fun getDraftById(draftId: String): DraftPostEntity?

    @Query("SELECT * FROM draft_images WHERE draftId = :draftId ORDER BY localId ASC")
    suspend fun getImagesFor(draftId: String): List<DraftImageEntity>

    @Query("DELETE FROM draft_images WHERE draftId = :draftId")
    suspend fun deleteImagesFor(draftId: String)

    @Query("DELETE FROM draft_posts WHERE id = :draftId")
    suspend fun deleteDraft(draftId: String)
}
