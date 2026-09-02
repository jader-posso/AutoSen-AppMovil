package com.autosen.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════
//  COLOR SCHEME "AUTOSEN" — Tema oscuro (igual que la web,
//  que es 100% dark). Mapea los tokens de Material 3 a la paleta
//  del CSS: fondo negro, tarjetas #16161e, acento índigo #030853,
//  estados teal/gold/red.
// ═══════════════════════════════════════════════════════════════

private val AutoSenDarkColorScheme = darkColorScheme(
    primary = AzulBrillante,
    onPrimary = BlancoHueso,
    primaryContainer = Color(0xFF3A5BDB),
    onPrimaryContainer = BlancoHueso,

    secondary = Oliva,
    onSecondary = Negro,
    secondaryContainer = Color(0xFF143A38),
    onSecondaryContainer = Color(0xFF9EF0E6),

    tertiary = Dorado,
    onTertiary = Color(0xFF1A1A1A),
    tertiaryContainer = Color(0xFF3A2A16),
    onTertiaryContainer = Color(0xFFFBD4A9),

    error = Rojo,
    onError = BlancoHueso,
    errorContainer = Color(0xFF4A1B20),
    onErrorContainer = Color(0xFFF5C0C5),

    background = Negro,
    onBackground = TextoPrinc,
    surface = Tarjeta,
    onSurface = TextoPrinc,
    surfaceVariant = Oscuro,
    onSurfaceVariant = TextoAtenc,
    outline = Color(0xFF4A5A7A),
    outlineVariant = Color(0xFF3A4A6A),
    scrim = Color(0xFF000000),
)

@Composable
fun AutoSenTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AutoSenDarkColorScheme,
        typography = Typography,
        content = content
    )
}