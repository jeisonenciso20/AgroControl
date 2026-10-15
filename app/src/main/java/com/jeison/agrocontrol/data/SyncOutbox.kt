package com.jeison.agrocontrol.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_outbox")
data class SyncOutbox(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entity: String,
    val operation: String,
    val localId: String,
    val serverId: String? = null,
    val payloadJson: String,
    val createdAtEpoch: Long = System.currentTimeMillis(),
    val sent: Boolean = false,
)
