package com.hato

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hato.data.repository.SessionManager
import com.hato.ui.MainScreen
import com.hato.ui.map.MapViewModel
import com.hato.ui.screens.auth.LoginScreen
import com.hato.ui.theme.HatoTheme
import com.hato.ui.theme.ThemeState
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDark = ThemeState.isDarkMode.value

            HatoTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val sessionManager = remember { SessionManager(context) }
                    var isLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }
                    val viewModel = remember { MapViewModel() }
                    var isShowingSplash by remember { mutableStateOf(true) }

                    LaunchedEffect(Unit) {
                        viewModel.initContext(context)
                        delay(1200)
                        isShowingSplash = false
                    }

                    LaunchedEffect(isLoggedIn) {
                        if (isLoggedIn) {
                            val u = sessionManager.getUser()
                            viewModel.loadUserData(u?.id, u?.email)
                        }
                    }

                    if (isShowingSplash) {
                        // Portada y Splash oficial con el logo
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo),
                                    contentDescription = "Portada Hato",
                                    modifier = Modifier
                                        .size(130.dp)
                                        .clip(RoundedCornerShape(30.dp)),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "HATO",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Ganadería Digital y Telemetría en Tiempo Real",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
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
}
