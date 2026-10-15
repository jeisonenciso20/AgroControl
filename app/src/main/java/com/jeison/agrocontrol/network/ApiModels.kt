package com.jeison.agrocontrol.network

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(
    val email: String,
    val password: String,
    val nombre: String,
    val fincaNombre: String,
)
data class TokenResponse(val accessToken: String, val refreshToken: String, val tokenType: String)
data class UserMeResponse(
    val id: String,
    val email: String,
    val nombre: String,
    val finca: FincaSummaryDto,
)
data class FincaSummaryDto(val id: String, val nombre: String)
data class RefreshRequest(val refreshToken: String)
data class DashboardSummary(
    val animalesRegistrados: Int,
    val potrerosRegistrados: Int,
    val eventosReproduccion: Int,
    val litrosLecheSemana: Double,
    val pendientesRecientes: List<PendingTaskDto>,
)
data class PendingTaskDto(val id: String, val titulo: String, val vencimiento: String, val tipo: String)
data class SyncPushRequest(val changes: List<SyncChange>)
data class SyncChange(
    val entity: String,
    val operation: String,
    val localId: String,
    val serverId: String? = null,
    val payload: Map<String, Any?>,
    val clientUpdatedAt: String,
)
data class SyncPushResponse(val applied: List<SyncApplied>)
data class SyncApplied(val localId: String, val serverId: String, val entity: String)
data class SyncPullResponse(
    val since: String,
    val animals: List<AnimalDto>,
    val productionRecords: List<ProductionDto>,
    val pendingTasks: List<PendingTaskFullDto>,
    val potreros: List<PotreroDto>,
    val reproductionEvents: List<ReproductionEventDto> = emptyList(),
)
data class AnimalDto(
    val id: String,
    val codigo: String,
    val nombre: String,
    val tipo: String,
    val raza: String,
    val sexo: String,
    val peso: Double,
    val fechaNacimiento: String,
    val potreroNombre: String,
    val observacion: String,
    val updatedAt: String,
)
data class PotreroDto(
    val id: String,
    val nombre: String,
    val areaHectareas: Double?,
    val estado: String,
    val tipoPasto: String? = null,
    val fechaUltimaSiembra: String? = null,
    val fechaProximoUso: String? = null,
    val observacion: String? = null,
    val especiesPermitidas: List<String>,
    val updatedAt: String,
)
data class ProductionDto(
    val id: String,
    val productType: String,
    val cantidad: Double,
    val unidad: String,
    val fecha: String,
    val jornada: String? = null,
    val animalId: String? = null,
    val notas: String,
    val updatedAt: String,
)
data class PendingTaskFullDto(
    val id: String,
    val titulo: String,
    val tipo: String,
    val status: String,
    val vencimiento: String,
    val referencia: String,
    val workType: String? = null,
    val observacion: String? = null,
    val updatedAt: String,
)
data class ReproductionEventDto(
    val id: String,
    val animalId: String,
    val tipoEvento: String,
    val programada: String,
    val notas: String,
    val updatedAt: String,
    val animal: AnimalDto? = null,
)
data class ReproductionSummaryDto(
    val enCelo: Int,
    val inseminadas: Int,
    val gestantes: Int,
    val partosMes: Int,
)
data class ReportResponse(val period: String, val totalsByProductType: Map<String, Double>, val recordCount: Int)

