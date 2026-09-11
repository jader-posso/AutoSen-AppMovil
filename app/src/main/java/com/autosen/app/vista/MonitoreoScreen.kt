package com.autosen.app.vista

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autosen.app.data.*
import com.autosen.app.ui.theme.Oliva
import com.autosen.app.ui.theme.Dorado
import com.autosen.app.ui.theme.Rojo
import kotlinx.coroutines.launch

// Dashboard principal: estadísticas + botón Bluetooth (conexión ELM327 real)
@Composable
fun MonitoreoScreen() {
    var vehiculos by remember { mutableStateOf<List<Vehiculo>>(emptyList()) }
    var estadisticas by remember { mutableStateOf<Estadisticas?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var conectando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var enElm327 by remember { mutableStateOf(false) }
    var vehiculoSeleccionado by remember { mutableStateOf<Vehiculo?>(null) }
    var enviadasObd by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()
    val correo = RetrofitClient.correoCliente

    fun cargarDatos() {
        if (correo == null) return
        cargando = true
        scope.launch {
            try {
                val response = RetrofitClient.apiService.datosCliente(correo)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.ok == true) {
                        vehiculos = body.vehiculos ?: emptyList()
                        estadisticas = body.estadisticas
                        mensaje = null
                    } else {
                        mensaje = body.mensaje ?: "No se pudieron cargar los datos."
                    }
                } else {
                    val msg = mensajeDeErrorApi(response)
                    mensaje = if (msg.isNotBlank()) msg
                    else "Error al cargar datos (${response.code()})"
                }
            } catch (e: Exception) {
                mensaje = "Error de conexión: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) { cargarDatos() }

    // Todos los sensores de todos los vehículos del cliente
    val sensoresTodos = vehiculos.flatMap { it.sensores }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("DASHBOARD", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Estado en tiempo real",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Tarjetas de estadísticas (web .stats-grid) ──
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("VEHÍCULOS", estadisticas?.totalVehiculos ?: vehiculos.size, Oliva, Modifier.weight(1f))
                    StatCard("SENSORES", estadisticas?.totalSensores ?: sensoresTodos.size, Oliva, Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("ADVERTENCIAS", estadisticas?.advertencias ?: 0, Dorado, Modifier.weight(1f))
                    StatCard("FALLAS", estadisticas?.fallas ?: 0, Rojo, Modifier.weight(1f))
                }
            }

            // ── Botón Bluetooth (conexión real al ELM327 OBD-II) ──
            item {
                Button(
                    onClick = {
                        if (vehiculos.isEmpty()) {
                            mensaje = "Registra primero un vehículo para conectar el ELM327."
                        } else {
                            vehiculoSeleccionado = vehiculos.first()
                            enElm327 = true
                        }
                    },
                    enabled = !conectando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10302E),
                        contentColor = Oliva
                    ),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(
                        "🔵 CONECTAR BLUETOOTH (ELM327)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
if (mensaje != null) {
                item {
                    Text(
                        text = mensaje ?: "",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ── Panel de sensores ──
            if (cargando && sensoresTodos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (vehiculos.isEmpty()) {
                item {
                    Text(
                        "Aún no tienes vehículos registrados.\nAgrega uno en la pestaña Vehículos.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    )
                }
            } else {
                item {
                    Text("SENSORES", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                items(sensoresTodos) { sensor ->
                    SensorCard(sensor)
                }
            }
        }
    }

    // Si el usuario tocó CONECTAR BLUETOOTH, mostramos la pantalla ELM327
    val vehiculoParaElm = vehiculoSeleccionado
    if (enElm327 && vehiculoParaElm != null) {
        Elm327Screen(
            idVehiculo = vehiculoParaElm.idVehiculo,
            onVolver = {
                enElm327 = false
                vehiculoSeleccionado = null
                cargarDatos()
            },
            onLecturasEnviadas = { enviadasObd = it }
        )
        return
    }
}

@Composable
private fun StatCard(etiqueta: String, valor: Int, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A38))) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                etiqueta,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "$valor",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}