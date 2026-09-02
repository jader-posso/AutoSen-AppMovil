package com.autosen.app.vista

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autosen.app.ui.theme.*

// ────────────────────────────────────────────────────────────────
//  WELCOME SCREEN — Primera pantalla de la app.
//  Rediseño inspirado en el HERO de la web (web_index.html):
//  eyebrow con línea de acento, título grande en Bebas,
//  CTAs y fila de estadísticas, con franja diagonal sutil.
// ────────────────────────────────────────────────────────────────

// Forma hexagonal para el logo (equivalente a clip-path polygon del CSS web)
private class HexagonoPortal : Shape {
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

// Franja diagonal decorativa del hero (espejo del .hero::after de la web)
private class FranjaDiagonal : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.2f, 0f)
            lineTo(w, 0f)
            lineTo(w * 0.8f, h)
            lineTo(0f, h)
            close()
        }
        return Outline.Generic(path)
    }
}

private val Hexagono: Shape = HexagonoPortal()
private val Diagonal: Shape = FranjaDiagonal()

// Marca superior: logo hexagonal "AS" + nombre de la app
@Composable
private fun Marca() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(Hexagono)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AS",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "AUTOSEN",
            fontFamily = BebasNeue,
            fontSize = 20.sp,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// Eyebrow con líneas de acento a ambos lados (estilo del hero web)
@Composable
private fun Eyebrow() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(width = 20.dp, height = 1.dp).background(AzulBrilloSuave))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "SISTEMA DE DIAGNÓSTICO VEHICULAR",
            fontFamily = JetBrainsMono,
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            color = AzulBrilloSuave,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.size(width = 20.dp, height = 1.dp).background(AzulBrilloSuave))
    }
}

// Línea del título grande en Bebas
@Composable
private fun LineaTitulo(texto: String, color: Color) {
    Text(
        text = texto,
        fontFamily = BebasNeue,
        fontSize = 42.sp,
        letterSpacing = 1.sp,
        color = color,
        lineHeight = 44.sp
    )
}

// Indicador de estadística: barra de acento + número + etiqueta mono
@Composable
private fun Indicador(numero: String, sufijo: String, etiqueta: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(width = 2.dp, height = 26.dp).background(AzulBrilloSuave))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = numero,
                fontFamily = BebasNeue,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = sufijo,
                fontFamily = BebasNeue,
                fontSize = 20.sp,
                color = AzulBrilloSuave
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = etiqueta,
            fontFamily = JetBrainsMono,
            fontSize = 9.sp,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Interfaz inicial de bienvenida (antes del login)
@Composable
fun WelcomeScreen(onIniciarSesion: () -> Unit, onRegistrarse: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Franja diagonal decorativa en la esquina superior derecha
        Box(
            modifier = Modifier
                .size(width = 300.dp, height = 460.dp)
                .align(Alignment.TopEnd)
                .clip(Diagonal)
                .background(Color(0x14C1121F)) // rojo automotriz a baja opacidad
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // ── Marca ──
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Marca()
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── HERO ──
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Eyebrow()

                Spacer(modifier = Modifier.height(18.dp))

                LineaTitulo("PROTEGE", MaterialTheme.colorScheme.onBackground)
                LineaTitulo("TU", AzulBrillo)
                LineaTitulo("VEHÍCULO", MaterialTheme.colorScheme.onBackground)

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Monitoreo inteligente de sensores automotrices en tiempo real. Detecta fallas antes de que ocurran y mantén tu vehículo siempre en óptimas condiciones.",
                    fontFamily = DMSans,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onRegistrarse,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("COMENZAR AHORA", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onIniciarSesion,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("INICIAR SESIÓN", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Estadísticas inferiores ──
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Indicador("98", "%", "PRECISIÓN")
                Indicador("24", "h", "MONITOREO")
                Indicador("+50", "K", "VEHÍCULOS")
            }
        }
    }
}