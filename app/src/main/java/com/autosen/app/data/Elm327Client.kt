package com.autosen.app.data

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * Cliente Bluetooth para comunicarse con el ELM327 V1.5 (OBD-II).
 * Detecta, conecta (RFCOMM), envía comandos OBD-II y recibe/interpreta
 * las respuestas reales de la ECU.
 */
class Elm327Client(private val device: BluetoothDevice) {

    private val UUID_SPP: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var socket: BluetoothSocket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null
    var conectado: Boolean = false
        private set

    /**
     * Abre la conexión RFCOMM con el ELM327 e inicializa el protocolo
     * (echo off para que las respuestas sean compactas).
     */
    fun conectar(): Boolean {
        return try {
            socket = device.createRfcommSocketToServiceRecord(UUID_SPP)
            socket?.connect()
            input = socket?.inputStream
            output = socket?.outputStream
            conectado = true
            enviarComando("ATZ\r")      // Reset
            Thread.sleep(1200)
            enviarComando("ATE0\r")     // Echo off
            Thread.sleep(500)
            enviarComando("ATL0\r")     // Linefeeds off
            Thread.sleep(300)
            enviarComando("ATS0\r")     // Spaces off
            Thread.sleep(300)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            cerrar()
            false
        }
    }

    /**
     * Envía un comando (PID OBD-II) y devuelve la respuesta cruda del ELM327.
     * Ejemplos: "010C" (RPM), "010D" (velocidad), "0105" (temperatura).
     */
    fun enviarComando(cmd: String): String {
        if (!conectado) return ""
        return try {
            output?.write(cmd.toByteArray())
            output?.flush()
            val buf = ByteArray(256)
            val bytes = input?.read(buf) ?: 0
            if (bytes <= 0) return ""
            String(buf, 0, bytes).trim()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Pide y parsea un PID real. Devuelve null si no hay respuesta válida.
     * @param pid ej. "010C", "010D", "0105"
     * @param parser convierte los bytes [A,B] en valor físico.
     */
    fun leerPid(pid: String, parser: (Int, Int) -> Double): Double? {
        val respuesta = enviarComando(pid + "\r")
        if (respuesta.isBlank()) return null
        // Respuesta típica: "41 0C 1F A0" -> A=0x1F, B=0xA0
        val tokens = respuesta.split(" ", "\r", "\n").filter { it.isNotBlank() }
// ── Lecturas OBD-II estándar (PIDs modo 01) ──────────────

    /** RPM del motor (PID 010C): (256*A+B)/4 */
    fun leerRPM(): Double? =
        leerPid("010C") { a, b -> (256 * a + b) / 4.0 }

    /** Velocidad del vehículo (PID 010D): B km/h */
    fun leerVelocidad(): Double? =
        leerPid("010D") { _, b -> b.toDouble() }

    /** Temperatura del refrigerante (PID 0105): B - 40 °C */
    fun leerTemperatura(): Double? =
        leerPid("0105") { _, b -> (b - 40).toDouble() }

    /** Flujo de masa de aire MAF (PID 0110): ((256*A)+B)/100 g/s */
    fun leerMAF(): Double? =
        leerPid("0110") { a, b -> ((256 * a + b) / 100.0) }

    /** Sonda de oxígeno (PID 0114): voltaje = B/200 V */
    fun leerO2(): Double? =
        leerPid("0114") { _, b -> b / 200.0 }

    /** Nivel de combustible (PID 012F): B*100/255 % */
    fun leerCombustible(): Double? =
        leerPid("012F") { _, b -> b * 100.0 / 255.0 }

    /** Carga del motor (PID 0104): (B/255)*100 % */
    fun leerCargaMotor(): Double? =
        leerPid("0104") { _, b -> b * 100.0 / 255.0 }

    fun cerrar() {
        try {
            conectado = false
            output?.close()
            input?.close()
            socket?.close()
        } catch (e: Exception) {
            // ignorar
        } finally {
            socket = null
            input = null
            output = null
        }
    }

    companion object {
        /** Comprueba si la app tiene permisos de Bluetooth (Android 12+) */
        fun tienePermisos(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                return ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            }
            return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }

        /** Lista los dispositivos Bluetooth emparejados (busca ELM327/OBD). */
        fun dispositivosEmparejados(context: Context): List<BluetoothDevice> {
            if (!tienePermisos(context)) return emptyList()
            val adapter = BluetoothAdapter.getDefaultAdapter() ?: return emptyList()
            return adapter.bondedDevices.toList().filter {
                it.name?.contains("OBD", true) == true ||
                it.name?.contains("ELM", true) == true ||
                it.name?.contains("OBDII", true) == true ||
                it.name?.contains("V-LINK", true) == true ||
                it.name?.contains("OBD2", true) == true ||
                it.name?.contains("Car", true) == true ||
                it.name?.contains("ELM327", true) == true ||
                it.name?.contains("BT", true) == true ||
                it.name?.contains("HC-05", true) == true ||
                it.name?.contains("OBD-II", true) == true
            }
        }
    }
}
        if (tokens.size < 4) return null
        val a = tokens[tokens.size - 2].toIntOrNull(16) ?: return null
        val b = tokens[tokens.size - 1].toIntOrNull(16) ?: return null
        return parser(a, b)
    }