package com.autosen.app.data


import com.google.gson.annotations.SerializedName

// ── AUTH ──────────────────────────────────────────────────

data class LoginRequest(
    val correo: String,
    val contrasena: String
)

data class LoginResponse(
    val token: String,
    val cliente: Cliente
)

data class Cliente(
    val id: Int,
    val nombre: String,
    val apellido: String,
    val correo: String
)

// ── VEHICULOS ─────────────────────────────────────────────

data class Vehiculo(
    @SerializedName("Id_vehiculo") val idVehiculo: Int,
    @SerializedName("Nombre_vehiculo") val nombreVehiculo: String,
    @SerializedName("Marca") val marca: String,
    @SerializedName("Modelo") val modelo: String,
    @SerializedName("Placa") val placa: String
)

// ── SENSORES ──────────────────────────────────────────────

data class Sensor(
    val id: Int,
    val nombre: String,
    val tipo: String,
    @SerializedName("tipo_daño") val tipoDanio: String,
    val nivel: Int,
    val estado: String
)

// ── LECTURAS ──────────────────────────────────────────────

data class LecturaRequest(
    @SerializedName("Id_sensor") val idSensor: Int,
    @SerializedName("Id_vehiculo") val idVehiculo: Int,
    @SerializedName("Nivel") val nivel: Int
)

data class LecturaResponse(
    val lectura: LecturaData,
    val alerta: AlertaData?,
    val estado: String
)

data class LecturaData(
    @SerializedName("Id_lectura") val idLectura: Int,
    @SerializedName("Id_sensor") val idSensor: Int,
    @SerializedName("Id_vehiculo") val idVehiculo: Int,
    @SerializedName("Nivel") val nivel: Int,
    @SerializedName("Estado") val estado: String
)

data class Lectura(
    val id: Int,
    val sensor: String,
    @SerializedName("tipo_sensor") val tipoSensor: String,
    val vehiculo: String,
    val nivel: Int,
    val estado: String,
    val fecha: String
)

// ── ALERTAS ───────────────────────────────────────────────

data class AlertaData(
    @SerializedName("Id_alerta") val idAlerta: Int,
    @SerializedName("Id_sensor") val idSensor: Int,
    @SerializedName("Id_vehiculo") val idVehiculo: Int,
    @SerializedName("Tipo") val tipo: String,
    @SerializedName("Mensaje") val mensaje: String
)

data class Alerta(
    val id: Int,
    val sensor: String,
    val vehiculo: String,
    val tipo: String,
    val mensaje: String,
    val fecha: String
)