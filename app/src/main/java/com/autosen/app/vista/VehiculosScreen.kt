package com.autosen.app.vista

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun VehiculosScreen() {
    var vehiculos by remember { mutableStateOf<List<Vehiculo>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var enFormulario by remember { mutableStateOf(false) }
    var vehiculoEditando by remember { mutableStateOf<Vehiculo?>(null) }
    var vehiculoAEliminar by remember { mutableStateOf<Vehiculo?>(null) }

    val scope = rememberCoroutineScope()

    fun cargarVehiculos() {
        scope.launch {
            cargando = true
            try {
                val response = RetrofitClient.apiService.getVehiculos()
                if (response.isSuccessful) {
                    vehiculos = response.body() ?: emptyList()
                    mensaje = null
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

    fun guardar(esEditar: Boolean, id: Int, request: VehiculoRequest) {
        scope.launch {
            cargando = true
            try {
                val response = if (esEditar) {
                    RetrofitClient.apiService.updateVehiculo(id, request)
                } else {
                    RetrofitClient.apiService.createVehiculo(request)
                }
                if (response.isSuccessful) {
                    mensaje = if (esEditar) "Vehículo actualizado ✓" else "Vehículo creado ✓"
                    enFormulario = false
                    vehiculoEditando = null
                    cargarVehiculos() // refresca con los datos de la BD
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

    fun eliminar(v: Vehiculo) {
        scope.launch {
            cargando = true
            try {
                val response = RetrofitClient.apiService.deleteVehiculo(v.idVehiculo)
                if (response.isSuccessful) {
                    mensaje = "Vehículo eliminado ✓"
                    cargarVehiculos()
                } else {
                    val msg = mensajeDeErrorApi(response)
                    mensaje = if (msg.isNotBlank()) msg
                    else "No se pudo eliminar (${response.code()})"
                }
            } catch (e: Exception) {
                mensaje = "Error de conexión: ${e.message}"
            } finally {
                cargando = false
                vehiculoAEliminar = null
            }
        }
    }
if (enFormulario) {
        VehiculoFormScreen(
            vehiculo = vehiculoEditando,
            cargando = cargando,
            mensaje = mensaje,
            onCancelar = {
                enFormulario = false
                vehiculoEditando = null
            },
            onGuardar = { request ->
                val editando = vehiculoEditando
                if (editando != null) guardar(true, editando.idVehiculo, request)
                else guardar(false, -1, request)
            }
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
                Text("Vehículos", fontSize = 22.sp)
                Text(
                    "${vehiculos.size} registrados",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                TextButton(onClick = { cargarVehiculos() }) {
                    Text("🔄 Actualizar")
                }
                TextButton(onClick = {
                    vehiculoEditando = null
                    enFormulario = true
                }) {
                    Text("➕ Agregar")
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
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(vehiculos) { v ->
                    VehiculoCard(
                        vehiculo = v,
                        onEditar = {
                            vehiculoEditando = v
                            enFormulario = true
                        },
                        onEliminar = { vehiculoAEliminar = v }
                    )
                }
            }
        }
    }

    vehiculoAEliminar?.let { v ->
        AlertDialog(
            onDismissRequest = { vehiculoAEliminar = null },
            title = { Text("Eliminar vehículo") },
            text = { Text("¿Eliminar \"${v.nombreVehiculo}\" (${v.placa})? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { eliminar(v) }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { vehiculoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}
@Composable
fun VehiculoCard(
    vehiculo: Vehiculo,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(vehiculo.nombreVehiculo, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${vehiculo.marca} ${vehiculo.modelo}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Placa: ${vehiculo.placa} · Tipo: ${vehiculo.tipoPlaca} · Color: ${vehiculo.color}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                TextButton(onClick = onEditar) {
                    Text("✏️")
                }
                TextButton(onClick = onEliminar) {
                    Text("🗑️", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun VehiculoFormScreen(
    vehiculo: Vehiculo?,
    cargando: Boolean,
    mensaje: String?,
    onCancelar: () -> Unit,
    onGuardar: (VehiculoRequest) -> Unit
) {
    var nombre by remember { mutableStateOf(vehiculo?.nombreVehiculo ?: "") }
    var color by remember { mutableStateOf(vehiculo?.color ?: "") }
    var marca by remember { mutableStateOf(vehiculo?.marca ?: "") }
    var modelo by remember { mutableStateOf(vehiculo?.modelo ?: "") }
    var placa by remember { mutableStateOf(vehiculo?.placa ?: "") }
    var tipoPlaca by remember { mutableStateOf(vehiculo?.tipoPlaca ?: "") }

    val formaValida = nombre.isNotBlank() && color.isNotBlank() && marca.isNotBlank() &&
            modelo.isNotBlank() && placa.isNotBlank() && tipoPlaca.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            if (vehiculo == null) "Nuevo vehículo" else "Editar vehículo",
            fontSize = 22.sp,
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
            label = { Text("Modelo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = placa,
            onValueChange = { placa = it },
            label = { Text("Placa") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = tipoPlaca,
            onValueChange = { tipoPlaca = it },
            label = { Text("Tipo de placa") },
            placeholder = { Text("Ej: Particular, Público...") },
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
                            nombreVehiculo = nombre,
                            color = color,
                            marca = marca,
                            modelo = modelo,
                            placa = placa,
                            tipoPlaca = tipoPlaca
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
                    Text("Guardar")
                }
            }
        }
    }
}