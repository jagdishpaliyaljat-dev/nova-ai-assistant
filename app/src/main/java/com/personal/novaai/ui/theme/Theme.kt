package com.personal.novaai.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val NavyDeep = Color(0xFF070B19)
val NavySurface = Color(0xFF0F172A)
val NavyCard = Color(0xFF1E293B)
val ElectricBlue = Color(0xFF00E5FF)
val ElectricBlueGlow = Color(0xFF00B0FF)
val VioletNeon = Color(0xFF7C4DFF)
val VioletGlow = Color(0xFFB388FF)
val EmeraldSuccess = Color(0xFF00E676)
val CrimsonError = Color(0xFFFF1744)
val AmberWarning = Color(0xFFFFAB00)
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val GlassBorder = Color(0x3300E5FF)
val GlassSurface = Color(0x221E293B)

val NovaColorScheme = darkColorScheme(
    primary = ElectricBlue,
    secondary = VioletNeon,
    tertiary = VioletGlow,
    background = NavyDeep,
    surface = NavySurface,
    onPrimary = NavyDeep,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)
