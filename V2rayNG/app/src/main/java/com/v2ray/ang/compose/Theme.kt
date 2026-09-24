package com.v2ray.ang.compose

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.v2ray.ang.AppConfig
import com.v2ray.ang.handler.MmkvManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val LightColor = lightColorScheme(
    primary = Color(0xFF16845F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9F8E7),
    onPrimaryContainer = Color(0xFF052A1E),
    secondary = Color(0xFF7CCB35),
    onSecondary = Color(0xFF10210B),
    secondaryContainer = Color(0xFFE8F8D4),
    onSecondaryContainer = Color(0xFF19330D),
    tertiary = Color(0xFF1B9A6C),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD5F5E5),
    onTertiaryContainer = Color(0xFF052C20),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF5F8F5),
    onBackground = Color(0xFF10211A),
    surface = Color.White,
    onSurface = Color(0xFF10211A),
    surfaceVariant = Color(0xFFE1EAE4),
    onSurfaceVariant = Color(0xFF52645B),
    outline = Color(0xFF75877D),
    outlineVariant = Color(0xFFC6D1CA),
    inverseSurface = Color(0xFF21352B),
    inverseOnSurface = Color(0xFFEAF3ED),
    inversePrimary = Color(0xFF77E0B1),
    scrim = Color.Black,
    surfaceTint = Color(0xFF16845F),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF0F5F1),
    surfaceContainer = Color(0xFFEBF1ED),
    surfaceContainerHigh = Color(0xFFE5ECE7),
    surfaceContainerHighest = Color(0xFFDDE6E0),
)

private val DarkColor = darkColorScheme(
    primary = Color(0xFFA9FF5B),
    onPrimary = Color(0xFF10210B),
    primaryContainer = Color(0xFF244A31),
    onPrimaryContainer = Color(0xFFD9F8E7),
    secondary = Color(0xFF5DE4A2),
    onSecondary = Color(0xFF052A1E),
    secondaryContainer = Color(0xFF173D2B),
    onSecondaryContainer = Color(0xFFC9F6DE),
    tertiary = Color(0xFF77E0B1),
    onTertiary = Color(0xFF062A1E),
    tertiaryContainer = Color(0xFF164B35),
    onTertiaryContainer = Color(0xFFCFF8E2),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF06100C),
    onBackground = Color(0xFFE5F0EA),
    surface = Color(0xFF0A1712),
    onSurface = Color(0xFFE5F0EA),
    surfaceVariant = Color(0xFF1B2B23),
    onSurfaceVariant = Color(0xFFACBDB4),
    outline = Color(0xFF74867C),
    outlineVariant = Color(0xFF34483D),
    inverseSurface = Color(0xFFE5F0EA),
    inverseOnSurface = Color(0xFF10211A),
    inversePrimary = Color(0xFF16845F),
    scrim = Color.Black,
    surfaceTint = Color(0xFFA9FF5B),
    surfaceContainerLowest = Color(0xFF030906),
    surfaceContainerLow = Color(0xFF08130E),
    surfaceContainer = Color(0xFF0D1B14),
    surfaceContainerHigh = Color(0xFF14231B),
    surfaceContainerHighest = Color(0xFF1B2C22),
)

val colorPing = Color(0xFF38D890)
val colorPingRed = Color(0xFFFF6B7D)
val colorConfigType = Color(0xFF36B982)
val colorFabActive = Color(0xFF1B9A6C)
val colorFabInactiveLight = Color(0xFF8A9A91)
val colorFabInactiveDark = Color(0xFF52645B)
val dividerColorLight = Color(0xFFDDE6E0)
val dividerColorDark = Color(0xFF2A3C32)

val toastNormalBgLight = Color(0xB321352B)
val toastNormalBgDark = Color(0xB30A1712)
val toastSuccessBg = Color(0xB316845F)
val toastErrorBg = Color(0xB3BA1A1A)
val toastInfoBg = Color(0xB31B9A6C)
val toastIconCircleBg = Color(0x33FFFFFF)
val toastTextColor = Color.White

object ThemeManager {
    private val _themeMode = MutableStateFlow(
        MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "0") ?: "0"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setThemeMode(mode: String) {
        MmkvManager.encodeSettings(AppConfig.PREF_UI_MODE_NIGHT, mode)
        _themeMode.value = mode
    }

    fun refresh() {
        _themeMode.value = MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "0") ?: "0"
    }
}

@Composable
fun resolveDarkTheme(): Boolean {
    val mode by ThemeManager.themeMode.collectAsState()
    return when (mode) {
        "1" -> false
        "2" -> true
        else -> isSystemInDarkTheme()
    }
}

val LocalDarkTheme = compositionLocalOf { false }

@Composable
fun AppTheme(
    darkTheme: Boolean = resolveDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColor else LightColor
    val snackbarController = rememberAppSnackbarController()
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            WindowCompat.getInsetsController(activity.window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalAppSnackbar provides snackbarController
    ) {
        MaterialTheme(colorScheme = colorScheme) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
                AppSnackbarBridge(controller = snackbarController)
                AppSnackbarHost(hostState = snackbarController.hostState)
            }
        }
    }
}
