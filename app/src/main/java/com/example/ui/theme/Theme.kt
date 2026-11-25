package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme =
  lightColorScheme(
    primary = TitaniumBlue,
    onPrimary = Color.White,
    primaryContainer = TitaniumBlueContainer,
    onPrimaryContainer = DeepNavyBrand,
    secondary = DeepNavyBrand,
    onSecondary = Color.White,
    secondaryContainer = MistSurface,
    onSecondaryContainer = ObsidianInk,
    tertiary = EmeraldVerify,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldSoft,
    onTertiaryContainer = EmeraldVerify,
    background = SnowSurface,
    onBackground = ObsidianInk,
    surface = PureCard,
    onSurface = ObsidianInk,
    surfaceVariant = MistSurface,
    onSurfaceVariant = SlateMuted,
    outline = HairlineOutline,
    error = CrimsonAlert,
    errorContainer = CrimsonSoft,
    onErrorContainer = CrimsonAlert,
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = SkyBlueAccent,
    onPrimary = DeepCarbon,
    primaryContainer = SkyBlueContainerDark,
    onPrimaryContainer = PearlText,
    secondary = PearlText,
    onSecondary = DeepCarbon,
    secondaryContainer = ElevatedSlate,
    onSecondaryContainer = PearlText,
    tertiary = EmeraldVerify,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF103622),
    onTertiaryContainer = Color(0xFF6EE7B7),
    background = DeepCarbon,
    onBackground = PearlText,
    surface = ElevatedSlate,
    onSurface = PearlText,
    surfaceVariant = CardSlateDark,
    onSurfaceVariant = CoolGrayText,
    outline = HairlineDark,
    error = Color(0xFFF87171),
    errorContainer = Color(0xFF45161B),
    onErrorContainer = Color(0xFFFCA5A5),
  )

val AppShapes =
  Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
  )

@Composable
fun HqAppleTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = AppTypography,
    shapes = AppShapes,
    content = content,
  )
}
