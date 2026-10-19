package com.focusflow.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Default: "Hourglass Flame" — Electric Lime & Ember Logo Theme (Matches App Logo)
var FocusBackground by mutableStateOf(Color(0xFF0C100D))      // Deep luxury obsidian dark canvas
var FocusPanel by mutableStateOf(Color(0xFF141C14))           // Rich dark charcoal container with subtle forest undertone
var FocusPanelLight by mutableStateOf(Color(0xFF1C271C))      // Elevated interactive card
var FocusPanelBorder by mutableStateOf(Color(0xFF283827))     // Subtle lime-tinted border
val FocusBorder get() = FocusPanelBorder

// Primary Logo Accents: Electric Lime (#84CC16) + Ember Gold (#FBBF24) + Emerald (#10B981)
var FocusAccent by mutableStateOf(Color(0xFF84CC16))          // Electric Lime (matches logo frame & caps)
var FocusAccentDark by mutableStateOf(Color(0xFF65A30D))      // Deep forest lime
var FocusAccentSoft by mutableStateOf(Color(0x2484CC16))      // Translucent lime aura
var FocusAccentGlow by mutableStateOf(Color(0x4084CC16))      // Luminous lime glow

var FocusSecondary by mutableStateOf(Color(0xFF98AA98))       // Soft sage secondary text
var FocusMuted by mutableStateOf(Color(0xFF687A68))           // Dim forest subtitle
var FocusWhite by mutableStateOf(Color(0xFFF7FCF7))           // Crisp snow white

// Status & Category Badges
val PriorityHigh = Color(0xFFFF5C5C)
val PriorityMedium = Color(0xFFFFB05C)
val PriorityLow = Color(0xFF5CA4FF)
val CategoryUrgent = Color(0xFFFF5C5C)
val FocusRed = PriorityHigh
val FocusYellow = PriorityMedium
val FocusGreen get() = FocusAccent

// Logo Specific Colors
val LogoLime = Color(0xFF84CC16)
val LogoLimeLight = Color(0xFFA3E635)
val LogoEmerald = Color(0xFF10B981)
val LogoAmber = Color(0xFFF59E0B)
val LogoGold = Color(0xFFFBBF24)
val LogoFlare = Color(0xFFFEF08A)

// Aurora Mind Colors (for alternate theme)
val AuroraViolet = Color(0xFF7C4DFF)
val AuroraMagenta = Color(0xFFE040FB)
val AuroraCoral = Color(0xFFFF6B6B)

val CategoryWork = Color(0xFF84CC16)
val CategoryStudy = Color(0xFF10B981)
val CategoryPersonal = Color(0xFFFBBF24)
val CategoryHealth = Color(0xFF6EE7B7)

// Dynamic Theme Switcher for Real-time Reskinning
fun applyThemeById(themeId: String) {
    when (themeId) {
        "theme_flame", "theme_hourglass", "theme_aurora", "theme_lime", "theme_emerald" -> { // Hourglass Flame (Logo-Matched Default)
            FocusBackground = Color(0xFF0C100D)
            FocusPanel = Color(0xFF141C14)
            FocusPanelLight = Color(0xFF1C271C)
            FocusPanelBorder = Color(0xFF283829)
            FocusAccent = Color(0xFF84CC16)
            FocusAccentDark = Color(0xFF65A30D)
            FocusAccentSoft = Color(0x2484CC16)
            FocusAccentGlow = Color(0x4084CC16)
            FocusSecondary = Color(0xFF98AA98)
            FocusMuted = Color(0xFF687A68)
            FocusWhite = Color(0xFFF7FCF7)
        }
        "theme_aurora_coral" -> { // Aurora Mind (Coral & Violet)
            FocusBackground = Color(0xFF0A0E1A)
            FocusPanel = Color(0xFF131835)
            FocusPanelLight = Color(0xFF1C2248)
            FocusPanelBorder = Color(0xFF2A3060)
            FocusAccent = Color(0xFFFF6B6B)
            FocusAccentDark = Color(0xFFE04848)
            FocusAccentSoft = Color(0x24FF6B6B)
            FocusAccentGlow = Color(0x40FF6B6B)
            FocusSecondary = Color(0xFF8B7EC8)
            FocusMuted = Color(0xFF635B85)
            FocusWhite = Color(0xFFEDE9FE)
        }
        "theme_lime", "theme_emerald" -> { // Classic Lime Zen
            FocusBackground = Color(0xFF0F1014)
            FocusPanel = Color(0xFF181A22)
            FocusPanelLight = Color(0xFF242733)
            FocusPanelBorder = Color(0xFF2D3140)
            FocusAccent = Color(0xFF9BE564)
            FocusAccentDark = Color(0xFF75B942)
            FocusAccentSoft = Color(0x249BE564)
            FocusAccentGlow = Color(0x409BE564)
            FocusSecondary = Color(0xFF949BA8)
            FocusMuted = Color(0xFF676E7E)
            FocusWhite = Color(0xFFF6F7FA)
        }
        "theme_midnight" -> { // Glacier Frost
            FocusBackground = Color(0xFF080D1A)
            FocusPanel = Color(0xFF0F172A)
            FocusPanelLight = Color(0xFF1E293B)
            FocusPanelBorder = Color(0xFF334155)
            FocusAccent = Color(0xFF38BDF8)
            FocusAccentDark = Color(0xFF0284C7)
            FocusAccentSoft = Color(0x2438BDF8)
            FocusAccentGlow = Color(0x4038BDF8)
            FocusSecondary = Color(0xFF64748B)
            FocusMuted = Color(0xFF475569)
            FocusWhite = Color(0xFFF0F9FF)
        }
        "theme_cyberpunk" -> { // Cyberpunk Neon
            FocusBackground = Color(0xFF0E0716)
            FocusPanel = Color(0xFF170C24)
            FocusPanelLight = Color(0xFF28143F)
            FocusPanelBorder = Color(0xFF4A1E75)
            FocusAccent = Color(0xFFEC4899)
            FocusAccentDark = Color(0xFFBE185D)
            FocusAccentSoft = Color(0x24EC4899)
            FocusAccentGlow = Color(0x40EC4899)
            FocusSecondary = Color(0xFF9C7AB0)
            FocusMuted = Color(0xFF6B4D80)
            FocusWhite = Color(0xFFFAF0FF)
        }
        "theme_sunset" -> { // Solar Amber
            FocusBackground = Color(0xFF140D05)
            FocusPanel = Color(0xFF1E1408)
            FocusPanelLight = Color(0xFF30200D)
            FocusPanelBorder = Color(0xFF4D3315)
            FocusAccent = Color(0xFFF59E0B)
            FocusAccentDark = Color(0xFFD97706)
            FocusAccentSoft = Color(0x24F59E0B)
            FocusAccentGlow = Color(0x40F59E0B)
            FocusSecondary = Color(0xFFA38A5C)
            FocusMuted = Color(0xFF7A6540)
            FocusWhite = Color(0xFFFFF8E8)
        }
    }
}