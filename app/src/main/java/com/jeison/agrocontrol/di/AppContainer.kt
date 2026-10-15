package com.jeison.agrocontrol.di

import com.jeison.agrocontrol.AgroControlApp
import com.jeison.agrocontrol.auth.AuthSession
import com.jeison.agrocontrol.data.AppDatabase
import com.jeison.agrocontrol.network.ApiService
import com.jeison.agrocontrol.network.RetrofitClient
import com.jeison.agrocontrol.ui.common.ConnectivityObserver
import com.jeison.agrocontrol.ui.common.SyncStatusRepository

class AppContainer(private val app: AgroControlApp) {
    val applicationContext get() = app.applicationContext
    val database: AppDatabase get() = AppDatabase.get(app)
    val tokenStore get() = app.tokenStore
    val syncManager get() = app.syncManager
    val syncScheduler get() = app.syncScheduler
    val animalRepository get() = app.animalRepository
    val potreroRepository get() = app.potreroRepository
    val productionRepository get() = app.productionRepository
    val reproductionRepository get() = app.reproductionRepository
    val pendingTaskRepository get() = app.pendingTaskRepository
    val localTenantClearer get() = app.localTenantClearer
    val authSession by lazy { AuthSession(this) }
    val localCacheDao get() = database.localCacheDao()
    val syncDao get() = database.syncDao()
    val animalDao get() = database.animalDao()
    val api: ApiService by lazy { RetrofitClient.createApi(app.tokenStore) }
    val connectivity by lazy { ConnectivityObserver(app) }
    val syncStatus by lazy { SyncStatusRepository(syncDao, syncManager) }
}

