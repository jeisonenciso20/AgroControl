package com.jeison.agrocontrol.data

import com.google.gson.Gson
import com.jeison.agrocontrol.sync.SyncScheduler
import java.util.UUID

class PotreroRepository(
    private val cache: LocalCacheDao,
    private val syncDao: SyncDao,
    private val gson: Gson = Gson(),
    private val syncScheduler: SyncScheduler? = null,
) {
    suspend fun insert(p: PotreroLocal): String {
        val localId = p.serverId.ifBlank { "local-${UUID.randomUUID()}" }
        val row = p.copy(serverId = localId, syncState = "PENDING", updatedAtEpoch = System.currentTimeMillis())
        cache.upsertPotreros(listOf(row))
        val payload = mapOf(
            "nombre" to row.nombre,
            "areaHectareas" to row.areaHectareas,
            "estado" to row.estado,
            "tipoPasto" to row.tipoPasto,
            "fechaUltimaSiembra" to row.fechaUltimaSiembra.takeIf { it.isNotBlank() },
            "fechaProximoUso" to row.fechaProximoUso.takeIf { it.isNotBlank() },
            "observacion" to row.observacion,
            "especiesPermitidas" to gson.fromJson(row.especiesJson, Array<String>::class.java)?.toList().orEmpty(),
        )
        syncDao.enqueue(
            SyncOutbox(entity = "POTRERO", operation = "CREATE", localId = localId, payloadJson = gson.toJson(payload)),
        )
        syncScheduler?.schedule()
        return localId
    }

    suspend fun update(p: PotreroLocal) {
        val row = p.copy(syncState = "PENDING", updatedAtEpoch = System.currentTimeMillis())
        cache.upsertPotreros(listOf(row))
        val payload = mapOf(
            "nombre" to row.nombre,
            "areaHectareas" to row.areaHectareas,
            "estado" to row.estado,
            "tipoPasto" to row.tipoPasto,
            "fechaUltimaSiembra" to row.fechaUltimaSiembra.takeIf { it.isNotBlank() },
            "fechaProximoUso" to row.fechaProximoUso.takeIf { it.isNotBlank() },
            "observacion" to row.observacion,
            "especiesPermitidas" to gson.fromJson(row.especiesJson, Array<String>::class.java)?.toList().orEmpty(),
        )
        syncDao.enqueue(
            SyncOutbox(
                entity = "POTRERO",
                operation = if (row.serverId.startsWith("local-")) "CREATE" else "UPDATE",
                localId = row.serverId,
                serverId = row.serverId.takeUnless { it.startsWith("local-") },
                payloadJson = gson.toJson(payload),
            ),
        )
        syncScheduler?.schedule()
    }
}

class ProductionRepository(
    private val cache: LocalCacheDao,
    private val syncDao: SyncDao,
    private val gson: Gson = Gson(),
    private val syncScheduler: SyncScheduler? = null,
) {
    suspend fun insert(p: ProductionLocal): String {
        val localId = "local-${UUID.randomUUID()}"
        val row = p.copy(serverId = localId, syncState = "PENDING", updatedAtEpoch = System.currentTimeMillis())
        cache.upsertProduction(listOf(row))
        val payload = mapOf(
            "productType" to row.productType,
            "cantidad" to row.cantidad,
            "unidad" to row.unidad,
            "fecha" to row.fecha,
            "jornada" to row.jornada,
            "animalId" to row.animalServerId.takeIf { it.isNotBlank() },
            "notas" to row.notas,
        )
        syncDao.enqueue(
            SyncOutbox(entity = "PRODUCTION_RECORD", operation = "CREATE", localId = localId, payloadJson = gson.toJson(payload)),
        )
        syncScheduler?.schedule()
        return localId
    }
}

class ReproductionRepository(
    private val cache: LocalCacheDao,
    private val syncDao: SyncDao,
    private val gson: Gson = Gson(),
    private val syncScheduler: SyncScheduler? = null,
) {
    suspend fun insert(e: ReproductionEventLocal): String {
        val localId = "local-${UUID.randomUUID()}"
        val row = e.copy(serverId = localId, syncState = "PENDING", updatedAtEpoch = System.currentTimeMillis())
        cache.upsertReproduction(listOf(row))
        val payload = mapOf(
            "animalId" to row.animalServerId,
            "tipoEvento" to row.tipoEvento,
            "programada" to row.programada,
            "notas" to row.notas,
        )
        syncDao.enqueue(
            SyncOutbox(entity = "REPRODUCTION_EVENT", operation = "CREATE", localId = localId, payloadJson = gson.toJson(payload)),
        )
        syncScheduler?.schedule()
        return localId
    }
}

class PendingTaskRepository(
    private val cache: LocalCacheDao,
    private val syncDao: SyncDao,
    private val gson: Gson = Gson(),
    private val syncScheduler: SyncScheduler? = null,
) {
    suspend fun insert(t: PendingTaskLocal): String {
        val localId = "local-${UUID.randomUUID()}"
        val row = t.copy(serverId = localId, syncState = "PENDING", updatedAtEpoch = System.currentTimeMillis())
        cache.upsertTasks(listOf(row))
        val payload = mapOf(
            "titulo" to row.titulo,
            "tipo" to row.tipo,
            "vencimiento" to row.vencimiento,
            "referencia" to row.referencia,
            "workType" to row.workType,
            "observacion" to row.observacion,
        )
        syncDao.enqueue(
            SyncOutbox(entity = "PENDING_TASK", operation = "CREATE", localId = localId, payloadJson = gson.toJson(payload)),
        )
        syncScheduler?.schedule()
        return localId
    }

    suspend fun completeTask(taskId: String) {
        val row = cache.taskById(taskId) ?: return
        if (row.status.equals("COMPLETED", ignoreCase = true)) return
        val updated = row.copy(
            status = "COMPLETED",
            syncState = "PENDING",
            updatedAtEpoch = System.currentTimeMillis(),
        )
        cache.upsertTasks(listOf(updated))
        if (!taskId.startsWith("local-")) {
            PendingTaskSync.enqueueComplete(syncDao, gson, taskId)
        }
        syncScheduler?.schedule()
    }
}

object PendingTaskSync {
    suspend fun enqueueComplete(syncDao: SyncDao, gson: Gson, taskId: String) {
        syncDao.enqueue(
            SyncOutbox(
                entity = "PENDING_TASK",
                operation = "UPDATE",
                localId = taskId,
                serverId = taskId,
                payloadJson = gson.toJson(mapOf("status" to "COMPLETED")),
            ),
        )
    }
}
