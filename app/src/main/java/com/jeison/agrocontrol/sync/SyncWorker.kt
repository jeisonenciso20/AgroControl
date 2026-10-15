package com.jeison.agrocontrol.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jeison.agrocontrol.AgroControlApp

class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as AgroControlApp
        return app.syncManager.syncAll().fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }
}
