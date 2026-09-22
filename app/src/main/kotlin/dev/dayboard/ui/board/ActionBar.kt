package dev.dayboard.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.dayboard.ui.common.ActionButton
import dev.dayboard.ui.common.CheckGlyph
import dev.dayboard.ui.common.CupGlyph
import dev.dayboard.ui.common.ForwardGlyph
import dev.dayboard.ui.common.PauseGlyph
import dev.dayboard.ui.common.PlayGlyph
import dev.dayboard.ui.common.Tone
import dev.dayboard.ui.theme.SystemGreen
import dev.dayboard.ui.theme.SystemOrange

/** One row, one obvious primary action, everything else quiet beside it. */
@Composable
fun ActionBar(
    running: Boolean,
    paused: Boolean,
    canStart: Boolean,
    canAct: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onBreak: (BreakKind) -> Unit,
    onExtend: () -> Unit,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var chipsOpen by remember { mutableStateOf(false) }

    if (chipsOpen) {
        BreakChips(
            onPick = {
                chipsOpen = false
                onBreak(it)
            },
            onCancel = { chipsOpen = false },
            modifier = modifier
        )
        return
    }

    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        when {
            !running -> ActionButton(
                label = "START",
                modifier = Modifier.weight(1.4f),
                tone = Tone.Filled,
                accent = SystemGreen,
                enabled = canStart,
                glyph = { PlayGlyph(it, 22.dp) },
                onClick = onStart
            )

            paused -> ActionButton(
                label = "RESUME",
                modifier = Modifier.weight(1.4f),
                tone = Tone.Filled,
                accent = SystemGreen,
                enabled = canAct,
                glyph = { PlayGlyph(it, 22.dp) },
                onClick = onResume
            )

            else -> ActionButton(
                label = "PAUSE",
                modifier = Modifier.weight(1.4f),
                enabled = canAct,
                glyph = { PauseGlyph(it, 22.dp) },
                onClick = onPause
            )
        }

        ActionButton(
            label = "BREAK",
            modifier = Modifier.weight(1f),
            enabled = canAct,
            glyph = { CupGlyph(it, 24.dp) },
            onClick = { chipsOpen = true }
        )
        ActionButton("+5 MIN", Modifier.weight(0.9f), enabled = canAct, onClick = onExtend)
        ActionButton(
            label = "DONE",
            modifier = Modifier.weight(1.2f),
            tone = if (running) Tone.Tinted else Tone.Glass,
            accent = SystemGreen,
            enabled = canAct,
            glyph = { CheckGlyph(it, 22.dp) },
            onClick = onDone
        )
        ActionButton(
            label = "SKIP",
            modifier = Modifier.weight(0.9f),
            accent = SystemOrange,
            enabled = canStart || canAct,
            glyph = { ForwardGlyph(it, 20.dp) },
            onClick = onSkip
        )
    }
}

@Composable
private fun BreakChips(onPick: (BreakKind) -> Unit, onCancel: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        ActionButton("WALK 10", Modifier.weight(1f), Tone.Tinted, SystemGreen) { onPick(BreakKind.WALK) }
        ActionButton("TOILET 5", Modifier.weight(1f), Tone.Tinted, SystemGreen) { onPick(BreakKind.TOILET) }
        ActionButton("MEAL 30", Modifier.weight(1f), Tone.Tinted, SystemGreen) { onPick(BreakKind.MEAL) }
        ActionButton("CUSTOM 15", Modifier.weight(1f), Tone.Tinted, SystemGreen) { onPick(BreakKind.CUSTOM) }
        ActionButton("BACK", Modifier.weight(0.7f), onClick = onCancel)
    }
}
