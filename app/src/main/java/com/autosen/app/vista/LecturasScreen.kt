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
import com.autosen.app.data.Lectura
import com.autosen.app.data.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun LecturasScreen() {
    var lecturas by remember { mutableStateOf<List<Lectura>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun cargarLecturas() {
        scope.launch {
            cargando = true
            try {
                val response = RetrofitClient.apiService.getLecturas()
                if (response.isSuccessful) {
                    lecturas = response.body() ?: emptyList()
                } else {
                    mensaje = "Error al cargar lecturas (${response.code()})"
                }
            } catch (e: Exception) {
                mensaje = "Error de conexión: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) { cargarLecturas() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Historial de lecturas", fontSize = 22.sp)
                Text(
                    "${lecturas.size} registros",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = { cargarLecturas() }) {
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

        if (cargando && lecturas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (lecturas.isEmpty() && !cargando) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Aún no hay lecturas registradas.\nUsa la pestaña Monitoreo para enviar una.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(lecturas) { lectura ->
                    LecturaCard(lectura)
                }
            }
        }
    }
}

@Composable
fun LecturaCard(lectura: Lectura) {
    val colorEstado = when (lectura.estado) {
        "falla" -> Color(0xFFE63946)
        "advertencia" -> Color(0xFFF4A261)
        else -> Color(0xFF2EC4B6)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(lectura.sensor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.weight(1f))
                Text("${lectura.nivel}%", fontSize = 18.sp, color = colorEstado, fontWeight = FontWeight.Bold)
            }
            Text(
                "${lectura.tipoSensor} · ${lectura.vehiculo}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Estado: ${lectura.estado}",
                fontSize = 12.sp,
                color = colorEstado
            )
            Text(
                "Fecha: ${lectura.fecha}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}