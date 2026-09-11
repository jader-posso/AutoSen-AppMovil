package com.autosen.app.vista

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

// Mis vehículos: lista + formulario para agregar (genera los 4 sensores automáticos)
@Composable
fun VehiculosScreen() {
    var vehiculos by remember { mutableStateOf<List<Vehiculo>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var enFormulario by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val correo = RetrofitClient.correoCliente

    fun cargarVehiculos() {
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
                        mensaje = body.mensaje ?: "No se pudieron cargar los vehículos."
                    }
                } else {
                    mensaje = "Error al cargar vehículos (${response.code()})"
                }
            } catch (e: Exception) {
                mensaje = "Error de conexión: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) { cargarVehiculos() }

    fun guardar(request: VehiculoRequest) {
        if (correo == null) return
        cargando = true
        scope.launch {
            try {
                val response = RetrofitClient.apiService.crearVehiculo(
                    correo = correo!!,
                    nombreVehiculo = request.nombreVehiculo,
                    marca = request.marca,
                    modelo = request.modelo,
                    color = request.color,
                    placa = request.placa,
                    tipoPlaca = request.tipoPlaca
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.ok == true) {
                        mensaje = "Vehículo registrado ✓"
                        enFormulario = false
                        cargarVehiculos()
                    } else {
                        mensaje = body.mensaje ?: "No se pudo guardar."
                    }
                } else {
                    val msg = mensajeDeErrorApi(response)
                    mensaje = if (msg.isNotBlank()) msg
                    else "Error al guardar (${response.code()})"
                }
            } catch (e: Exception) {
                mensaje = "Error de conexión: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    if (enFormulario) {
        FormularioVehiculo(
            cargando = cargando,
            mensaje = mensaje,
            onCancelar = {
                enFormulario = false
                mensaje = null
            },
            onGuardar = { request -> guardar(request) }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("MIS VEHÍCULOS", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${vehiculos.size} registrados",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                TextButton(onClick = { cargarVehiculos() }) { Text("🔄") }
                Button(onClick = { enFormulario = true }) { Text("➕ Agregar") }
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

        if (cargando && vehiculos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (vehiculos.isEmpty() && !cargando) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Aún no tienes vehículos.\nPulsa + para agregar uno.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(vehiculos) { v ->
                    VehiculoCard(v)
                }
            }
        }
    }
}
// Tarjeta de vehículo (web .vehicle-card) con barras de sensores
@Composable
fun VehiculoCard(v: Vehiculo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A38))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    v.placa,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Año ${v.modelo}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "${v.nombreVehiculo} — ${v.marca}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Color: ${v.color} · Tipo: ${v.tipoPlaca}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (v.sensores.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    v.sensores.forEach { s ->
                        val color = when {
                            s.nivel >= 70 -> Rojo
                            s.nivel >= 40 -> Dorado
                            else -> Oliva
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .background(Color(0xFF2A2A38), RoundedCornerShape(3.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(
                                        (s.nivel.coerceIn(0, 100) / 100f).coerceAtLeast(0.04f)
                                    )
                                    .height(5.dp)
                                    .background(color, RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${v.sensores.size} sensores",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "Sin sensores asignados",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
// Formulario para agregar vehículo (igual al de la web .crear-vehiculo)
@Composable
fun FormularioVehiculo(
    cargando: Boolean,
    mensaje: String?,
    onCancelar: () -> Unit,
    onGuardar: (VehiculoRequest) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    var tipoPlaca by remember { mutableStateOf("") }

    val formaValida = nombre.isNotBlank() && marca.isNotBlank() && modelo.isNotBlank() &&
            color.isNotBlank() && placa.isNotBlank() && tipoPlaca.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("AGREGAR VEHÍCULO", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "Se generarán 4 sensores automáticamente (MAF, O2, ECT, MAP).",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del vehículo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = marca,
            onValueChange = { marca = it },
            label = { Text("Marca") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = modelo,
            onValueChange = { modelo = it },
            label = { Text("Año / Modelo (1990-2030)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = placa,
            onValueChange = { placa = it },
            label = { Text("Placa (ABC123 o ABC-123)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = tipoPlaca,
            onValueChange = { tipoPlaca = it },
            label = { Text("Tipo de placa") },
            placeholder = { Text("Ej: particular, publico, oficial") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("Color") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (mensaje != null) {
            Text(
                text = mensaje,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onCancelar,
                enabled = !cargando,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = {
                    onGuardar(
                        VehiculoRequest(
                            nombreVehiculo = nombre.trim(),
                            marca = marca.trim(),
                            modelo = modelo.trim(),
                            color = color.trim(),
                            placa = placa.trim(),
                            tipoPlaca = tipoPlaca.trim()
                        )
                    )
                },
                enabled = formaValida && !cargando,
                modifier = Modifier.weight(1f)
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Registrar")
                }
            }
        }
    }
}