package com.autosen.app.vista

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autosen.app.data.Alerta
import com.autosen.app.data.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun AlertasScreen() {
    var alertas by remember { mutableStateOf<List<Alerta>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun cargarAlertas() {
        scope.launch {
            cargando = true
            try {
                val response = RetrofitClient.apiService.getAlertas()
                if (response.isSuccessful) {
                    alertas = response.body() ?: emptyList()
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

    fun marcarLeida(alerta: Alerta) {
        scope.launch {
            try {
                val response = RetrofitClient.apiService.marcarAlertaLeida(alerta.id)
                if (response.isSuccessful) {
                    mensaje = "Alerta marcada como leída ✓"
                    cargarAlertas()
                } else {
                    mensaje = "No se pudo marcar la alerta (${response.code()})"
                }
            } catch (e: Exception) {
                mensaje = "Error de conexión: ${e.message}"
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
                Text("Alertas", fontSize = 22.sp)
                Text(
                    "${alertas.size} sin leer",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = { cargarAlertas() }) {
                Text("🔄 Actualizar")
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

        Spacer(modifier = Modifier.height(8.dp))

        if (cargando && alertas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (alertas.isEmpty() && !cargando) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay alertas activas. 🎉",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(alertas) { alerta ->
                    AlertaCard(
                        alerta = alerta,
                        onMarcarLeida = { marcarLeida(alerta) }
                    )
                }
            }
        }
    }
}

@Composable
fun AlertaCard(alerta: Alerta, onMarcarLeida: () -> Unit) {
    val colorTipo = when (alerta.tipo) {
        "falla" -> Color(0xFFE63946)
        "advertencia" -> Color(0xFFF4A261)
        else -> Color(0xFF2EC4B6)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    alerta.tipo.uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorTipo,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onMarcarLeida) {
                    Text("Marcar leída", fontSize = 13.sp)
                }
            }
            Text(alerta.mensaje, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${alerta.sensor} · ${alerta.vehiculo}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Fecha: ${alerta.fecha}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}