package com.jeison.agrocontrol.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reproduction_events")
data class ReproductionEventLocal(
    @PrimaryKey
    val serverId: String,
    val animalServerId: String,
    val animalCodigo: String,
    val tipoEvento: String,
    val programada: String,
    val notas: String,
    val syncState: String = "SYNCED",
    val updatedAtEpoch: Long,
)
