package com.jeison.agrocontrol.data

import com.google.gson.Gson
import com.jeison.agrocontrol.sync.SyncScheduler

class AnimalRepository(
    private val animalDao: AnimalDao,
    private val syncDao: SyncDao,
    private val gson: Gson = Gson(),
    private val syncScheduler: SyncScheduler? = null,
) {

    suspend fun getAll(): List<Animal> = animalDao.getAll()

    suspend fun search(query: String): List<Animal> = animalDao.search(query)

    suspend fun count(): Int = animalDao.count()

    suspend fun insert(animal: Animal): Long {
        val withMeta = animal.copy(
            syncState = Animal.SyncState.PENDING.name,
            updatedAtEpoch = System.currentTimeMillis(),
        )
        val id = animalDao.insert(withMeta)
        val payload = mapOf(
            "codigo" to withMeta.codigo,
            "nombre" to withMeta.nombre,
            "tipo" to withMeta.tipo,
            "raza" to withMeta.raza,
            "sexo" to withMeta.sexo,
            "peso" to withMeta.peso,
            "fechaNacimiento" to withMeta.fechaNacimiento,
            "potreroNombre" to withMeta.potrero,
            "observacion" to withMeta.observacion,
        )
        syncDao.enqueue(
            SyncOutbox(
                entity = "ANIMAL",
                operation = "CREATE",
                localId = id.toString(),
                payloadJson = gson.toJson(payload),
            ),
        )
        syncScheduler?.schedule()
        return id
    }

    suspend fun updateAnimal(codigo: String, data: Animal) {
        val existing = animalDao.findByCodigo(codigo)
            ?: throw IllegalArgumentException("Animal no encontrado")
        val updated = data.copy(
            id = existing.id,
            serverId = existing.serverId,
            codigo = existing.codigo,
            syncState = Animal.SyncState.PENDING.name,
            updatedAtEpoch = System.currentTimeMillis(),
        )
        animalDao.update(updated)
        val payload = mapOf(
            "nombre" to updated.nombre,
            "tipo" to updated.tipo,
            "raza" to updated.raza,
            "sexo" to updated.sexo,
            "peso" to updated.peso,
            "fechaNacimiento" to updated.fechaNacimiento,
            "potreroNombre" to updated.potrero,
            "observacion" to updated.observacion,
        )
        syncDao.enqueue(
            SyncOutbox(
                entity = "ANIMAL",
                operation = if (existing.serverId.isNullOrBlank()) "CREATE" else "UPDATE",
                localId = existing.id.toString(),
                serverId = existing.serverId,
                payloadJson = gson.toJson(payload),
            ),
        )
        syncScheduler?.schedule()
    }
}
