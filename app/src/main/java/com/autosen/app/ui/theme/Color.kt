package com.autosen.app.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════
//  PALETA "AUTOSEN" (espejo del tema CSS de la versión web)
//  :root{ --black:#0a0a0c --dark:#111116 --card:#16161e
//         --border:#2a2a38 --accent:#030853 --teal:#2ec4b6
//         --gold:#f4a261 --red:#e63946 --white:#f0eff4
//         --muted:#7a7a96 }
// ═══════════════════════════════════════════════════════════════

// Fondos / superficies
val Negro = Color(0xFF0A0A0C)      // --black
val Oscuro = Color(0xFF111116)     // --dark
val Tarjeta = Color(0xFF16161E)    // --card
val Borde = Color(0xFF2A2A38)     // --border
val TextoPrinc = Color(0xFFF0EFF4) // --white
val TextoAtenc = Color(0xFF9BA9B8) // --muted (aclarado para más legibilidad)

// Acentos
val AzulAcento = Color(0xFF030853)   // --accent (índigo oscuro / fondos)
val AzulBrillante = Color(0xFF2B4AC8) // acento claro para botones y texto (más visible)
val Oliva = Color(0xFF2EC4B6)          // --teal   (OK)
val Dorado = Color(0xFFF4A261)        // --gold   (advertencia)
val Rojo =    Color(0xFFE63946)       // --red    (falla)

// Variantes usadas en estados (fondo + borde con alpha)
val AzulAcentoSuave = Color(0xFF1A237E)

// Acentos claros para texto/iconos sobre fondo oscuro (bien legibles)
val AzulBrillo = Color(0xFF2B4AC8)      // alias de AzulBrillante
val AzulBrilloSuave = Color(0xFF7B95E8) // índigo más claro para textos pequeños y líneas

// Versiones claras (para texto sobre acento)
val BlancoHueso = Color(0xFFF0EFF4)