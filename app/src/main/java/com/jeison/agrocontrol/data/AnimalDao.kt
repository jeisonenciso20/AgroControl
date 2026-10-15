package com.jeison.agrocontrol.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AnimalDao {

    @Query("SELECT * FROM animales ORDER BY codigo ASC")
    suspend fun getAll(): List<Animal>

    @Query(
        """
        SELECT * FROM animales
        WHERE codigo LIKE '%' || :query || '%'
           OR nombre LIKE '%' || :query || '%'
           OR tipo LIKE '%' || :query || '%'
        ORDER BY codigo ASC
        """
    )
    suspend fun search(query: String): List<Animal>

    @Query("SELECT COUNT(*) FROM animales")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(animal: Animal): Long

    @Query("UPDATE animales SET serverId = :serverId, syncState = :syncState WHERE id = :localId")
    suspend fun updateSyncState(localId: Long, serverId: String?, syncState: String)

    @Query("SELECT * FROM animales WHERE codigo = :codigo LIMIT 1")
    suspend fun findByCodigo(codigo: String): Animal?

    @Update
    suspend fun update(animal: Animal)

    @Query("DELETE FROM animales")
    suspend fun deleteAll()
}
