package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val JdmcDarkColorScheme = darkColorScheme(
    primary = JdmcLightYellow,
    onPrimary = JdmcObsidianBlack,
    primaryContainer = JdmcGoldContainer,
    onPrimaryContainer = JdmcLightYellow,
    secondary = JdmcBrushedSilver,
    onSecondary = JdmcObsidianBlack,
    secondaryContainer = JdmcCarbonHighlight,
    onSecondaryContainer = JdmcMetallicSilver,
    tertiary = JdmcWarmGold,
    onTertiary = JdmcObsidianBlack,
    background = JdmcObsidianBlack,
    onBackground = JdmcMetallicSilver,
    surface = JdmcCarbonSurface,
    onSurface = JdmcMetallicSilver,
    surfaceVariant = JdmcCarbonElevated,
    onSurfaceVariant = JdmcGunmetal,
    surfaceContainerLowest = JdmcObsidianBlack,
    surfaceContainerLow = JdmcCarbonSurface,
    surfaceContainer = JdmcCarbonElevated,
    surfaceContainerHigh = JdmcCarbonHighlight,
    outline = JdmcSteelOutline,
    outlineVariant = JdmcSteelSubtle,
    error = JdmcAmberAlert,
    onError = JdmcObsidianBlack,
    errorContainer = JdmcAmberAlertContainer,
    onErrorContainer = JdmcLightYellow
)

val JdmcShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(22.dp)
)

@Composable
fun JdmcHqTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JdmcDarkColorScheme,
        typography = JdmcTypography,
        shapes = JdmcShapes,
        content = content
    )
}

@Composable
fun JdmcClipperTheme(
    content: @Composable () -> Unit
) {
    JdmcHqTheme(content = content)
}
