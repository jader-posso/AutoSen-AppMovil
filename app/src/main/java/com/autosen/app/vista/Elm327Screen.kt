package com.autosen.app.vista

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autosen.app.data.*
import com.autosen.app.ui.theme.Oliva
import com.autosen.app.ui.theme.Dorado
import com.autosen.app.ui.theme.Rojo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Estados de la pantalla de conexión
private enum class EstadoElm {
    SIN_CONEXION, CONECTANDO, CONECTADO
}

/**
 * Pantalla de conexión Bluetooth al ELM327 V1.5:
 *  - Detecta el ELM327 (dispositivos emparejados).
 *  - Se conecta por RFCOMM.
 *  - Envía comandos OBD-II y recibe datos reales de la ECU.
 *  - Guía los datos a través de la API al backend Spring Boot (MySQL).
 */
@Composable
fun Elm327Screen(
    idVehiculo: Long,
    onVolver: () -> Unit,
    onLecturasEnviadas: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var estado by remember { mutableStateOf(EstadoElm.SIN_CONEXION) }
    var dispositivoActual by remember { mutableStateOf<String?>(null) }

    // Datos en vivo (valores reales de la ECU)
    var rpm by remember { mutableStateOf<Double?>(null) }
    var velocidad by remember { mutableStateOf<Double?>(null) }
    var temperatura by remember { mutableStateOf<Double?>(null) }
    var maf by remember { mutableStateOf<Double?>(null) }
    var o2 by remember { mutableStateOf<Double?>(null) }
    var combustible by remember { mutableStateOf<Double?>(null) }
    var carga by remember { mutableStateOf<Double?>(null) }

    var mensaje by remember { mutableStateOf<String?>(null) }
    var enviadas by remember { mutableStateOf(0) }

    // Permisos Bluetooth (Android 12+ requiere BLUETOOTH_CONNECT)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    fun pedirPermisos() {
        val permisos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        launcher.launch(permisos)
    }

    LaunchedEffect(Unit) {
        pedirPermisos()
        // Si el vehículo ya tiene un ELM327 registrado, avisamos para reconexión automática
        val elmGuardado = RetrofitClient.elmMacVehiculo
        if (elmGuardado != null) {
            mensaje = "ELM327 guardado: $elmGuardado. Reconéctate cuando quieras."
        }
    }

    // Desconexión al salir de la pantalla
