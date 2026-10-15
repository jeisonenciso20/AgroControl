package com.jeison.agrocontrol.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): TokenResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): TokenResponse

    @GET("auth/me")
    suspend fun me(): UserMeResponse

    @GET("dashboard/summary")
    suspend fun dashboardSummary(): DashboardSummary

    @POST("sync/push")
    suspend fun syncPush(@Body body: SyncPushRequest): SyncPushResponse

    @GET("sync/pull")
    suspend fun syncPull(@Query("since") since: String?): SyncPullResponse

    @GET("reports/weekly")
    suspend fun reportWeekly(@Query("from") from: String, @Query("to") to: String): ReportResponse

    @GET("reports/monthly")
    suspend fun reportMonthly(@Query("year") year: Int, @Query("month") month: Int): ReportResponse

    @GET("tasks")
    suspend fun tasks(): List<PendingTaskFullDto>

    @GET("reproduction/summary")
    suspend fun reproductionSummary(): ReproductionSummaryDto
}
