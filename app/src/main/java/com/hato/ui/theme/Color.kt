package com.hato.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// SISTEMA DE COLOR "VERDANT PRECISION" (HATO GANADERÍA DIGITAL)
// =========================================================================

// --- MODO OSCURO (DEEP FOREST SLATE & LUMINOUS EMERALD) ---
val SurfaceDark = Color(0xFF0D1512)
val SurfaceDimDark = Color(0xFF0D1512)
val SurfaceBrightDark = Color(0xFF333B37)

val SurfaceContainerLowest = Color(0xFF08100D)
val SurfaceContainerLow = Color(0xFF151D1A)
val SurfaceContainer = Color(0xFF19211E)
val SurfaceContainerHigh = Color(0xFF232C28)
val SurfaceContainerHighest = Color(0xFF2E3733)

val OnSurface = Color(0xFFDCE5DF)
val OnSurfaceVariant = Color(0xFFBBCABF)
val InverseSurface = Color(0xFFDCE5DF)
val InverseOnSurface = Color(0xFF2A322F)

val Outline = Color(0xFF86948A)
val OutlineVariant = Color(0xFF3C4A42)

val Primary = Color(0xFF4EDEA3)
val OnPrimary = Color(0xFF003824)
val PrimaryContainer = Color(0xFF10B981)
val OnPrimaryContainer = Color(0xFF00422B)
val InversePrimary = Color(0xFF006C49)

val Secondary = Color(0xFF68DBA9)
val OnSecondary = Color(0xFF003825)
val SecondaryContainer = Color(0xFF25A475)
val OnSecondaryContainer = Color(0xFF00311F)

val Tertiary = Color(0xFF45DFA4)
val OnTertiary = Color(0xFF003825)
val TertiaryContainer = Color(0xFF00B982)
val OnTertiaryContainer = Color(0xFF00422C)

// Alertas Urgentes (Rojo)
val Error = Color(0xFFFFB4AB)
val OnError = Color(0xFF690005)
val ErrorContainer = Color(0xFF93000A)
val OnErrorContainer = Color(0xFFFFDAD6)

// Alertas Preventivas (Amarillo suave / Amber acorde a la app)
val WarningYellow = Color(0xFFFBBF24)
val WarningYellowContainer = Color(0xFF3B2800)
val OnWarningYellowContainer = Color(0xFFFFDF9E)

// --- MODO CLARO (HIGH-VISIBILITY FIELD MOCKUP SYSTEM) ---
val LightBackground = Color(0xFFF8FAF9)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFF1F6F3)
val LightSurfaceContainer = Color(0xFFE8F1EC)
val LightSurfaceContainerHigh = Color(0xFFDEECE5)
val LightSurfaceContainerHighest = Color(0xFFD4E5DC)

val LightOnSurface = Color(0xFF0F1E17)
val LightOnSurfaceVariant = Color(0xFF526058)

val LightPrimary = Color(0xFF005C3B) // Deep Forest Green matching Stitch mockup
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFD1FAE5) // Mint
val LightOnPrimaryContainer = Color(0xFF064E3B)

val LightSecondary = Color(0xFF006C49)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFD4F7E3)
val LightOnSecondaryContainer = Color(0xFF003825)

val LightTertiary = Color(0xFF10B981)
val LightOutline = Color(0xFF78887F)
val LightOutlineVariant = Color(0xFFE2ECE6)

val LightError = Color(0xFFDC2626)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFEE2E2)
val LightOnErrorContainer = Color(0xFF7F1D1D)

val LightWarningYellow = Color(0xFFD97706)
val LightWarningYellowContainer = Color(0xFFFEF3C7)
val LightOnWarningYellowContainer = Color(0xFF78350F)

// Aliases para compatibilidad con código existente
val PrimaryGreen = Primary
val PrimaryGreenFixed = PrimaryContainer
val OnPrimaryFixed = OnPrimary
val SecondaryBlue = Secondary
val SecondaryBlueContainer = SecondaryContainer
val BackgroundDark = SurfaceDark
val SurfaceVariantDark = SurfaceContainerHighest
val TextWhite = OnSurface
val TextLightGray = OnSurfaceVariant
val TextDimGray = Outline
val TextGreen = Primary
val ErrorRed = Error
