package com.autosen.app.vista

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autosen.app.data.*
import kotlinx.coroutines.launch

@Composable
fun MonitoreoScreen() {
    var sensores by remember { mutableStateOf<List<Sensor>>(emptyList()) }
    var vehiculos by remember { mutableStateOf<List<Vehiculo>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    var sensorSeleccionado by remember { mutableStateOf<Sensor?>(null) }
    var vehiculoSeleccionado by remember { mutableStateOf<Vehiculo?>(null) }
    var nivel by remember { mutableStateOf(50) }
    var enviandoLectura by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun cargarDatos() {
        cargando = true
        scope.launch {
            try {
                val resSensores = RetrofitClient.apiService.getSensores()
                if (resSensores.isSuccessful) {
                    sensores = resSensores.body() ?: emptyList()
                    if (sensorSeleccionado == null) sensorSeleccionado = sensores.firstOrNull()
                }
                val resVehiculos = RetrofitClient.apiService.getVehiculos()
                if (resVehiculos.isSuccessful) {
                    vehiculos = resVehiculos.body() ?: emptyList()
                    if (vehiculoSeleccionado == null) vehiculoSeleccionado = vehiculos.firstOrNull()
                }
            } catch (e: Exception) {
                mensaje = "Error cargando datos: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) { cargarDatos() }

    fun enviarLectura() {
        val sensor = sensorSeleccionado
        val vehiculo = vehiculoSeleccionado
        if (sensor == null || vehiculo == null) {
            mensaje = "Selecciona un sensor y un vehículo"
            return
        }
        enviandoLectura = true
        scope.launch {
            try {
                val response = RetrofitClient.apiService.createLectura(
                    LecturaRequest(sensor.id, vehiculo.idVehiculo, nivel)
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    mensaje = if (body?.alerta != null) {
                        "Alerta generada (${body.alerta.tipo}) en ${sensor.nombre}"
                    } else {
                        "Lectura registrada en ${sensor.nombre} → $nivel%"
                    }
                    cargarDatos() // refresca el nivel del sensor desde el servidor
                } else {
                    val msg = mensajeDeErrorApi(response)
                    mensaje = if (msg.isNotBlank()) "Error del servidor: $msg"
                    else "Error al registrar lectura (${response.code()})"
                }
            } catch (e: Exception) {
                mensaje = "Error de conexión: ${e.message}"
            } finally {
                enviandoLectura = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Panel de sensores", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "${sensores.size} sensores · ${vehiculos.size} vehículos",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = { cargarDatos() }) {
                Text("🔄 Actualizar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Enviar lectura OBD2", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(12.dp))

                SelectorSimple(
                    label = "sensor",
                    opciones = sensores,
                    seleccionado = sensorSeleccionado,
                    textoDe = { s -> s.nombre },
                    onSeleccion = { sensorSeleccionado = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                SelectorSimple(
                    label = "vehículo",
                    opciones = vehiculos,
                    seleccionado = vehiculoSeleccionado,
                    textoDe = { v -> "${v.nombreVehiculo} (${v.placa})" },
                    onSeleccion = { vehiculoSeleccionado = it }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Nivel", modifier = Modifier.weight(1f))
                    Text("$nivel%", fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = nivel.toFloat(),
                    onValueChange = { nivel = it.toInt() },
                    valueRange = 0f..100f
                )

                Button(
                    onClick = { enviarLectura() },
                    enabled = sensores.isNotEmpty() && vehiculos.isNotEmpty() && !enviandoLectura,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (enviandoLectura) "Enviando..." else "Enviar lectura al servidor")
                }
            }
        }

        if (mensaje != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mensaje ?: "",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Sensores", fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))

        if (cargando && sensores.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sensores) { sensor ->
                    SensorCard(sensor)
                }
            }
        }
    }
}