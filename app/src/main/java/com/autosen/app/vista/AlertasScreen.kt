package com.autosen.app.vista

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autosen.app.data.*
import com.autosen.app.ui.theme.Oliva
import com.autosen.app.ui.theme.Dorado
import com.autosen.app.ui.theme.Rojo
import kotlinx.coroutines.launch

// Sensor junto con su vehículo (para mostrar la alerta con contexto)
data class AlertaSensor(val sensor: Sensor, val vehiculo: Vehiculo)

// Alertas: sensores con nivel de advertencia (>=40%) o falla (>=70%) + alertas OBD2 reales
@Composable
fun AlertasScreen() {
    var vehiculos by remember { mutableStateOf<List<Vehiculo>>(emptyList()) }
    var alertasObd by remember { mutableStateOf<List<AlertaObd>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val correo = RetrofitClient.correoCliente

    fun cargarAlertas() {
        if (correo == null) return
        cargando = true
        scope.launch {
            try {
                val response = RetrofitClient.apiService.datosCliente(correo)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.ok == true) {
                        vehiculos = body.vehiculos ?: emptyList()
                        mensaje = null
                        // Cargar alertas OBD2 reales de cada vehículo
                        val obd = ArrayList<AlertaObd>()
                        for (v in vehiculos) {
                            try {
                                val r = RetrofitClient.apiService.alertasObd(v.idVehiculo)
                                if (r.isSuccessful && r.body()?.alertas != null) {
                                    obd.addAll(r.body()!!.alertas!!)
                                }
                            } catch (_: Exception) {}
                        }
                        alertasObd = obd
                    } else {
                        mensaje = body.mensaje ?: "No se pudieron cargar las alertas."
                    }
                } else {
                    mensaje = "Error al cargar alertas (${response.code()})"
                }
            } catch (e: Exception) {
                mensaje = "Error de conexión: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) { cargarAlertas() }

    // Sensores en advertencia o falla, incluyendo su vehículo (vista general)
    val alertas: List<AlertaSensor> = vehiculos.flatMap { v ->
        v.sensores.filter { it.nivel >= 40 }.map { AlertaSensor(it, v) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ALERTAS", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${alertas.size} sensores con atención",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = { cargarAlertas() }) { Text("🔄") }
        }

        if (mensaje != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = mensaje ?: "", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (cargando && vehiculos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (alertas.isEmpty() && alertasObd.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay alertas activas. 🎉\nTodos tus sensores están en niveles OK.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(alertasObd) { a ->
                    AlertaObdCard(a)
                }
                items(alertas) { a ->
                    AlertaSensorCard(a)
                }
            }
        }
    }
}

@Composable
private fun AlertaSensorCard(a: AlertaSensor) {
    val color = when {
        a.sensor.nivel >= 70 -> Rojo
        else -> Dorado
    }
    val estado = nivelAEstado(a.sensor.nivel)

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    estado,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${a.sensor.nivel}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(a.sensor.nombreSensor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "${a.vehiculo.nombreVehiculo} (${a.vehiculo.placa}) · ${a.sensor.tipoSensor}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Tipo daño: ${a.sensor.tipoDano}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            BarraNivel(nivel = a.sensor.nivel, color = color)
        }
    }
}
// Tarjeta de alerta OBD2 real (generada por el backend)
@Composable
private fun AlertaObdCard(a: AlertaObd) {
    val color = when (a.tipo) {
        "falla" -> Rojo
        "advertencia" -> Dorado
        else -> Oliva
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    a.tipo.uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${a.nivel}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(a.mensaje, fontSize = 14.sp)
            Text(
                "${a.nombreSensor} · ${a.tipoSensor}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Valor: ${a.valor} · Fecha: ${a.fechaAlerta}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}