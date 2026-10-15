package com.jeison.agrocontrol.data

import androidx.room.withTransaction

class LocalTenantClearer(private val database: AppDatabase) {

    suspend fun clearAll() {
        database.withTransaction {
            database.animalDao().deleteAll()
            database.localCacheDao().clearAllCaches()
            database.syncDao().clearOutbox()
            database.syncDao().deleteMetadata("last_pull")
        }
    }
}
