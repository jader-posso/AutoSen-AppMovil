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
import com.autosen.app.ui.theme.AutoSenTheme
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

@Composable
fun AutoSenApp() {
    // Estado simple para saber si el usuario está logueado o no
    var token by remember { mutableStateOf<String?>(null) }
    var nombreCliente by remember { mutableStateOf("") }

    if (token == null) {
        LoginScreen(
            onLoginSuccess = { nuevoToken, nombre ->
                token = nuevoToken
                nombreCliente = nombre
            }
        )
    } else {
        DashboardScreen(
            token = token!!,
            nombreCliente = nombreCliente
        )
    }
}