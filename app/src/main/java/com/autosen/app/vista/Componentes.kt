package com.autosen.app.vista

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autosen.app.data.Sensor
import com.autosen.app.ui.theme.Dorado
import com.autosen.app.ui.theme.Oliva
import com.autosen.app.ui.theme.Rojo

// Devuelve el color de estado (mismo tricromático de la web:
// falla #e63946, advertencia #f4a261, ok #2ec4b6)
internal fun colorDeEstado(estado: String): Color {
    return when (estado) {
        "falla" -> Rojo
        "advertencia" -> Dorado
        else -> Oliva
    }
}

// Barra de progreso estilo web (.bar-track / .bar-fill)
@Composable
internal fun BarraNivel(nivel: Int, color: Color) {
    val fraccion = (nivel.coerceIn(0, 100)) / 100f
    // Pista
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(99.dp))
    ) {
        // Relleno proporcional
        Box(
            modifier = Modifier
                .fillMaxWidth(fraccion.coerceAtLeast(0.02f))
                .height(6.dp)
                .background(color, RoundedCornerShape(99.dp))
        )
    }
}

@Composable
fun SensorCard(sensor: Sensor) {
    val colorEstado = colorDeEstado(sensor.estado)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(sensor.nombre, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        sensor.tipo,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${sensor.nivel}%",
                        fontSize = 20.sp,
                        color = colorEstado,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        sensor.estado,
                        fontSize = 11.sp,
                        color = colorEstado
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            BarraNivel(nivel = sensor.nivel, color = colorEstado)
        }
    }
}

@Composable
fun <T> SelectorSimple(
    label: String,
    opciones: List<T>,
    seleccionado: T?,
    textoDe: (T) -> String,
    onSeleccion: (T) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expandido = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = seleccionado?.let(textoDe) ?: "Selecciona un $label...",
                maxLines = 1
            )
        }
        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(textoDe(opcion)) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}