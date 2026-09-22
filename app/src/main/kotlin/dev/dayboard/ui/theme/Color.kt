package dev.dayboard.ui.theme

import androidx.compose.ui.graphics.Color
import dev.dayboard.engine.model.ColorRole

// True black stays: the board is always on, and only pixels that are off cost nothing
// and cannot burn in. Everything above it is the iOS dark elevation ramp.
val BoardBlack = Color(0xFF000000)
val BoardSurface = Color(0xFF1C1C1E)
val BoardSurfaceHigh = Color(0xFF2C2C2E)
val BoardHairline = Color(0x14FFFFFF)

// Label colours, as opacities of white rather than separate greys, so they sit on any panel.
val LabelPrimary = Color(0xFFFFFFFF)
val LabelSecondary = Color(0x99EBEBF5)
val LabelTertiary = Color(0x4DEBEBF5)

val SystemBlue = Color(0xFF0A84FF)
val SystemGreen = Color(0xFF30D158)
val SystemIndigo = Color(0xFF5E5CE6)
val SystemOrange = Color(0xFFFF9F0A)
val SystemPink = Color(0xFFFF375F)
val SystemPurple = Color(0xFFBF5AF2)
val SystemRed = Color(0xFFFF453A)
val SystemTeal = Color(0xFF40C8E0)
val SystemGray = Color(0xFF8E8E93)
val SystemGray3 = Color(0xFF48484A)

/** Warnings and overrun. One colour, used for nothing else. */
val BoardAmber = SystemOrange

fun colorFor(role: ColorRole): Color = when (role) {
    ColorRole.DEEP -> SystemBlue
    ColorRole.ADMIN -> SystemIndigo
    ColorRole.REST -> SystemGreen
    ColorRole.MEAL -> SystemOrange
    ColorRole.APPOINTMENT -> SystemRed
    ColorRole.BUFFER -> SystemGray3
    ColorRole.ACCENT_A -> SystemTeal
    ColorRole.ACCENT_B -> SystemPink
    ColorRole.ACCENT_C -> SystemPurple
    ColorRole.NEUTRAL -> SystemGray
}
