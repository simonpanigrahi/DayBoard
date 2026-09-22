package dev.dayboard.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.dayboard.ui.theme.BoardSurface

private val TARGET = 72.dp

/** Every common action is one tap at a 72dp target. Editing lives elsewhere. */
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

    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        if (!running) {
            BarButton("▶  START", Modifier.weight(1f), canStart, onStart)
        } else if (paused) {
            BarButton("▶  RESUME", Modifier.weight(1f), canAct, onResume)
        } else {
            BarButton("⏸  PAUSE", Modifier.weight(1f), canAct, onPause)
        }
        BarButton("☕  BREAK", Modifier.weight(1f), canAct) { chipsOpen = true }
        BarButton("+5m", Modifier.weight(1f), canAct, onExtend)
        BarButton("✓  DONE", Modifier.weight(1f), canAct, onDone)
    }
}

@Composable
private fun BreakChips(onPick: (BreakKind) -> Unit, onCancel: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        BarButton("WALK 10m", Modifier.weight(1f)) { onPick(BreakKind.WALK) }
        BarButton("TOILET 5m", Modifier.weight(1f)) { onPick(BreakKind.TOILET) }
        BarButton("MEAL 30m", Modifier.weight(1f)) { onPick(BreakKind.MEAL) }
        BarButton("CUSTOM", Modifier.weight(1f)) { onPick(BreakKind.CUSTOM) }
        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(0.6f).height(TARGET)) {
            Text("CANCEL", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun BarButton(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(TARGET),
        colors = ButtonDefaults.buttonColors(
            containerColor = BoardSurface,
            contentColor = MaterialTheme.colorScheme.onBackground
        )
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}
