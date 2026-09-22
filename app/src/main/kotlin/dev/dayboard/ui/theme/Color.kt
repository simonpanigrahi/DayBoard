package dev.dayboard.ui.theme

import androidx.compose.ui.graphics.Color
import dev.dayboard.engine.model.ColorRole

// True black, not near-black: the board is always on, and an OLED panel only saves
// power and avoids burn-in on pixels that are actually off.
val BoardBlack = Color(0xFF000000)
val BoardSurface = Color(0xFF101010)
val BoardText = Color(0xFFEDEDED)
val BoardDim = Color(0xFF7C7C7C)
val BoardAmber = Color(0xFFFFB020)

val RoleDeep = Color(0xFF5B8DEF)
val RoleAdmin = Color(0xFF9B7BEA)
val RoleRest = Color(0xFF4CAF7D)
val RoleMeal = Color(0xFFD9904B)
val RoleAppointment = Color(0xFFD9584B)
val RoleBuffer = Color(0xFF3A4350)
val RoleNeutral = Color(0xFF6E7681)

fun colorFor(role: ColorRole): Color = when (role) {
    ColorRole.DEEP -> RoleDeep
    ColorRole.ADMIN -> RoleAdmin
    ColorRole.REST -> RoleRest
    ColorRole.MEAL -> RoleMeal
    ColorRole.APPOINTMENT -> RoleAppointment
    ColorRole.BUFFER -> RoleBuffer
    ColorRole.ACCENT_A -> RoleDeep
    ColorRole.ACCENT_B -> RoleAdmin
    ColorRole.ACCENT_C -> RoleMeal
    ColorRole.NEUTRAL -> RoleNeutral
}
