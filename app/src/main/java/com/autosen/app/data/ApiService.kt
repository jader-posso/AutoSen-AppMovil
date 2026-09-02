package com.autosen.app.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // ── AUTENTICACIÓN ─────────────────────────────────────
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @GET("api/me")
    suspend fun me(): Response<Cliente>

    @POST("api/logout")
    suspend fun logout(): Response<MensajeResponse>

    // ── VEHÍCULOS — CRUD completo ─────────────────────────
    @GET("api/vehiculos")
    suspend fun getVehiculos(): Response<List<Vehiculo>>

    @GET("api/vehiculos/{id}")
    suspend fun getVehiculo(@Path("id") id: Int): Response<Vehiculo>

    @POST("api/vehiculos")
    suspend fun createVehiculo(@Body request: VehiculoRequest): Response<Vehiculo>

    @PUT("api/vehiculos/{id}")
    suspend fun updateVehiculo(@Path("id") id: Int, @Body request: VehiculoRequest): Response<Vehiculo>

    @DELETE("api/vehiculos/{id}")
    suspend fun deleteVehiculo(@Path("id") id: Int): Response<MensajeResponse>

    // ── SENSORES — solo lectura ───────────────────────────
    @GET("api/sensores")
    suspend fun getSensores(): Response<List<Sensor>>

    @GET("api/sensores/{id}")
    suspend fun getSensor(@Path("id") id: Int): Response<Sensor>

    // ── LECTURAS — historial + crear ──────────────────────
    @GET("api/lecturas")
    suspend fun getLecturas(): Response<List<Lectura>>

    @POST("api/lecturas")
    suspend fun createLectura(@Body request: LecturaRequest): Response<LecturaResponse>

    // ── ALERTAS — listar + marcar leída ───────────────────
    @GET("api/alertas")
    suspend fun getAlertas(): Response<List<Alerta>>

    @POST("api/alertas/{id}/leer")
    suspend fun marcarAlertaLeida(@Path("id") id: Int): Response<MensajeResponse>
}