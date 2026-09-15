package com.studytracker.core.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Zomo File Brand Palette (Vibrant Violet, Neon Mint & Soft Lavender)
val ZomoPurplePrimary = Color(0xFF7C3AED)
val ZomoPurpleLight = Color(0xFFA78BFA)
val ZomoPurpleDark = Color(0xFF5B21B6)

// Hero Gradients
val ZomoGradientStart = Color(0xFF6D28D9)
val ZomoGradientMiddle = Color(0xFF8B5CF6)
val ZomoGradientEnd = Color(0xFFA855F7)

val ZomoHeroGradient = Brush.horizontalGradient(
    colors = listOf(ZomoGradientStart, ZomoGradientMiddle, ZomoGradientEnd)
)

val ZomoCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF7C3AED), Color(0xFF9333EA))
)

// High-Contrast Neon Mint / Cyan (CTAs, sliders, progress bars)
val ZomoNeonMint = Color(0xFF2DD4BF)
val ZomoNeonMintDark = Color(0xFF0D9488)
val ZomoNeonMintContainer = Color(0xFFCCFBF1)
val ZomoNeonMintText = Color(0xFF0F766E)

// Soft Lavender / Lilac Canvas & Surfaces
val ZomoLavenderBg = Color(0xFFF5F2FB)
val ZomoLavenderSurface = Color(0xFFFFFFFF)
val ZomoLavenderCard = Color(0xFFECE7F8)
val ZomoLavenderBorder = Color(0xFFE2DCF3)
val ZomoTextPrimary = Color(0xFF1E1B4B)
val ZomoTextSecondary = Color(0xFF6B7280)

// Category Pastel Squircles (Zomo Subject & Action Tiles)
val ZomoPink = Color(0xFFF43F5E)
val ZomoPinkContainer = Color(0xFFFFE4E6)

val ZomoAmber = Color(0xFFF59E0B)
val ZomoAmberContainer = Color(0xFFFEF3C7)

val ZomoSky = Color(0xFF0EA5E9)
val ZomoSkyContainer = Color(0xFFE0F2FE)

val ZomoEmerald = Color(0xFF10B981)
val ZomoEmeraldContainer = Color(0xFFD1FAE5)

val ZomoViolet = Color(0xFF8B5CF6)
val ZomoVioletContainer = Color(0xFFEDE9FE)

// Dark Theme Variants
val ZomoDarkBg = Color(0xFF0E0B1F)
val ZomoDarkSurface = Color(0xFF1B1633)
val ZomoDarkCard = Color(0xFF272147)
val ZomoDarkBorder = Color(0xFF38305F)

// Backward Compatibility & Semantic Aliases for components
val ZomoSoftLavender = ZomoLavenderCard
val ZomoCardBackground = ZomoLavenderSurface
val ZomoBackground = ZomoLavenderBg
val ZomoMintAccent = ZomoNeonMint
val ZomoSquirclePurple = ZomoVioletContainer
val ZomoSquircleAmber = ZomoAmberContainer
val ZomoSquircleEmerald = ZomoEmeraldContainer
val ZomoSquirclePink = ZomoPinkContainer
val ZomoSquircleBlue = ZomoSkyContainer
val ZomoSquircleSky = ZomoSkyContainer

val SapphirePrimary = ZomoPurplePrimary
val SapphireDark = ZomoPurpleLight
val SapphireContainer = ZomoVioletContainer

val EmeraldSuccess = ZomoEmerald
val EmeraldDark = ZomoNeonMint
val EmeraldContainer = ZomoEmeraldContainer

val AmberWarning = ZomoAmber
val AmberDark = Color(0xFFD97706)
val AmberContainer = ZomoAmberContainer

val PurpleActive = ZomoPurplePrimary
val PurpleDark = ZomoPurpleLight
val PurpleContainer = ZomoVioletContainer

val RoseReject = ZomoPink
val RoseDark = Color(0xFFFB7185)
val RoseContainer = ZomoPinkContainer

val DarkBackground = ZomoDarkBg
val DarkSurface = ZomoDarkSurface
val DarkCard = ZomoDarkCard

val LightBackground = ZomoLavenderBg
val LightSurface = ZomoLavenderSurface
val LightCard = ZomoLavenderCard


