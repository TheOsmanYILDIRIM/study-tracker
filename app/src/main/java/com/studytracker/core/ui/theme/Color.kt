package com.studytracker.core.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// 🌌 ZEN STARRY NIGHT & STORYBOOK PAPER CRAFT DESIGN SYSTEM
// Layered paper cutout depth, peaceful night sky & nature aesthetics
// ============================================================================

// 1. Deep Midnight Blue & Starry Night Surfaces (Calming, Eye-friendly, OLED optimized)
val ZenNightCanvas = Color(0xFF080D1A)         // Deepest night sky canvas
val ZenNightSurface = Color(0xFF10192D)        // Solid night slate card for zero-lag rendering
val ZenNightCard = Color(0xFF141F36)           // Elevated soft slate paper card
val ZenNightCardElevated = Color(0xFF1A2845)   // High elevation paper surface
val ZenNightBorder = Color(0x3838BDF8)         // Soft aurora glow glass border (22% cyan alpha)
val ZenGlassBorder = Color(0x24FFFFFF)         // Delicate translucent white border (14% alpha)

// Semi-transparent Backplates for Text & Header Readability
val ZenTextBackplate = Color(0xDC080E1B)       // Frosted dark backplate for crisp text reading
val ZenTopBarBackplate = Color(0xEE080D1A)     // Semi-transparent top bar backing

// Solid High-Performance Paper craft layers (prevents expensive GPU alpha overdraw during scroll)
val ZenPaperCard = Color(0xFF111B30)
val ZenPaperElevated = Color(0xFF182642)
val ZenPaperBorder = Color(0x4038BDF8)

// Backward compatible aliases
val ZomoDarkCanvas = ZenNightCanvas
val ZomoDarkSurface = ZenNightSurface
val ZomoDarkCard = ZenNightCard
val ZomoDarkCardElevated = ZenNightCardElevated
val ZomoDarkBorder = ZenNightBorder
val ZomoGlassBorder = ZenGlassBorder

// 2. Serene Hero Gradients (Deep Indigo -> Mountain Teal -> Starry Cyan)
val ZenGradientStart = Color(0xFF1E293B)       // Serene slate blue
val ZenGradientMiddle = Color(0xFF0F3A5D)      // Mountain lake indigo
val ZenGradientEnd = Color(0xFF0369A1)         // Midnight cyan glow

val ZomoGradientStart = Color(0xFF1E293B)
val ZomoGradientMiddle = Color(0xFF0F3A5D)
val ZomoGradientEnd = Color(0xFF0369A1)

val ZenHeroGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF0F172A), Color(0xFF162D4A), Color(0xFF0C4A6E))
)

val ZomoHeroGradient = ZenHeroGradient

val ZomoCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF131D33), Color(0xFF0A1020))
)

val ZomoGlowGradient = Brush.radialGradient(
    colors = listOf(Color(0x3338BDF8), Color(0x00000000))
)

// 3. Calming Moon & Aurora Accents
val ZenMoonGold = Color(0xFFFBBF24)            // Warm golden moon accent
val ZenMoonGoldContainer = Color(0x28FBBF24)
val ZenSkyCyan = Color(0xFF38BDF8)             // Serene lake cyan
val ZenSkyCyanContainer = Color(0x2838BDF8)
val ZenMintSoft = Color(0xFF2DD4BF)            // Soft nature mint
val ZenMintContainer = Color(0x282DD4BF)
val ZenMintText = Color(0xFF032824)
val ZenForestGreen = Color(0xFF10B981)         // Pine emerald
val ZenForestContainer = Color(0x2810B981)
val ZenRoseCoral = Color(0xFFF43F5E)           // Gentle sunset coral
val ZenRoseContainer = Color(0x28F43F5E)
val ZenLavender = Color(0xFFA78BFA)            // Twilight violet
val ZenLavenderContainer = Color(0x28A78BFA)

// Mappings for existing symbols
val ZomoNeonMint = ZenSkyCyan
val ZomoNeonMintDark = Color(0xFF0284C7)
val ZomoNeonMintContainer = ZenSkyCyanContainer
val ZomoNeonMintText = Color(0xFF082F49)

val ZomoPink = ZenRoseCoral
val ZomoPinkContainer = ZenRoseContainer

val ZomoAmber = ZenMoonGold
val ZomoAmberContainer = ZenMoonGoldContainer

val ZomoSky = ZenSkyCyan
val ZomoSkyContainer = ZenSkyCyanContainer

val ZomoEmerald = ZenForestGreen
val ZomoEmeraldContainer = ZenForestContainer

val ZomoPurplePrimary = ZenLavender
val ZomoPurpleLight = Color(0xFFC4B5FD)
val ZomoPurpleDark = Color(0xFF7C3AED)
val ZomoVioletContainer = ZenLavenderContainer

// 4. Crisp High-Contrast Typography
val ZomoTextPrimary = Color(0xFFF8FAFC)        // Pure crisp white
val ZomoTextSecondary = Color(0xFF94A3B8)      // Serene slate / silver
val ZomoTextMuted = Color(0xFF64748B)          // Deep muted slate
val ZomoTextAccent = ZenSkyCyan                // Glowing starry cyan

// Semantic Aliases
val ZomoBackground = ZomoDarkCanvas
val ZomoLavenderBg = ZomoDarkCanvas
val ZomoCardBackground = ZomoDarkSurface
val ZomoLavenderSurface = ZomoDarkSurface
val ZomoLavenderCard = ZomoDarkCard
val ZomoLavenderBorder = ZomoDarkBorder
val ZomoSoftLavender = ZomoDarkCardElevated
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
val RoseDark = Color(0xFFE11D48)
val RoseContainer = ZomoPinkContainer

val DarkBackground = ZomoDarkCanvas
val DarkSurface = ZomoDarkSurface
val DarkCard = ZomoDarkCard

val LightBackground = ZomoDarkCanvas
val LightSurface = ZomoDarkSurface
val LightCard = ZomoDarkCard
