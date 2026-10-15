package com.jeison.agrocontrol.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "potreros")
data class PotreroLocal(
    @PrimaryKey
    val serverId: String,
    val nombre: String,
    val areaHectareas: Double?,
    val estado: String,
    val tipoPasto: String,
    val fechaUltimaSiembra: String,
    val fechaProximoUso: String,
    val observacion: String,
    val especiesJson: String,
    val syncState: String = "SYNCED",
    val updatedAtEpoch: Long,
)
