package com.notex.sd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.notex.sd.core.preferences.AppPreferences
import com.notex.sd.core.preferences.ThemeMode
import com.notex.sd.core.theme.NoteXTheme
import com.notex.sd.navigation.NoteXNavGraph
import com.notex.sd.navigation.Route
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    private var keepSplashScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val onboardingCompleted = runBlocking {
            appPreferences.onboardingCompleted.first()
        }

        setContent {
            val themeMode by appPreferences.themeMode.collectAsStateWithLifecycle(
                initialValue = ThemeMode.SYSTEM
            )
            val dynamicColors by appPreferences.dynamicColors.collectAsStateWithLifecycle(
                initialValue = true
            )

            LaunchedEffect(Unit) {
                keepSplashScreen = false
            }

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            NoteXTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicColors
            ) {
                NoteXApp(
                    startDestination = if (onboardingCompleted) Route.Home else Route.Onboarding
                )
            }
        }
    }
}

@Composable
private fun NoteXApp(
    startDestination: Route
) {
    val navController = rememberNavController()

    NoteXNavGraph(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    )
}
