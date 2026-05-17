package com.example.proyectotfg.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    secondary = GreenGrey80,
    tertiary = Mint80,
    background = Color(0xFF0E1F10),
    surface = Color(0xFF172918),
    primaryContainer = Color(0xFF245A29),
    secondaryContainer = Color(0xFF2B3F2B),
    onPrimary = Color(0xFF0B260D),
    onSecondary = Color(0xFF0B260D),
    onTertiary = Color(0xFF0B260D),
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    secondary = GreenGrey40,
    tertiary = Mint40,
    background = FondoPlantas,
    surface = SuperficiePlantas,
    primaryContainer = ContenedorPrincipalPlantas,
    secondaryContainer = ContenedorSecundarioPlantas,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onPrimaryContainer = TextoPlantas,
    onSecondaryContainer = TextoPlantas,
    onBackground = TextoPlantas,
    onSurface = TextoPlantas
)

@Composable
fun ProyectoTFGTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
