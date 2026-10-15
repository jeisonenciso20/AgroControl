package com.jeison.agrocontrol.sync

import com.google.gson.Gson
import com.jeison.agrocontrol.auth.TokenStore
import com.jeison.agrocontrol.data.Animal
import com.jeison.agrocontrol.data.AppDatabase
import com.jeison.agrocontrol.data.LocalCacheDao
import com.jeison.agrocontrol.data.PendingTaskLocal
import com.jeison.agrocontrol.data.PendingTaskSync
import com.jeison.agrocontrol.data.SyncDao
import com.jeison.agrocontrol.data.PotreroLocal
import com.jeison.agrocontrol.data.ProductionLocal
import com.jeison.agrocontrol.data.ReproductionEventLocal
import com.jeison.agrocontrol.network.RetrofitClient
import com.jeison.agrocontrol.network.SyncChange
import com.jeison.agrocontrol.network.SyncPushRequest
import java.time.Instant

class SyncManager(
    private val database: AppDatabase,
    private val tokenStore: TokenStore,
    private val gson: Gson = Gson(),
) {
    private val api by lazy { RetrofitClient.createApi(tokenStore) }

    suspend fun syncAll(): Result<Unit> {
        if (!tokenStore.isLoggedIn()) return Result.success(Unit)
        val pushError = runCatching { pushOutbox() }.exceptionOrNull()
        val pullResult = runCatching { pullRemote() }
        return when {
            pullResult.isFailure -> Result.failure(pullResult.exceptionOrNull()!!)
            pushError != null -> Result.failure(pushError)
            else -> Result.success(Unit)
        }
    }

    private suspend fun pushOutbox() {
        val syncDao = database.syncDao()
        val animalDao = database.animalDao()
        val pending = syncDao.pendingOutbox()
        if (pending.isEmpty()) return
        val changes = pending.map { row ->
            SyncChange(
                entity = row.entity,
                operation = row.operation,
                localId = row.localId,
                serverId = row.serverId,
                payload = gson.fromJson(row.payloadJson, Map::class.java) as Map<String, Any?>,
                clientUpdatedAt = Instant.now().toString(),
            )
        }
        val response = api.syncPush(SyncPushRequest(changes))
        val cache = database.localCacheDao()
        response.applied.forEach { applied ->
            when (applied.entity) {
                "ANIMAL" -> animalDao.updateSyncState(
                    applied.localId.toLong(),
                    applied.serverId,
                    Animal.SyncState.SYNCED.name,
                )
                "POTRERO" -> remapPotrero(cache, applied.localId, applied.serverId)
                "PRODUCTION_RECORD" -> remapProduction(cache, applied.localId, applied.serverId)
                "PENDING_TASK" -> handleTaskApplied(cache, syncDao, applied.localId, applied.serverId)
                "REPRODUCTION_EVENT" -> remapReproduction(cache, applied.localId, applied.serverId)
            }
        }
        pending.forEach { syncDao.markSent(it.id) }
    }

    private suspend fun remapPotrero(cache: LocalCacheDao, localId: String, serverId: String) {
        val row = cache.allPotreros().find { it.serverId == localId } ?: return
        if (localId != serverId) cache.deletePotrero(localId)
        cache.upsertPotreros(listOf(row.copy(serverId = serverId, syncState = "SYNCED")))
    }

    private suspend fun remapProduction(cache: LocalCacheDao, localId: String, serverId: String) {
        val row = cache.allProduction().find { it.serverId == localId } ?: return
        if (localId != serverId) cache.deleteProduction(localId)
        cache.upsertProduction(listOf(row.copy(serverId = serverId, syncState = "SYNCED")))
    }

    private suspend fun handleTaskApplied(cache: LocalCacheDao, syncDao: SyncDao, localId: String, serverId: String) {
        val isCreateRemap = localId.startsWith("local-") && localId != serverId
        if (isCreateRemap) {
            remapTaskAfterCreate(cache, syncDao, localId, serverId)
        } else {
            val row = cache.taskById(serverId) ?: cache.taskById(localId) ?: return
            cache.upsertTasks(listOf(row.copy(serverId = serverId, syncState = "SYNCED")))
        }
    }

    private suspend fun remapTaskAfterCreate(cache: LocalCacheDao, syncDao: SyncDao, localId: String, serverId: String) {
        val row = cache.taskById(localId) ?: return
        if (localId != serverId) cache.deleteTask(localId)
        val completed = row.status.equals("COMPLETED", ignoreCase = true)
        cache.upsertTasks(
            listOf(
                row.copy(
                    serverId = serverId,
                    syncState = if (completed) "PENDING" else "SYNCED",
                ),
            ),
        )
        if (completed) {
            PendingTaskSync.enqueueComplete(syncDao, gson, serverId)
        }
    }

    private suspend fun remapReproduction(cache: LocalCacheDao, localId: String, serverId: String) {
        val row = cache.allReproduction().find { it.serverId == localId } ?: return
        if (localId != serverId) cache.deleteReproduction(localId)
        cache.upsertReproduction(listOf(row.copy(serverId = serverId, syncState = "SYNCED")))
    }

    private suspend fun pullRemote() {
        val syncDao = database.syncDao()
        val since = syncDao.getMetadata("last_pull")?.value
        val pull = api.syncPull(if (since.isNullOrBlank()) null else since)
        val cache = database.localCacheDao()
        val animalDao = database.animalDao()
        pull.animals.forEach { dto ->
            val existing = animalDao.findByCodigo(dto.codigo)
            val merged = Animal(
                id = existing?.id ?: 0,
                serverId = dto.id,
                codigo = dto.codigo,
                nombre = dto.nombre,
                tipo = dto.tipo,
                raza = dto.raza,
                sexo = dto.sexo,
                peso = dto.peso,
                fechaNacimiento = dto.fechaNacimiento.take(10),
                potrero = dto.potreroNombre,
                observacion = dto.observacion,
                syncState = Animal.SyncState.SYNCED.name,
                updatedAtEpoch = System.currentTimeMillis(),
            )
            if (existing == null) animalDao.insert(merged) else animalDao.update(merged)
        }
        cache.upsertPotreros(
            pull.potreros.map {
                PotreroLocal(
                    serverId = it.id,
                    nombre = it.nombre,
                    areaHectareas = it.areaHectareas,
                    estado = it.estado,
                    tipoPasto = it.tipoPasto.orEmpty(),
                    fechaUltimaSiembra = it.fechaUltimaSiembra?.take(10).orEmpty(),
                    fechaProximoUso = it.fechaProximoUso?.take(10).orEmpty(),
                    observacion = it.observacion.orEmpty(),
                    especiesJson = gson.toJson(it.especiesPermitidas),
                    syncState = "SYNCED",
                    updatedAtEpoch = System.currentTimeMillis(),
                )
            },
        )
        cache.upsertTasks(
            pull.pendingTasks.map {
                PendingTaskLocal(
                    serverId = it.id,
                    titulo = it.titulo,
                    tipo = it.tipo,
                    status = it.status,
                    vencimiento = it.vencimiento.take(10),
                    referencia = it.referencia,
                    workType = it.workType.orEmpty(),
                    observacion = it.observacion.orEmpty(),
                    syncState = "SYNCED",
                    updatedAtEpoch = System.currentTimeMillis(),
                )
            },
        )
        cache.upsertProduction(
            pull.productionRecords.map { rec ->
                val animal = pull.animals.find { a -> a.id == rec.animalId }
                ProductionLocal(
                    serverId = rec.id,
                    productType = rec.productType,
                    cantidad = rec.cantidad,
                    unidad = rec.unidad,
                    fecha = rec.fecha.take(10),
                    jornada = rec.jornada ?: "MANANA",
                    animalServerId = rec.animalId.orEmpty(),
                    animalCodigo = animal?.codigo.orEmpty(),
                    notas = rec.notas,
                    syncState = "SYNCED",
                    updatedAtEpoch = System.currentTimeMillis(),
                )
            },
        )
        cache.upsertReproduction(
            pull.reproductionEvents.map { ev ->
                ReproductionEventLocal(
                    serverId = ev.id,
                    animalServerId = ev.animalId,
                    animalCodigo = ev.animal?.codigo ?: "",
                    tipoEvento = ev.tipoEvento,
                    programada = ev.programada.take(10),
                    notas = ev.notas,
                    syncState = "SYNCED",
                    updatedAtEpoch = System.currentTimeMillis(),
                )
            },
        )
        syncDao.upsertMetadata(
            com.jeison.agrocontrol.data.SyncMetadata("last_pull", Instant.now().toString()),
        )
    }
}

