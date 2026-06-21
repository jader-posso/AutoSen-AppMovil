package com.autosen.app.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/vehiculos")
    suspend fun getVehiculos(@Header("Authorization") token: String): Response<List<Vehiculo>>

    @GET("api/sensores")
    suspend fun getSensores(@Header("Authorization") token: String): Response<List<Sensor>>

    @POST("api/lecturas")
    suspend fun postLectura(
        @Header("Authorization") token: String,
        @Body request: LecturaRequest
    ): Response<LecturaResponse>

    @GET("api/lecturas")
    suspend fun getLecturas(@Header("Authorization") token: String): Response<List<Lectura>>

    @GET("api/alertas")
    suspend fun getAlertas(@Header("Authorization") token: String): Response<List<Alerta>>
}
