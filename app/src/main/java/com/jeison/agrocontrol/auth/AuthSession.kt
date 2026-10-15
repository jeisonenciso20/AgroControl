package com.jeison.agrocontrol.auth

import com.jeison.agrocontrol.di.AppContainer
import com.jeison.agrocontrol.network.TokenResponse

class AuthSession(private val container: AppContainer) {

    suspend fun completeAuth(tokens: TokenResponse, isRegister: Boolean) {
        container.tokenStore.saveTokens(tokens.accessToken, tokens.refreshToken)
        val fincaId = container.api.me().finca.id
        val previousFinca = container.tokenStore.getActiveFincaId()
        val hadStaleLocal = container.animalRepository.count() > 0 ||
            container.localCacheDao.allPotreros().isNotEmpty()
        val switchedFinca = previousFinca != null && previousFinca != fincaId
        val orphanCache = previousFinca == null && hadStaleLocal
        if (isRegister || switchedFinca || orphanCache) {
            container.localTenantClearer.clearAll()
        }
        container.tokenStore.saveActiveFincaId(fincaId)
        container.syncStatus.syncNow()
    }

    suspend fun logout() {
        container.localTenantClearer.clearAll()
        container.tokenStore.clear()
    }
}
