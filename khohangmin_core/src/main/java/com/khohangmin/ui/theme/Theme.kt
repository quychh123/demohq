package com.khohangmin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightWarehouseScheme =
  lightColorScheme(
    primary = AmberAction,
    onPrimary = Color.White,
    primaryContainer = AmberActionSoft,
    onPrimaryContainer = AmberAction,
    secondary = SlateDeep,
    onSecondary = Color.White,
    secondaryContainer = SoftStoneSurface,
    onSecondaryContainer = SlateDeep,
    tertiary = ForestStock,
    onTertiary = Color.White,
    tertiaryContainer = ForestStockSoft,
    onTertiaryContainer = ForestStock,
    background = WarmPaperBg,
    onBackground = SlateDeep,
    surface = CleanCardWhite,
    onSurface = SlateDeep,
    surfaceVariant = SoftStoneSurface,
    onSurfaceVariant = SlateMuted,
    outline = BorderStone,
    error = CrimsonLow,
    errorContainer = CrimsonLowSoft,
    onErrorContainer = CrimsonLow,
  )

private val DarkWarehouseScheme =
  darkColorScheme(
    primary = AmberBright,
    onPrimary = CharcoalVault,
    primaryContainer = AmberContainerDark,
    onPrimaryContainer = Color(0xFFFFD8C2),
    secondary = OffWhiteInk,
    onSecondary = CharcoalVault,
    secondaryContainer = ElevatedDark,
    onSecondaryContainer = OffWhiteInk,
    tertiary = Color(0xFF4ADE80),
    onTertiary = CharcoalVault,
    tertiaryContainer = Color(0xFF14532D),
    onTertiaryContainer = Color(0xFFBBF7D0),
    background = CharcoalVault,
    onBackground = OffWhiteInk,
    surface = SlateCardDark,
    onSurface = OffWhiteInk,
    surfaceVariant = ElevatedDark,
    onSurfaceVariant = MutedSilver,
    outline = Color(0xFF334155),
    error = Color(0xFFF87171),
    errorContainer = Color(0xFF450A0A),
    onErrorContainer = Color(0xFFFCA5A5),
  )

val WarehouseShapes =
  Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
  )

@Composable
fun KhoHangMinTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val scheme = if (darkTheme) DarkWarehouseScheme else LightWarehouseScheme
  MaterialTheme(
    colorScheme = scheme,
    typography = WarehouseTypography,
    shapes = WarehouseShapes,
    content = content,
  )
}
