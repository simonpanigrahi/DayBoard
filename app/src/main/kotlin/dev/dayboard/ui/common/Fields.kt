package dev.dayboard.ui.common

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import dev.dayboard.ui.theme.BoardHairline
import dev.dayboard.ui.theme.BoardSurfaceHigh
import dev.dayboard.ui.theme.LabelPrimary
import dev.dayboard.ui.theme.LabelTertiary
import dev.dayboard.ui.theme.SystemBlue

/** Filled, hairlined, and only coloured while it has focus. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun boardFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = LabelPrimary,
    unfocusedTextColor = LabelPrimary,
    focusedContainerColor = BoardSurfaceHigh,
    unfocusedContainerColor = BoardSurfaceHigh,
    cursorColor = SystemBlue,
    focusedBorderColor = SystemBlue,
    unfocusedBorderColor = BoardHairline,
    focusedPlaceholderColor = LabelTertiary,
    unfocusedPlaceholderColor = LabelTertiary
)