// Conecta al ELM327 (busca en emparejados si no se le indicó uno)
    fun conectarAlElm327() {
        if (!Elm327Client.tienePermisos(context)) {
            pedirPermisos()
            mensaje = "Necesitas permisos de Bluetooth para conectar el ELM327."
            return
        }
        estado = EstadoElm.CONECTANDO
        mensaje = "Buscando ELM327 V1.5..."
        scope.launch(Dispatchers.IO) {
            val dispositivo: BluetoothDevice? = try {
                val emparejados = Elm327Client.dispositivosEmparejados(context)
                emparejados.firstOrNull()
            } catch (e: Exception) { null }

            if (dispositivo == null) {
                withContext(Dispatchers.Main) {
                    estado = EstadoElm.SIN_CONEXION
                    mensaje = "No se encontró el ELM327 emparejado. Ve a Ajustes → Bluetooth y empareja el ELM327."
                }
                return@launch
            }

            val cliente = Elm327Client(dispositivo)
            val ok = cliente.conectar()
            withContext(Dispatchers.Main) {
                if (ok) {
                    RetrofitClient.elm327 = cliente
                    estado = EstadoElm.CONECTADO
                    val mac = dispositivo.address ?: ""
                    RetrofitClient.elmMacVehiculo = mac
                    dispositivoActual = "${dispositivo.name ?: "ELM327"} ($mac)"
                    mensaje = "Conectado al ${dispositivo.name ?: "ELM327"} ✓ — leyendo ECU en vivo"
                    // Guardar la MAC para futuras reconexiones
                    scope.launch {
                        try {
                            RetrofitClient.apiService.guardarElm(idVehiculo, mac)
                        } catch (_: Exception) {}
                    }
                } else {
                    estado = EstadoElm.SIN_CONEXION
                    mensaje = "No se pudo conectar al dispositivo. Verifica el ELM327 y que esté encendido."
                }
            }
        }
    }

    // Lee todos los PIDs en un bucle y los muestra + envía al backend
    fun iniciarLecturaContinua() {
        scope.launch(Dispatchers.IO) {
            while (RetrofitClient.elm327?.conectado == true) {
                val elm = RetrofitClient.elm327 ?: break
                val lecturas = listOf(
                    Triple("RPM del motor", "RPM", elm.leerRPM()),
                    Triple("Velocidad", "VELOCIDAD", elm.leerVelocidad()),
                    Triple("Temperatura refrigerante", "TEMP", elm.leerTemperatura()),
                    Triple("Flujo de masa de aire (MAF)", "MAF", elm.leerMAF()),
                    Triple("Sonda de oxígeno (O2)", "O2", elm.leerO2()),
                    Triple("Nivel de combustible", "FUEL", elm.leerCombustible()),
                    Triple("Carga del motor", "CARGA", elm.leerCargaMotor())
                )

                val valores = lecturas.mapNotNull { it.third }
                if (valores.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        rpm = lecturas[0].third
                        velocidad = lecturas[1].third
                        temperatura = lecturas[2].third
                        maf = lecturas[3].third
                        o2 = lecturas[4].third
                        combustible = lecturas[5].third
                        carga = lecturas[6].third
                    }
                    // Enviar cada lectura real al backend (API REST) que guarda en MySQL
                    var count = 0
                    for ((nombre, tipo, valor) in lecturas) {
                        if (valor != null) {
                            try {
                                val unidad = when (tipo) {
                                    "RPM" -> "rpm"
                                    "VELOCIDAD" -> "km/h"
                                    "TEMP" -> "°C"
                                    "MAF" -> "g/s"
                                    "O2" -> "V"
                                    "FUEL" -> "%"
                                    "CARGA" -> "%"
                                    else -> ""
                                }
                                val resp = RetrofitClient.apiService.guardarLecturaObd(
                                    idVehiculo = idVehiculo,
                                    nombreSensor = nombre,
                                    tipoSensor = tipo,
                                    valor = valor,
                                    unidad = unidad
                                )
                                if (resp.isSuccessful && resp.body()?.ok == true) {
                                    count++
                                    val nivel = resp.body()?.nivel
                                    if (nivel != null) RetrofitClient.ultimoNivel = nivel
                                }
                            } catch (_: Exception) {}
                            delay(150)
                        }
                    }
                    if (count > 0) {
                        withContext(Dispatchers.Main) {
                            enviadas += count
                            onLecturasEnviadas(enviadas)
                        }
                    }
                }
                delay(2000) // intervalo entre ciclos completos
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            RetrofitClient.elm327?.cerrar()
            RetrofitClient.elm327 = null
        }
    }
Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Cabecera con botón volver
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onVolver) { Text("← Volver") }
            Spacer(modifier = Modifier.weight(1f))
            Text("ELM327 OBD2", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
        }

        if (dispositivoActual != null) {
            Text(
                dispositivoActual!!,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botón conectar / desconectar
        when (estado) {
            EstadoElm.SIN_CONEXION, EstadoElm.CONECTANDO -> {
                Button(
                    onClick = {
                        if (estado == EstadoElm.CONECTANDO) {
                            mensaje = "Conectando, espera..."
                        } else {
                            conectarAlElm327()
                        }
                    },
                    enabled = estado != EstadoElm.CONECTANDO,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10302E), contentColor = Oliva),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(
                        if (estado == EstadoElm.CONECTANDO) "🔵 Conectando..." else "🔵 CONECTAR ELM327",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            EstadoElm.CONECTADO -> {
                Button(
                    onClick = {
                        RetrofitClient.elm327?.cerrar()
                        RetrofitClient.elm327 = null
                        estado = EstadoElm.SIN_CONEXION
                        mensaje = "Desconectado del ELM327."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rojo, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("🔴 DESCONECTAR", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (mensaje != null) {
            Text(
                mensaje ?: "",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            "Enviadas al servidor: $enviadas",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Datos en vivo (valores reales de la ECU)
        Text("DATOS EN VIVO (ECU)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { DatoVivo("RPM del motor", "rpm", rpm, Oliva) }
            item { DatoVivo("Velocidad", "km/h", velocidad, Oliva) }
            item { DatoVivo("Temp. refrigerante", "°C", temperatura, colorDeValor(temperatura)) }
            item { DatoVivo("Flujo MAF", "g/s", maf, colorDeValor(maf)) }
            item { DatoVivo("Sonda O2", "V", o2, Oliva) }
            item { DatoVivo("Combustible", "%", combustible, colorDeValor(combustible)) }
            item { DatoVivo("Carga motor", "%", carga, colorDeValor(carga)) }
        }
    }
}

// Tarjeta de un dato en vivo: etiqueta, valor, unidad y barra de estado
@Composable
private fun DatoVivo(titulo: String, unidad: String, valor: Double?, color: Color) {
    Card(modifier = Modifier.fillMaxWidth(), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A38))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(titulo, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text(
                if (valor != null) "${String.format("%.1f", valor)} $unidad" else "—",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (valor != null) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Color según el valor (rojo si es muy alto)
private fun colorDeValor(valor: Double?): Color {
    if (valor == null) return Oliva
    val nivel = RetrofitClient.ultimoNivel ?: 0
    return when {
        nivel >= 70 -> Rojo
        nivel >= 40 -> Dorado
        else -> Oliva
    }
}