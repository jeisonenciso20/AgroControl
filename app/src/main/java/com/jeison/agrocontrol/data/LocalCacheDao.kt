package com.jeison.agrocontrol.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocalCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPotreros(items: List<PotreroLocal>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTasks(items: List<PendingTaskLocal>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProduction(items: List<ProductionLocal>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReproduction(items: List<ReproductionEventLocal>)

    @Query("SELECT * FROM reproduction_events ORDER BY programada DESC")
    suspend fun allReproduction(): List<ReproductionEventLocal>

    @Query("SELECT * FROM potreros ORDER BY nombre")
    suspend fun allPotreros(): List<PotreroLocal>

    @Query("SELECT * FROM pending_tasks WHERE status = 'PENDING' ORDER BY vencimiento")
    suspend fun pendingTasks(): List<PendingTaskLocal>

    @Query("SELECT * FROM pending_tasks WHERE serverId = :serverId LIMIT 1")
    suspend fun taskById(serverId: String): PendingTaskLocal?

    @Query("SELECT * FROM production_records ORDER BY fecha DESC")
    suspend fun allProduction(): List<ProductionLocal>

    @Query("DELETE FROM potreros WHERE serverId = :serverId")
    suspend fun deletePotrero(serverId: String)

    @Query("DELETE FROM pending_tasks WHERE serverId = :serverId")
    suspend fun deleteTask(serverId: String)

    @Query("DELETE FROM production_records WHERE serverId = :serverId")
    suspend fun deleteProduction(serverId: String)

    @Query("DELETE FROM reproduction_events WHERE serverId = :serverId")
    suspend fun deleteReproduction(serverId: String)

    @Query("DELETE FROM potreros")
    suspend fun deleteAllPotreros()

    @Query("DELETE FROM pending_tasks")
    suspend fun deleteAllTasks()

    @Query("DELETE FROM production_records")
    suspend fun deleteAllProduction()

    @Query("DELETE FROM reproduction_events")
    suspend fun deleteAllReproduction()

    suspend fun clearAllCaches() {
        deleteAllPotreros()
        deleteAllTasks()
        deleteAllProduction()
        deleteAllReproduction()
    }
}
