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
import kotlinx.coroutines.launch

@Composable
fun SensoresScreen() {
    var vehiculos by remember { mutableStateOf<List<Vehiculo>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val correo = RetrofitClient.correoCliente

    fun cargarSensores() {
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
                    } else {
                        mensaje = body.mensaje ?: "No se pudieron cargar los sensores."
                    }
                } else {
                    mensaje = "Error al cargar sensores (${response.code()})"
                }
            } catch (e: Exception) {
                mensaje = "Error de conexión: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) { cargarSensores() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("MIS SENSORES", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Estado en tiempo real", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = { cargarSensores() }) { Text("🔄") }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LeyendaItem(Color(0xFF2EC4B6), "OK (0-39%)")
            LeyendaItem(Color(0xFFF4A261), "ADVERTENCIA (40-69%)")
            LeyendaItem(Color(0xFFE63946), "FALLA (70-100%)")
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (mensaje != null) {
            Text(text = mensaje ?: "", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(bottom = 8.dp))
        }
if (cargando && vehiculos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (vehiculos.isEmpty()) {
                    item {
                        Text(
                            "Sin vehículos registrados. Agrega uno para ver sus sensores.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(24.dp)
                        )
                    }
                } else {
                    items(vehiculos) { v ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A38))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "🚗 ${v.nombreVehiculo} — ${v.marca}",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "Color: ${v.color}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            v.placa,
                                            fontSize = 11.sp,
                                            color = Oliva,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            if (v.sensores.isEmpty()) {
                                Text(
                                    "Este vehículo no tiene sensores asignados.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                                )
                            } else {
                                v.sensores.forEach { s ->
                                    SensorCard(s)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeyendaItem(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(texto, fontSize = 11.sp, color = color)
    }
}