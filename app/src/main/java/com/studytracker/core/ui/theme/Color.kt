package com.studytracker.core.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// 🌌 CYBER-VIOLET FUTURISTIC DESIGN SYSTEM (Dark Violet Canvas + Neon Accents)
// ============================================================================

// 1. Deep Dark Violet Canvas & Glass Surfaces (Futuristic & OLED-Friendly)
val ZomoDarkCanvas = Color(0xFF090414)        // Deepest cosmic purple background
val ZomoDarkSurface = Color(0xFF140C28)       // Glass card surface
val ZomoDarkCard = Color(0xFF1A1033)          // Elevated card background
val ZomoDarkCardElevated = Color(0xFF231645)  // High elevated surface
val ZomoDarkBorder = Color(0x33A855F7)        // Subtle luminous purple glass border (20% alpha)
val ZomoGlassBorder = Color(0x26C084FC)       // Delicate glow border (15% alpha)

// 2. Futuristic Hero Gradients (Glowing Violet -> Magenta -> Cyan)
val ZomoGradientStart = Color(0xFF6D28D9)      // Vivid deep violet
val ZomoGradientMiddle = Color(0xFF9333EA)     // Electric purple
val ZomoGradientEnd = Color(0xFFD946EF)        // Neon fuchsia / magenta

val ZomoHeroGradient = Brush.horizontalGradient(
    colors = listOf(ZomoGradientStart, ZomoGradientMiddle, ZomoGradientEnd)
)

val ZomoCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1F123D), Color(0xFF130A26))
)

val ZomoGlowGradient = Brush.radialGradient(
    colors = listOf(Color(0x4DA855F7), Color(0x00000000))
)

// 3. High-Contrast Neon Mint / Cyan (Primary CTA buttons, progress sliders, active thumbs)
val ZomoNeonMint = Color(0xFF00F5D4)          // Electric glowing mint
val ZomoNeonMintDark = Color(0xFF0F766E)
val ZomoNeonMintContainer = Color(0x2800F5D4)  // Translucent glowing mint container
val ZomoNeonMintText = Color(0xFF02241F)      // Deep dark contrast text for neon mint buttons

// 4. Vibrant Neon Accent Colors for Categories & Statuses
val ZomoPink = Color(0xFFFF2E93)              // Electric hot pink (videos / urgent)
val ZomoPinkContainer = Color(0x26FF2E93)

val ZomoAmber = Color(0xFFFBBF24)             // Electric gold / amber (paused / goals)
val ZomoAmberContainer = Color(0x26FBBF24)

val ZomoSky = Color(0xFF38BDF8)               // Electric cyan (reading / books)
val ZomoSkyContainer = Color(0x2638BDF8)

val ZomoEmerald = Color(0xFF10B981)           // Emerald green (approved / success)
val ZomoEmeraldContainer = Color(0x2610B981)

val ZomoPurplePrimary = Color(0xFFA855F7)     // Vibrant electric violet
val ZomoPurpleLight = Color(0xFFC084FC)
val ZomoPurpleDark = Color(0xFF7C3AED)
val ZomoVioletContainer = Color(0x26A855F7)

// 5. Crisp High-Contrast Typography
val ZomoTextPrimary = Color(0xFFF8FAFC)       // Pure crisp white
val ZomoTextSecondary = Color(0xFF94A3B8)     // Soft slate / silver
val ZomoTextMuted = Color(0xFF64748B)         // Deep muted slate
val ZomoTextAccent = Color(0xFFC084FC)        // Glowing purple text

// ============================================================================
// Semantic Aliases for Universal Dark Futuristic Experience
// ============================================================================
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
val RoseDark = Color(0xFFFB7185)
val RoseContainer = ZomoPinkContainer

val DarkBackground = ZomoDarkCanvas
val DarkSurface = ZomoDarkSurface
val DarkCard = ZomoDarkCard

val LightBackground = ZomoDarkCanvas
val LightSurface = ZomoDarkSurface
val LightCard = ZomoDarkCard
