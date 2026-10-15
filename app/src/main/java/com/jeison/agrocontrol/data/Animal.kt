package com.jeison.agrocontrol.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "animales",
    indices = [Index(value = ["codigo"], unique = true)],
)
data class Animal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val codigo: String,
    val nombre: String,
    val tipo: String,
    val raza: String,
    val sexo: String,
    val peso: Double,
    val fechaNacimiento: String,
    val potrero: String,
    val observacion: String = "",
    val serverId: String? = null,
    val syncState: String = SyncState.PENDING.name,
    val updatedAtEpoch: Long = System.currentTimeMillis(),
) {
    fun tituloDisplay(): String {
        val emoji = if (tipo.equals("Toro", ignoreCase = true)) "🐂" else "🐄"
        val titulo = nombre.ifBlank { "$tipo $codigo" }
        return "$emoji $titulo"
    }

    fun detalleLineas(): String =
        "Código: $codigo\nRaza: $raza\nPeso: $peso kg\nPotrero: $potrero"

    enum class SyncState { PENDING, SYNCED }

    companion object {
        const val EXTRA_CODIGO = "codigo"
        const val EXTRA_NOMBRE = "nombre"
        const val EXTRA_TIPO = "tipo"
        const val EXTRA_RAZA = "raza"
        const val EXTRA_SEXO = "sexo"
        const val EXTRA_PESO = "peso"
        const val EXTRA_FECHA = "fecha"
        const val EXTRA_POTRERO = "potrero"
        const val EXTRA_OBSERVACION = "observacion"
    }
}
