package com.jeison.agrocontrol.ui.common

import com.jeison.agrocontrol.data.SyncDao
import com.jeison.agrocontrol.sync.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class SyncBannerState(
    val isOnline: Boolean = true,
    val pendingCount: Int = 0,
    val isSyncing: Boolean = false,
    val lastError: String? = null,
    val lastPullIso: String? = null,
)

class SyncStatusRepository(
    private val syncDao: SyncDao,
    private val syncManager: SyncManager,
) {
    private val mutex = Mutex()
    private val _state = MutableStateFlow(SyncBannerState())
    val bannerState: StateFlow<SyncBannerState> = _state.asStateFlow()

    fun setOnline(online: Boolean) {
        _state.update { it.copy(isOnline = online) }
    }

    suspend fun refreshPending() {
        val pending = syncDao.pendingOutbox().size
        val lastPull = syncDao.getMetadata("last_pull")?.value
        _state.update { it.copy(pendingCount = pending, lastPullIso = lastPull) }
    }

    suspend fun syncNow(): Result<Unit> = mutex.withLock {
        _state.update { it.copy(isSyncing = true, lastError = null) }
        val result = syncManager.syncAll()
        refreshPending()
        _state.update {
            it.copy(
                isSyncing = false,
                lastError = result.exceptionOrNull()?.message,
            )
        }
        result
    }
}
