package dev.dayboard.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.dayboard.ui.theme.BoardAmber
import dev.dayboard.ui.theme.BoardBlack
import dev.dayboard.ui.theme.BoardDim
import dev.dayboard.ui.theme.BoardSurface
import dev.dayboard.ui.theme.RoleRest

private val TARGET = 84.dp

/** Every common action is one tap at a target you can hit without looking. */
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
            !running -> BarButton("▶  START", Modifier.weight(1.3f), canStart, primary = BoardAmber, onClick = onStart)
            paused -> BarButton("▶  RESUME", Modifier.weight(1.3f), canAct, primary = BoardAmber, onClick = onResume)
            else -> BarButton("⏸  PAUSE", Modifier.weight(1.3f), canAct, onClick = onPause)
        }
        BarButton("☕  BREAK", Modifier.weight(1f), canAct) { chipsOpen = true }
        BarButton("+5m", Modifier.weight(0.8f), canAct, onClick = onExtend)
        BarButton("✓  DONE", Modifier.weight(1.3f), canAct, primary = if (running) RoleRest else null, onClick = onDone)
        BarButton("SKIP", Modifier.weight(0.8f), canStart || canAct, onClick = onSkip)
    }
}

@Composable
private fun BreakChips(onPick: (BreakKind) -> Unit, onCancel: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        BarButton("WALK 10m", Modifier.weight(1f), primary = RoleRest) { onPick(BreakKind.WALK) }
        BarButton("TOILET 5m", Modifier.weight(1f), primary = RoleRest) { onPick(BreakKind.TOILET) }
        BarButton("MEAL 30m", Modifier.weight(1f), primary = RoleRest) { onPick(BreakKind.MEAL) }
        BarButton("CUSTOM 15m", Modifier.weight(1f), primary = RoleRest) { onPick(BreakKind.CUSTOM) }
        BarButton("BACK", Modifier.weight(0.7f), onClick = onCancel)
    }
}

@Composable
private fun BarButton(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Color? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier.height(TARGET),
        colors = ButtonDefaults.buttonColors(
            containerColor = primary ?: BoardSurface,
            contentColor = if (primary != null) BoardBlack else MaterialTheme.colorScheme.onBackground,
            disabledContainerColor = BoardSurface,
            disabledContentColor = BoardDim.copy(alpha = 0.45f)
        )
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}
