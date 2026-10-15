package com.jeison.agrocontrol.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface SyncDao {
    @Insert
    suspend fun enqueue(item: SyncOutbox): Long

    @Query("SELECT * FROM sync_outbox WHERE sent = 0 ORDER BY id ASC")
    suspend fun pendingOutbox(): List<SyncOutbox>

    @Query("UPDATE sync_outbox SET sent = 1 WHERE id = :id")
    suspend fun markSent(id: Long)

    @Upsert
    suspend fun upsertMetadata(meta: SyncMetadata)

    @Query("SELECT * FROM sync_metadata WHERE key = :key LIMIT 1")
    suspend fun getMetadata(key: String): SyncMetadata?

    @Query("DELETE FROM sync_outbox")
    suspend fun clearOutbox()

    @Query("DELETE FROM sync_metadata WHERE key = :key")
    suspend fun deleteMetadata(key: String)
}
