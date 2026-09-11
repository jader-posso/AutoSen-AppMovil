package com.autosen.app.vista

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

enum class PantallaPrincipal(val titulo: String, val icono: String) {
    MONITOREO("Monitoreo", "🏠"),
    VEHICULOS("Vehículos", "🚗"),
    SENSORES("Sensores", "📡"),
    ALERTAS("Alertas", "🔔")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(nombreCliente: String, onLogout: () -> Unit) {
    var pantalla by remember { mutableStateOf(PantallaPrincipal.MONITOREO) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nombreCliente, fontWeight = FontWeight.SemiBold, fontSize = 20.sp) },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Salir")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                PantallaPrincipal.entries.forEach { p ->
                    NavigationBarItem(
                        selected = pantalla == p,
                        onClick = { pantalla = p },
                        icon = { Text(p.icono, fontSize = 20.sp) },
                        label = { Text(p.titulo) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (pantalla) {
                PantallaPrincipal.MONITOREO -> MonitoreoScreen()
                PantallaPrincipal.VEHICULOS -> VehiculosScreen()
                PantallaPrincipal.SENSORES -> SensoresScreen()
                PantallaPrincipal.ALERTAS -> AlertasScreen()
            }
        }
    }
}