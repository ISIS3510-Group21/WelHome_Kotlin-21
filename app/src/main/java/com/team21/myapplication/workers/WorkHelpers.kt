package com.team21.myapplication.workers

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import android.content.Context
import androidx.work.ExistingWorkPolicy

fun enqueueUploadDraft(context: Context, draftId: String) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val request  = OneTimeWorkRequestBuilder<UploadDraftWorker>()
        .setConstraints(constraints)
        .setInputData(workDataOf(UploadDraftWorker.KEY_DRAFT_ID to draftId))
        .addTag("uploadDraft:$draftId")
        .build()

    // unico por draftId (keep evita duplicados)
    WorkManager.getInstance(context).enqueueUniqueWork(
        "uploadDraft:$draftId",
        ExistingWorkPolicy.KEEP,
        request
    )
}

fun enqueueUploadScheduleDraft(context: Context, draftId: String) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val request = OneTimeWorkRequestBuilder<UploadScheduleDraftWorker>()
        .setConstraints(constraints)
        .setInputData(
            workDataOf(UploadScheduleDraftWorker.KEY_DRAFT_ID to draftId)
        )
        .addTag("uploadScheduleDraft:$draftId")
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "uploadScheduleDraft:$draftId",
        ExistingWorkPolicy.KEEP,
        request
    )
}
