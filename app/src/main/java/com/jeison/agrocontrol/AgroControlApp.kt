package com.jeison.agrocontrol

import android.app.Application
import com.jeison.agrocontrol.auth.TokenStore
import com.jeison.agrocontrol.di.AppContainer
import com.jeison.agrocontrol.data.AnimalRepository
import com.jeison.agrocontrol.data.AppDatabase
import com.jeison.agrocontrol.data.LocalTenantClearer
import com.jeison.agrocontrol.data.PendingTaskRepository
import com.jeison.agrocontrol.data.PotreroRepository
import com.jeison.agrocontrol.data.ProductionRepository
import com.jeison.agrocontrol.data.ReproductionRepository
import com.jeison.agrocontrol.sync.SyncManager
import com.jeison.agrocontrol.sync.SyncScheduler

class AgroControlApp : Application() {

    private val database by lazy { AppDatabase.get(this) }

    val tokenStore: TokenStore by lazy { TokenStore(this) }

    val syncScheduler: SyncScheduler by lazy { SyncScheduler(this) }

    val syncManager: SyncManager by lazy {
        SyncManager(database, tokenStore)
    }

    val animalRepository: AnimalRepository by lazy {
        AnimalRepository(database.animalDao(), database.syncDao(), syncScheduler = syncScheduler)
    }

    val potreroRepository: PotreroRepository by lazy {
        PotreroRepository(database.localCacheDao(), database.syncDao(), syncScheduler = syncScheduler)
    }

    val productionRepository: ProductionRepository by lazy {
        ProductionRepository(database.localCacheDao(), database.syncDao(), syncScheduler = syncScheduler)
    }

    val reproductionRepository: ReproductionRepository by lazy {
        ReproductionRepository(database.localCacheDao(), database.syncDao(), syncScheduler = syncScheduler)
    }

    val pendingTaskRepository: PendingTaskRepository by lazy {
        PendingTaskRepository(database.localCacheDao(), database.syncDao(), syncScheduler = syncScheduler)
    }

    val localTenantClearer: LocalTenantClearer by lazy {
        LocalTenantClearer(database)
    }

    val container: AppContainer by lazy { AppContainer(this) }
}

