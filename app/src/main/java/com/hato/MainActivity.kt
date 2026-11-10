package com.hato

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.hato.data.repository.SessionManager
import com.hato.ui.MainScreen
import com.hato.ui.map.MapViewModel
import com.hato.ui.screens.auth.LoginScreen
import com.hato.ui.theme.HatoTheme
import com.hato.ui.theme.ThemeState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HatoTheme(darkTheme = ThemeState.isDarkMode.value) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val sessionManager = remember { SessionManager(context) }
                    var isLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }
                    val viewModel = remember { MapViewModel() }

                    LaunchedEffect(isLoggedIn) {
                        if (isLoggedIn) {
                            val u = sessionManager.getUser()
                            viewModel.loadUserData(u?.id, u?.email)
                        }
                    }

                    if (isLoggedIn) {
                        MainScreen(
                            viewModel = viewModel,
                            onLogout = {
                                sessionManager.clearSession()
                                com.hato.data.api.ApiClient.authToken = null
                                isLoggedIn = false
                            }
                        )
                    } else {
                        LoginScreen(
                            onLoginSuccess = {
                                val u = sessionManager.getUser()
                                viewModel.loadUserData(u?.id, u?.email)
                                isLoggedIn = true
                            }
                        )
                    }
                }
            }
        }
    }
}
