package dev.dayboard.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.dayboard.R

/**
 * Inter, bundled and subset to Latin. San Francisco cannot ship in an Android app, and
 * the device's own font is whatever the owner picked, which is no basis for a layout.
 */
val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold)
)

/** Timers and clocks only: fixed-width digits so nothing shifts as the seconds tick. */
private const val TABULAR = "tnum"

private val Trim = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.Both
)

/**
 * Read at arm's length, so roughly 1.5x normal, and tracked in tight as the sizes grow.
 * Large type set at default tracking is the single clearest tell of an unconsidered scale.
 */
val BoardTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 128.sp, lineHeight = 132.sp,
        letterSpacing = (-0.045).em, fontFeatureSettings = TABULAR, lineHeightStyle = Trim
    ),
    displayMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 92.sp, lineHeight = 96.sp,
        letterSpacing = (-0.035).em, fontFeatureSettings = TABULAR, lineHeightStyle = Trim
    ),
    displaySmall = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 52.sp, lineHeight = 58.sp,
        letterSpacing = (-0.03).em, fontFeatureSettings = TABULAR
    ),
    headlineLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 40.sp, lineHeight = 46.sp,
        letterSpacing = (-0.022).em
    ),
    headlineMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 30.sp, lineHeight = 36.sp,
        letterSpacing = (-0.015).em
    ),
    titleLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp,
        letterSpacing = (-0.015).em
    ),
    titleMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 21.sp, lineHeight = 27.sp,
        letterSpacing = (-0.01).em
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 21.sp, lineHeight = 29.sp,
        letterSpacing = (-0.005).em
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 24.sp
    ),
    // Section headers, set small, uppercase and tracked out the way iOS sets them.
    labelLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 20.sp,
        letterSpacing = 0.09.em
    ),
    labelMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp,
        letterSpacing = 0.07.em
    ),
    labelSmall = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 16.sp,
        letterSpacing = 0.04.em
    )
)
