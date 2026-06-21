package com.autosen.app.vista

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autosen.app.data.LecturaRequest
import com.autosen.app.data.RetrofitClient
import com.autosen.app.data.Sensor
import com.autosen.app.data.Vehiculo
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun DashboardScreen(token: String, nombreCliente: String) {

    var sensores by remember { mutableStateOf<List<Sensor>>(emptyList()) }
    var vehiculos by remember { mutableStateOf<List<Vehiculo>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    suspend fun cargarDatos() {
        cargando = true
        try {
            val resSensores = RetrofitClient.apiService.getSensores(token)
            if (resSensores.isSuccessful) {
                sensores = resSensores.body() ?: emptyList()
            }
            val resVehiculos = RetrofitClient.apiService.getVehiculos(token)
            if (resVehiculos.isSuccessful) {
                vehiculos = resVehiculos.body() ?: emptyList()
            }
        } catch (e: Exception) {
            mensaje = "Error cargando datos: ${e.message}"
        } finally {
            cargando = false
        }
    }

    // Carga inicial al entrar a la pantalla
    LaunchedEffect(Unit) {
        cargarDatos()
    }

    fun simularLecturaObd2() {
        if (sensores.isEmpty() || vehiculos.isEmpty()) {
            mensaje = "No hay sensores o vehículos disponibles"
            return
        }
        scope.launch {
            cargando = true
            try {
                val sensorAleatorio = sensores.random()
                val vehiculoAleatorio = vehiculos.first()
                val nivelSimulado = Random.nextInt(10, 100)

                val response = RetrofitClient.apiService.postLectura(
                    token,
                    LecturaRequest(
                        idSensor = sensorAleatorio.id,
                        idVehiculo = vehiculoAleatorio.idVehiculo,
                        nivel = nivelSimulado
                    )
                )

                if (response.isSuccessful) {
                    mensaje = "Lectura enviada: ${sensorAleatorio.nombre} → $nivelSimulado%"
                    cargarDatos() // refresca la lista con el nuevo estado
                } else {
                    mensaje = "Error al enviar lectura"
                }
            } catch (e: Exception) {
                mensaje = "Error de conexión: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Hola, $nombreCliente", fontSize = 22.sp)
                Text(
                    "Panel de sensores",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { scope.launch { cargarDatos() } }) {
                Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón simular OBD2
        Button(
            onClick = { simularLecturaObd2() },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (cargando) "Enviando..." else "📡 Simular lectura OBD2")
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
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
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

@Composable
fun SensorCard(sensor: Sensor) {
    val colorEstado = when (sensor.estado) {
        "falla" -> Color(0xFFE63946)
        "advertencia" -> Color(0xFFF4A261)
        else -> Color(0xFF2EC4B6)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(sensor.nombre, fontSize = 16.sp)
                Text(
                    sensor.tipo,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "${sensor.nivel}%",
                fontSize = 20.sp,
                color = colorEstado
            )
        }
    }
}