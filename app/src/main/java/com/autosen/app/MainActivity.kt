package com.autosen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.autosen.app.data.RetrofitClient
import com.autosen.app.ui.theme.AutoSenTheme
import com.autosen.app.vista.WelcomeScreen
import com.autosen.app.vista.DashboardScreen
import com.autosen.app.vista.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoSenTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AutoSenApp()
                }
            }
        }
    }
}

// Pantallas previas al inicio de sesión: bienvenida → login
private enum class PantallaAcceso {
    BIENVENIDA,
    LOGIN,
}

@Composable
fun AutoSenApp() {
    var token by remember { mutableStateOf<String?>(null) }
    var nombreCliente by remember { mutableStateOf("") }
    var pantalla by remember { mutableStateOf(PantallaAcceso.BIENVENIDA) }
    var iniciarRegistro by remember { mutableStateOf(false) }

    if (token == null) {
        when (pantalla) {
            PantallaAcceso.BIENVENIDA -> {
                WelcomeScreen(
                    onIniciarSesion = {
                        iniciarRegistro = false
                        pantalla = PantallaAcceso.LOGIN
                    },
                    onRegistrarse = {
                        iniciarRegistro = true
                        pantalla = PantallaAcceso.LOGIN
                    }
                )
            }
            PantallaAcceso.LOGIN -> {
                LoginScreen(
                    iniciarEnRegistro = iniciarRegistro,
                    onLoginSuccess = { nuevoToken, nombre ->
                        RetrofitClient.token = nuevoToken
                        token = nuevoToken
                        nombreCliente = nombre
                    }
                )
            }
        }
    } else {
        DashboardScreen(
            nombreCliente = nombreCliente,
            onLogout = {
                RetrofitClient.token = null
                token = null
                nombreCliente = ""
                pantalla = PantallaAcceso.BIENVENIDA
            }
        )
    }
}