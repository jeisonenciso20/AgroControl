package com.jeison.agrocontrol.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_tasks")
data class PendingTaskLocal(
    @PrimaryKey
    val serverId: String,
    val titulo: String,
    val tipo: String,
    val status: String,
    val vencimiento: String,
    val referencia: String,
    val workType: String,
    val observacion: String,
    val syncState: String = "SYNCED",
    val updatedAtEpoch: Long,
)
