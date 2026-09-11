package com.autosen.app.vista

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autosen.app.data.LoginRequest
import com.autosen.app.data.RegisterRequest
import com.autosen.app.data.RetrofitClient
import com.autosen.app.data.mensajeDeErrorApi
import com.autosen.app.ui.theme.BebasNeue
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    iniciarEnRegistro: Boolean = false,
    onLoginSuccess: (token: String, nombre: String) -> Unit,
    onVolver: (() -> Unit)? = null
) {

    var esRegistro by remember { mutableStateOf(iniciarEnRegistro) }
    var nombreCliente by remember { mutableStateOf("") }
    var apellidoCliente by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val formValido = correo.isNotBlank() && contrasena.isNotBlank() &&
            (!esRegistro ||
                    (nombreCliente.isNotBlank() && apellidoCliente.isNotBlank() &&
                            contrasena.length >= 8 && confirmarContrasena == contrasena))

    Box(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo hexágono estilo web (.logo-icon con clip-path)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(Hexagono)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "AUTO SEN",
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = BebasNeue
            )
            Text(
                text = "Monitoreo OBD2",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (esRegistro) {
                OutlinedTextField(
                    value = nombreCliente,
                    onValueChange = { nombreCliente = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = apellidoCliente,
                    onValueChange = { apellidoCliente = it },
                    label = { Text("Apellido") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            if (esRegistro) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmarContrasena,
                    onValueChange = { confirmarContrasena = it },
                    label = { Text("Confirmar contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    error = null
                    cargando = true
                    scope.launch {
                        try {
                            val response = if (esRegistro) {
                                RetrofitClient.apiService.registrar(
                                    nombre = nombreCliente,
                                    apellido = apellidoCliente,
                                    correo = correo,
                                    contrasena = contrasena
                                )
                            } else {
                                RetrofitClient.apiService.login(correo, contrasena)
                            }

                            if (response.isSuccessful && response.body() != null) {
                                val body = response.body()!!
                                if (body.ok == true) {
                                    RetrofitClient.correoCliente = correo
                                    val cliente = body.cliente
                                    val nombreCompleto = if (cliente != null) {
                                        "${cliente.nombreCliente} ${cliente.apellidoCliente}".trim()
                                    } else {
                                        correo.substringBefore("@")
                                    }
                                    RetrofitClient.nombreCliente = nombreCompleto
                                    onLoginSuccess(correo, nombreCompleto)
                                } else {
                                    error = body.mensaje ?: "No se pudo iniciar sesión."
                                }
                            } else {
                                val msg = mensajeDeErrorApi(response)
                                error = if (msg.isNotBlank()) msg
                                else "Error al iniciar sesión (${response.code()})"
                            }
                        } catch (e: Exception) {
                            error = "Error de conexión: ${e.message}"
                        } finally {
                            cargando = false
                        }
                    }
                },
                enabled = formValido && !cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (esRegistro) "Crear cuenta" else "Iniciar sesión")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = {
                esRegistro = !esRegistro
                error = null
            }) {
                Text(
                    if (esRegistro) "¿Ya tienes cuenta? Inicia sesión"
                    else "¿No tienes cuenta? Regístrate"
                )
            }

            if (onVolver != null) {
                TextButton(onClick = onVolver) {
                    Text(
                        "← Volver al inicio",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
// Forma hexagonal para el logo (equivalente a clip-path polygon en el CSS web)
private class HexagonoShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            lineTo(w, h * 0.25f)
            lineTo(w, h * 0.75f)
            lineTo(w * 0.5f, h)
            lineTo(0f, h * 0.75f)
            lineTo(0f, h * 0.25f)
            close()
        }
        return Outline.Generic(path)
    }
}

private val Hexagono: Shape = HexagonoShape()