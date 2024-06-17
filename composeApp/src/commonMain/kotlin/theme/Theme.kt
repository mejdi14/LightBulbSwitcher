package theme

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

val LightColorPalette = lightColors(
    primary = Color(0xFF000000),
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    error = Color(0xFFB00020),
    onError = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black
)

// Define the dark color palette
val DarkColorPalette = darkColors(
    primary = Color(0xFFFFFFFF),
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFF000000),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    error = Color(0xFFCF6679),
    onError = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color.White
)

